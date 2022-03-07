package org.dcsa.ebl.repository.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.ebl.repository.ShippingInstructionCustomRepository;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.dialect.RenderContextFactory;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.SQL;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

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

    @Data
    @RequiredArgsConstructor
    private static class Pair {
        private final String shippingInstructionReference;
        private final String carrierBookingReference;
    }

    @Override
    public Mono<Map<String, List<String>>> findCarrierBookingReferences(List<String> shippingInstructionReferences) {
        if (shippingInstructionReferences.isEmpty()) {
            log.debug("No shipping instructions found");
            return Mono.just(Collections.emptyMap());
        }

        Select query = Select.builder()
                .select(List.of(ShippingInstructionSpec.id, ShipmentSpec.carrierBookingReference))
                .from(ShippingInstructionSpec.table, CargoItemSpec.table, ShipmentEquipmentSpec.table, ShipmentSpec.table)
                .where(
                        isEqual(ShippingInstructionSpec.id, CargoItemSpec.shippingInstructionReference)
                                .and(isEqual(CargoItemSpec.shipmentEquipmentId, ShipmentEquipmentSpec.id))
                                .and(isEqual(ShipmentEquipmentSpec.shipmentId, ShipmentSpec.id))
                                .and(columnIn(ShippingInstructionSpec.id, shippingInstructionReferences))
                ).build();

        RenderContextFactory factory = new RenderContextFactory(r2dbcDialect);
        SqlRenderer sqlRenderer = SqlRenderer.create(factory.createRenderContext());

        String sql = sqlRenderer.render(query);
        log.debug("select = {}", sql);

        return addBinds(client.sql(sql), shippingInstructionReferences)
                .map(row ->
                    new Pair(
                            row.get("id", String.class),
                            row.get("carrier_booking_reference", String.class)
                            )
                )
                .all()
                .collect(
                        HashMap::new,
                        (map, value) -> map.computeIfAbsent(value.shippingInstructionReference, k -> new ArrayList<>()).add(value.carrierBookingReference)
                );
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
    }

    private static class CargoItemSpec {
        public static final Table table = Table.create("cargo_item");
        public static final Column id = Column.create("id", table);
        public static final Column shippingInstructionReference = Column.create("shipping_instruction_id", table);
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
