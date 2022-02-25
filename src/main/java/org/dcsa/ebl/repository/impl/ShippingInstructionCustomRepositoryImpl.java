package org.dcsa.ebl.repository.impl;

import io.r2dbc.spi.Row;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.ebl.model.transferobjects.ShippingInstructionSummaryTO;
import org.dcsa.ebl.repository.ShippingInstructionCustomRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.dialect.RenderContextFactory;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.SQL;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.data.relational.core.sql.Conditions.isEqual;

@Slf4j
@RequiredArgsConstructor
public class ShippingInstructionCustomRepositoryImpl implements ShippingInstructionCustomRepository {
    private final DatabaseClient client;
    private final R2dbcDialect r2dbcDialect;

    /**
     * Looks up ShippingInstructions by carrierBookingReference and documentStatus.
     *
     * The carrier_booking_reference we are looking for are on shipment.
     * The relation between them is like this:
     * shipping_instruction <- cargo_items -> shipment_equipment -> shipment
     *
     * @param carrierBookingReferences
     * @param documentStatus
     * @param pageable
     * @return
     */
    public Flux<ShippingInstructionSummaryTO> findShippingInstructions(List<String> carrierBookingReferences, ShipmentEventTypeCode documentStatus, Pageable pageable) {
        Flux<ShippingInstructionSummaryTO> noCarrierBookingReferences = findShippingInstructionsInternal(carrierBookingReferences, documentStatus, pageable);
        return populateCarrierBookingReferences(noCarrierBookingReferences);
    }

    /**
     * Finds the ShippingInstructions, but does not populate the carrierBookingReferences.
     */
    private Flux<ShippingInstructionSummaryTO> findShippingInstructionsInternal(List<String> carrierBookingReferences, ShipmentEventTypeCode documentStatus, Pageable pageable) {
        log.debug("carrierBookingReferences=", carrierBookingReferences);

        SelectBuilder.SelectFromAndJoin partialQuery1 = Select.builder()
                .select(ShippingInstructionSpec.allFields).distinct()
                .from(ShippingInstructionSpec.table)
                .limitOffset(pageable.getPageSize(), pageable.getOffset());

        SelectBuilder.SelectWhere partialQuery2 = partialQuery1;
        if (!carrierBookingReferences.isEmpty()) {
            partialQuery2 = partialQuery1
                    .join(CargoItemSpec.table).on(CargoItemSpec.shippingInstructionId).equals(ShippingInstructionSpec.id)
                    .join(ShipmentEquipmentSpec.table).on(ShipmentEquipmentSpec.id).equals(CargoItemSpec.shipmentEquipmentId)
                    .join(ShipmentSpec.table).on(ShipmentSpec.id).equals(ShipmentEquipmentSpec.shipmentId);
        }
        Select query = addWhereConditions(partialQuery2, documentStatus, carrierBookingReferences).build();

        RenderContextFactory factory = new RenderContextFactory(r2dbcDialect);
        SqlRenderer sqlRenderer = SqlRenderer.create(factory.createRenderContext());

        String sql = sqlRenderer.render(query);
        log.debug("select = {}", sql);

        return addBinds(client.sql(sql), carrierBookingReferences)
                .map(ShippingInstructionSpec::mapShippingInstruction)
                .all();
    }

    private Flux<ShippingInstructionSummaryTO> populateCarrierBookingReferences(Flux<ShippingInstructionSummaryTO> summaries) {
        log.debug("populateCarrierBookingReferences");

        return summaries
                .collectList()
                .map(shippingInstructions -> findCarrierBookingReferences(
                        shippingInstructions.stream().map(si -> si.getShippingInstructionID()).collect(Collectors.toList())
                    ).map(map -> shippingInstructions.stream()
                        .map(si -> si.withCarrierBookingReferences(map.getOrDefault(si.getShippingInstructionID(), Collections.emptyList())))
                        .collect(Collectors.toList())
                ))
                .flatMap(monolist -> monolist)
                .flatMapMany(Flux::fromIterable);
    }

    @Data
    @RequiredArgsConstructor
    private static class Pair {
        private final String shippingInstructionId;
        private final String carrierBookingReference;
    }

    private Mono<Map<String, List<String>>> findCarrierBookingReferences(List<String> shippingInstructionIds) {
        if (shippingInstructionIds.isEmpty()) {
            log.debug("No shipping instructions found");
            return Mono.just(Collections.emptyMap());
        }

        Select query = Select.builder()
                .select(List.of(ShippingInstructionSpec.id, ShipmentSpec.carrierBookingReference))
                .from(ShippingInstructionSpec.table, CargoItemSpec.table, ShipmentEquipmentSpec.table, ShipmentSpec.table)
                .where(
                        isEqual(ShippingInstructionSpec.id, CargoItemSpec.shippingInstructionId)
                                .and(isEqual(CargoItemSpec.shipmentEquipmentId, ShipmentEquipmentSpec.id))
                                .and(isEqual(ShipmentEquipmentSpec.shipmentId, ShipmentSpec.id))
                                .and(columnIn(ShippingInstructionSpec.id, shippingInstructionIds))
                ).build();

        RenderContextFactory factory = new RenderContextFactory(r2dbcDialect);
        SqlRenderer sqlRenderer = SqlRenderer.create(factory.createRenderContext());

        String sql = sqlRenderer.render(query);
        log.debug("select = {}", sql);

        return addBinds(client.sql(sql), shippingInstructionIds)
                .map(row ->
                    new Pair(
                            row.get("id", String.class),
                            row.get("carrier_booking_reference", String.class)
                            )
                )
                .all()
                .collect(
                        HashMap::new,
                        (map, value) -> map.computeIfAbsent(value.shippingInstructionId, k -> new ArrayList<>()).add(value.carrierBookingReference)
                );
    }

    /**
     * Workaround for SelectBuilder not being very flexible.
     */
    private SelectBuilder.BuildSelect addWhereConditions(SelectBuilder.SelectWhere selectWhere, ShipmentEventTypeCode documentStatus, List<String> carrierBookingReferences) {
        List<Condition> conditions = new ArrayList<>();
        if (documentStatus != null) {
            conditions.add(isEqual(ShippingInstructionSpec.documentStatus, SQL.literalOf(documentStatus.name())));
        }
        if (!carrierBookingReferences.isEmpty()) {
            conditions.add(columnIn(ShipmentSpec.carrierBookingReference, carrierBookingReferences));
        }
        if (!conditions.isEmpty()) {
            Condition whereCondition = conditions.get(0);
            for (int i = 1; i < conditions.size(); i++) {
                whereCondition = whereCondition.and(conditions.get(i));
            }
            return selectWhere.where(whereCondition);
        }
        return selectWhere;
    }

    private Condition columnIn(Column column, List<String> refs) {
        return Conditions.in(column,
                IntStream.range(0, refs.size())
                        .mapToObj(i -> SQL.bindMarker(":ref" + i))
                        .collect(Collectors.toList()));
    }

    private DatabaseClient.GenericExecuteSpec addBinds(DatabaseClient.GenericExecuteSpec spec, List<String> refs) {
        for (int i = 0; i < refs.size(); i++) {
            log.debug("Binding ref{} to {}", i, refs.get(i));
            spec = spec.bind("ref" + i, refs.get(i));
        }
        return spec;
    }

    private static class ShippingInstructionSpec {
        public static final Table table = Table.create("shipping_instruction");

        public static final Column id = Column.create("id", table);
        public static final Column documentStatus = Column.create("document_status", table);
        public static final Column createdDateTime = Column.create("created_date_time", table);
        public static final Column updatedDateTime = Column.create("updated_date_time", table);
        public static final Column transportDocumentTypeCode = Column.create("transport_document_type_code", table);
        public static final Column isShippedOnboardType = Column.create("is_shipped_onboard_type", table);
        public static final Column numberOfCopies = Column.create("number_of_copies", table);
        public static final Column numberOfOriginals = Column.create("number_of_originals", table);
        public static final Column isElectronic = Column.create("is_electronic", table);
        public static final Column isToOrder = Column.create("is_to_order", table);
        public static final Column areChargesDisplayedOnOriginals = Column.create("are_charges_displayed_on_originals", table);
        public static final Column areChargesDisplayedOnCopies = Column.create("are_charges_displayed_on_copies", table);
        public static final Column displayedNameForPlaceOfReceipt = Column.create("displayed_name_for_place_of_receipt", table);
        public static final Column displayedNameForPortOfLoad = Column.create("displayed_name_for_port_of_load", table);
        public static final Column displayedNameForPortOfDischarge = Column.create("displayed_name_for_port_of_discharge", table);
        public static final Column displayedNameForPlaceOfDelivery = Column.create("displayed_name_for_place_of_delivery", table);

        public static final List<Column> allFields =
                List.of(id, documentStatus, createdDateTime, updatedDateTime, transportDocumentTypeCode,
                        isShippedOnboardType, numberOfCopies, numberOfOriginals, isElectronic, isToOrder, areChargesDisplayedOnCopies,
                        areChargesDisplayedOnOriginals, displayedNameForPlaceOfReceipt, displayedNameForPortOfLoad,
                        displayedNameForPortOfDischarge, displayedNameForPlaceOfDelivery);

        public static ShippingInstructionSummaryTO mapShippingInstruction(Row row) {
            return ShippingInstructionSummaryTO.builder()
                    .shippingInstructionID(row.get("id", String.class))
                    .documentStatus(documentStatus(row.get("document_status", String.class)))
                    .createdDateTime(row.get("created_date_time", OffsetDateTime.class))
                    .updatedDateTime(row.get("updated_date_time", OffsetDateTime.class))
                    .transportDocumentTypeCode(row.get("transport_document_type_code", String.class))
                    .shippedOnboardType(row.get("is_shipped_onboard_type", Boolean.class))
                    .numberOfCopies(row.get("number_of_copies", Integer.class))
                    .numberOfOriginals(row.get("number_of_originals", Integer.class))
                    .isElectronic(row.get("is_electronic", Boolean.class))
                    .isToOrder(row.get("is_to_order", Boolean.class))
                    .areChargesDisplayedOnOriginals(row.get("are_charges_displayed_on_originals", Boolean.class))
                    .areChargesDisplayedOnCopies(row.get("are_charges_displayed_on_copies", Boolean.class))
                    .displayedNameForPlaceOfReceipt(row.get("displayed_name_for_place_of_receipt", String.class))
                    .displayedNameForPortOfLoad(row.get("displayed_name_for_port_of_load", String.class))
                    .displayedNameForPortOfDischarge(row.get("displayed_name_for_port_of_discharge", String.class))
                    .displayedNameForPlaceOfDelivery(row.get("displayed_name_for_place_of_delivery", String.class))
                    .build();
        }

        private static ShipmentEventTypeCode documentStatus(String value) {
            return value != null ? ShipmentEventTypeCode.valueOf(value) : null;
        }
    }

    private static class CargoItemSpec {
        public static final Table table = Table.create("cargo_item");
        public static final Column id = Column.create("id", table);
        public static final Column shippingInstructionId = Column.create("shipping_instruction_id", table);
        public static final Column shipmentEquipmentId = Column.create("shipment_equipment_id", table);
    }

    private static class ShipmentSpec {
        public static final Table table = Table.create("shipment");
        public static final Column id = Column.create("id", table);
        public static final Column carrierBookingReference = Column.create("carrier_booking_reference", table);
    }

    private static class ShipmentEquipmentSpec {
        public static final Table table = Table.create("shipment_equipment");
        public static final Column id = Column.create("id", table);
        public static final Column shipmentId = Column.create("shipment_id", table);
    }
}
