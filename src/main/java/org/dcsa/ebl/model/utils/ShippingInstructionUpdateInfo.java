package org.dcsa.ebl.model.utils;

import lombok.Data;
import lombok.NonNull;
import org.dcsa.core.events.model.transferobjects.ShippingInstructionTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ShippingInstructionUpdateInfo {

    private final String shippingInstructionReference;
    private final ShippingInstructionTO shippingInstructionTO;

    private Map<String, String> equipmentReference2CarrierBookingReference;
    private Map<String, UUID> carrierBookingReference2ShipmentID;
    private Map<String, UUID> equipmentReference2ShipmentEquipmentID;

    public List<UUID> getAllShipmentIDs() {
        if (carrierBookingReference2ShipmentID == null) {
            throw new IllegalStateException("Called too early");
        }
        return new ArrayList<>(carrierBookingReference2ShipmentID.values());
    }

    public UUID getShipmentIDForCarrierBookingReference(@NonNull String carrierBookingReference) {
        if (carrierBookingReference2ShipmentID == null) {
            throw new IllegalStateException("Called too early");
        }
        UUID uuid = carrierBookingReference2ShipmentID.get(carrierBookingReference);
        if (uuid == null) {
            throw new IllegalArgumentException(carrierBookingReference);
        }
        return uuid;
    }

    public UUID getShipmentIDForEquipmentReference(@NonNull String equipmentReference) {
        if (equipmentReference2CarrierBookingReference == null) {
            throw new IllegalStateException("Called too early");
        }
        String carrierBookingReference = equipmentReference2CarrierBookingReference.get(equipmentReference);
        if (carrierBookingReference == null) {
            throw new IllegalArgumentException(equipmentReference);
        }
        return getShipmentIDForCarrierBookingReference(carrierBookingReference);
    }

    public UUID getShipmentEquipmentIDFor(@NonNull String equipmentReference) {
        if (equipmentReference2ShipmentEquipmentID == null) {
            throw new IllegalStateException("Called too early");
        }
        UUID uuid = equipmentReference2ShipmentEquipmentID.get(equipmentReference);
        if (uuid == null) {
            throw new IllegalArgumentException(equipmentReference);
        }
        return uuid;
    }
}
