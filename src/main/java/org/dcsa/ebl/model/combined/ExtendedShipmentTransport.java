package org.dcsa.ebl.model.combined;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.JoinedWithModel;
import org.dcsa.core.model.ModelClass;
import org.dcsa.core.model.PrimaryModel;
import org.dcsa.core.model.ViaJoinAlias;
import org.dcsa.ebl.model.*;
import org.dcsa.ebl.model.enums.DCSATransportType;
import org.dcsa.ebl.model.transferobjects.LocationTO;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.sql.Join;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@PrimaryModel(ShipmentTransport.class)
@JoinedWithModel(lhsFieldName = "transportID", rhsModel = Transport.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)

@JoinedWithModel(lhsModel = Transport.class, lhsFieldName = "modeOfTransport",
        rhsModel = ModeOfTransport.class, rhsFieldName = "modeOfTransportCode", joinType = Join.JoinType.LEFT_OUTER_JOIN)

@JoinedWithModel(lhsModel = Transport.class, lhsFieldName = "loadTransportCall",
        rhsJoinAlias = "ltc", rhsModel = TransportCall.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)
@JoinedWithModel(lhsJoinAlias = "ltc", lhsModel = TransportCall.class, lhsFieldName = "locationID",
        rhsJoinAlias = "ll", rhsModel = Location.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)
@JoinedWithModel(lhsJoinAlias = "ll", lhsModel = Location.class, lhsFieldName = "addressID",
        rhsJoinAlias = "la", rhsModel = Address.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)

@JoinedWithModel(lhsModel = Transport.class, lhsFieldName = "dischargeTransportCall",
        rhsJoinAlias = "dtc", rhsModel = TransportCall.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)
@JoinedWithModel(lhsJoinAlias = "dtc", lhsModel = TransportCall.class, lhsFieldName = "locationID",
        rhsJoinAlias = "dl", rhsModel = Location.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)
@JoinedWithModel(lhsJoinAlias = "dl", lhsModel = Location.class, lhsFieldName = "addressID",
        rhsJoinAlias = "da", rhsModel = Address.class, rhsFieldName = "id", joinType = Join.JoinType.LEFT_OUTER_JOIN)

public class ExtendedShipmentTransport extends ShipmentTransport {

    // Load Transport

    @ModelClass(value = Transport.class, fieldName = "vesselIMONumber")
    private String vesselIMONumber;

    @ModelClass(value = ModeOfTransport.class, fieldName = "modeOfTransportType")
    private DCSATransportType modeOfTransport;

    // Load TransportCall ID
    @ModelClass(value = TransportCall.class, fieldName = "id")
    @ViaJoinAlias("ltc")
    private UUID loadTransportCallId;

    // Load Location

    @ModelClass(value = Location.class, fieldName = "id")
    @ViaJoinAlias("ll")
    private UUID loadLocationId;

    @ModelClass(value = Location.class, fieldName = "locationName")
    @ViaJoinAlias("ll")
    private String loadLocationLocationName;

    @ModelClass(value = Location.class, fieldName = "addressID")
    @ViaJoinAlias("ll")
    private UUID loadLocationAddressID;

    @ModelClass(value = Location.class, fieldName = "latitude")
    @ViaJoinAlias("ll")
    private String loadLocationLatitude;

    @ModelClass(value = Location.class, fieldName = "longitude")
    @ViaJoinAlias("ll")
    private String loadLocationLongitude;

    @ModelClass(value = Location.class, fieldName = "unLocationCode")
    @ViaJoinAlias("ll")
    private String loadLocationUnLocationCode;

    // Load Location Address

    @ModelClass(value = Address.class, fieldName = "id")
    @ViaJoinAlias("la")
    private UUID loadAddressId;

    @ModelClass(value = Address.class, fieldName = "name")
    @ViaJoinAlias("la")
    private String loadAddressName;

    @ModelClass(value = Address.class, fieldName = "street")
    @ViaJoinAlias("la")
    private String loadAddressStreet;

    @ModelClass(value = Address.class, fieldName = "streetNumber")
    @ViaJoinAlias("la")
    private String loadAddressStreetNumber;

    @ModelClass(value = Address.class, fieldName = "floor")
    @ViaJoinAlias("la")
    private String loadAddressFloor;

    @ModelClass(value = Address.class, fieldName = "postalCode")
    @ViaJoinAlias("la")
    private String loadAddressPostalCode;

    @ModelClass(value = Address.class, fieldName = "city")
    @ViaJoinAlias("la")
    private String loadAddressCity;

    @ModelClass(value = Address.class, fieldName = "stateRegion")
    @ViaJoinAlias("la")
    private String loadAddressStateRegion;

    @ModelClass(value = Address.class, fieldName = "country")
    @ViaJoinAlias("la")
    private String loadAddressCountry;

    // Discharge TransportCall ID
    @ModelClass(value = TransportCall.class, fieldName = "id")
    @ViaJoinAlias("dtc")
    private UUID dischargeTransportCallId;

    // Discharge Location

    @ModelClass(value = Location.class, fieldName = "id")
    @ViaJoinAlias("dl")
    private UUID dischargeLocationId;

    @ModelClass(value = Location.class, fieldName = "locationName")
    @ViaJoinAlias("dl")
    private String dischargeLocationLocationName;

    @ModelClass(value = Location.class, fieldName = "addressID")
    @ViaJoinAlias("dl")
    private UUID dischargeLocationAddressID;

    @ModelClass(value = Location.class, fieldName = "latitude")
    @ViaJoinAlias("dl")
    private String dischargeLocationLatitude;

    @ModelClass(value = Location.class, fieldName = "longitude")
    @ViaJoinAlias("dl")
    private String dischargeLocationLongitude;

    @ModelClass(value = Location.class, fieldName = "unLocationCode")
    @ViaJoinAlias("dl")
    private String dischargeLocationUnLocationCode;

    // Discharge Location Address

    @ModelClass(value = Address.class, fieldName = "id")
    @ViaJoinAlias("da")
    private UUID dischargeAddressId;

    @ModelClass(value = Address.class, fieldName = "name")
    @ViaJoinAlias("da")
    private String dischargeAddressName;

    @ModelClass(value = Address.class, fieldName = "street")
    @ViaJoinAlias("da")
    private String dischargeAddressStreet;

    @ModelClass(value = Address.class, fieldName = "streetNumber")
    @ViaJoinAlias("da")
    private String dischargeAddressStreetNumber;

    @ModelClass(value = Address.class, fieldName = "floor")
    @ViaJoinAlias("da")
    private String dischargeAddressFloor;

    @ModelClass(value = Address.class, fieldName = "postalCode")
    @ViaJoinAlias("da")
    private String dischargeAddressPostalCode;

    @ModelClass(value = Address.class, fieldName = "city")
    @ViaJoinAlias("da")
    private String dischargeAddressCity;

    @ModelClass(value = Address.class, fieldName = "stateRegion")
    @ViaJoinAlias("da")
    private String dischargeAddressStateRegion;

    @ModelClass(value = Address.class, fieldName = "country")
    @ViaJoinAlias("da")
    private String dischargeAddressCountry;

    public Address getLoadAddress() {
        Address loadAddress = new Address();
        loadAddress.setName(getLoadAddressName());
        loadAddress.setStreet(getLoadAddressStreet());
        loadAddress.setStreetNumber(getLoadAddressStreetNumber());
        loadAddress.setFloor(getLoadAddressFloor());
        loadAddress.setPostalCode(getLoadAddressPostalCode());
        loadAddress.setCity(getLoadAddressCity());
        loadAddress.setStateRegion(getLoadAddressStateRegion());
        loadAddress.setCountry(getLoadAddressCountry());
        return loadAddress;
    }

    public Address getDischargeAddress() {
        Address dischargeAddress = new Address();
        dischargeAddress.setName(getDischargeAddressName());
        dischargeAddress.setStreet(getDischargeAddressStreet());
        dischargeAddress.setStreetNumber(getDischargeAddressStreetNumber());
        dischargeAddress.setFloor(getDischargeAddressFloor());
        dischargeAddress.setPostalCode(getDischargeAddressPostalCode());
        dischargeAddress.setCity(getDischargeAddressCity());
        dischargeAddress.setStateRegion(getDischargeAddressStateRegion());
        dischargeAddress.setCountry(getDischargeAddressCountry());
        return dischargeAddress;
    }
    
    public LocationTO getLoadLocationTO() {
        LocationTO loadLocation = new LocationTO();
        loadLocation.setAddress(getLoadAddress());
        loadLocation.setLocationName(getLoadLocationLocationName());
        loadLocation.setLatitude(getLoadLocationLatitude());
        loadLocation.setLongitude(getLoadLocationLongitude());
        loadLocation.setUnLocationCode(getLoadLocationUnLocationCode());
        return loadLocation;
    }

    public LocationTO getDischargeLocationTO() {
        LocationTO dischargeLocation = new LocationTO();
        dischargeLocation.setAddress(getDischargeAddress());
        dischargeLocation.setLocationName(getDischargeLocationLocationName());
        dischargeLocation.setLatitude(getDischargeLocationLatitude());
        dischargeLocation.setLongitude(getDischargeLocationLongitude());
        dischargeLocation.setUnLocationCode(getDischargeLocationUnLocationCode());
        return dischargeLocation;
    }
}
