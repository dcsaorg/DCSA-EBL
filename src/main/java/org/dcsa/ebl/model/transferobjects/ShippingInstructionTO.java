package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Location;
import org.dcsa.ebl.model.Reference;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShippingInstructionTO extends AbstractShippingInstruction {

    @NotNull
    @Valid
    private List<CargoItemTO> cargoItems;

    @NotNull
    @Valid
    private List<ShipmentEquipmentTO> shipmentEquipments;

    @NotNull
    @Valid
    private List<Reference> references;

    @NotNull
    @Valid
    private List<DocumentPartyTO> documentParties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String carrierBookingReference;

    @NotNull
    private LocationTO freightPayableAt;

    /**
     * Pull the carrierBookingReference from cargo items into the ShippingInstruction if possible
     *
     * If the CargoItems all have the same carrierBookingReference, the value is moved up to the
     * this ShippingInstruction and cleared from the CargoItems.
     *
     * This is useful on output to "prettify" the resulting ShippingInstruction to avoid unnecessary
     * "per CargoItem" booking references.  The method is idempotent.
     *
     * This is more or less the logical opposite of {@link #pushCarrierBookingReferenceIntoCargoItemsIfNecessary()}.
     *
     * @throws IllegalStateException If the ShippingInstruction already has a carrierBookingReference
     * and it is not exactly the same as it would get after this call.
     * @throws NullPointerException If one or more CargoItemTOs had a null carrierBookingReference AND one or more
     * of them had a non-null carrierBookingReference.  (I.e. either they all must have a carrierBookingReference
     * or none of them can have one).
     */
    @JsonIgnore
    public void hoistCarrierBookingReferenceIfPossible() {
        String actualCentralBookingReference = this.getCarrierBookingReference();
        String possibleCentralBookingReference = cargoItems.isEmpty() ? null : cargoItems.get(0).getCarrierBookingReference();
        Boolean allNull = null;
        for (CargoItemTO cargoItemTO : cargoItems) {
            String cargoBookingReference = cargoItemTO.getCarrierBookingReference();
            if (cargoBookingReference == null) {
                if (allNull == Boolean.FALSE) {
                    throw new NullPointerException("One of the CargoItemTOs had a null carrierBookingReference while another did not");
                }
                allNull = Boolean.TRUE;
                continue;
            }

            if (allNull == Boolean.TRUE) {
                throw new NullPointerException("One of the CargoItemTOs had a null carrierBookingReference while another did not");
            }
            allNull = Boolean.FALSE;
            if (!cargoBookingReference.equals(possibleCentralBookingReference)) {
                possibleCentralBookingReference = null;
                break;
            }
        }
        if (actualCentralBookingReference != null && !actualCentralBookingReference.equals(possibleCentralBookingReference)) {
            throw new IllegalStateException("Internal error: ShippingInstruction had booking reference "
                    + this.getCarrierBookingReference() + " but it should have been: " + actualCentralBookingReference);
        }
        if (possibleCentralBookingReference != null) {
            // Hoist up the booking reference to the SI since it is the same on all items.
            for (CargoItemTO cargoItemTO : cargoItems) {
                cargoItemTO.setCarrierBookingReference(null);
            }
            this.setCarrierBookingReference(possibleCentralBookingReference);
        }
    }

    /**
     * Pushes the carrierBookingReference to cargoItems and clears it if it is not null
     *
     * This is useful on input to "normalize" the ShippingInstruction so the code can always
     * assume that the booking reference will appear on the cargoItems.  The method is idempotent.
     *
     * This is more or less the logical opposite of {@link #hoistCarrierBookingReferenceIfPossible()}.
     *
     * @throws IllegalStateException If the ShippingInstruction and one of its CargoItem
     * both have a non-null carrierBookingReference.
     */
    @JsonIgnore
    public void pushCarrierBookingReferenceIntoCargoItemsIfNecessary() {
        String centralBookingReference = this.getCarrierBookingReference();
        if (centralBookingReference != null) {
            for (CargoItemTO cargoItemTO : this.cargoItems) {
                String cargoBookingReference = cargoItemTO.getCarrierBookingReference();
                if (cargoBookingReference != null) {
                    throw new IllegalStateException("CarrierBookingReference defined on SI and CargoItemTO level.");
                }
                cargoItemTO.setCarrierBookingReference(centralBookingReference);
            }
            this.setCarrierBookingReference(null);
        }
    }
}

