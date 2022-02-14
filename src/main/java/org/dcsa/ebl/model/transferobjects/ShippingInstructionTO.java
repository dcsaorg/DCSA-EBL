package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.events.model.transferobjects.*;
import org.dcsa.ebl.model.base.AbstractShippingInstruction;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShippingInstructionTO extends AbstractShippingInstruction {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String carrierBookingReference;

  private LocationTO placeOfIssue;

  @JsonProperty("utilizedTransportEquipments")
  @Valid
  private List<ShipmentEquipmentTO> shipmentEquipments;

  @Valid private List<DocumentPartyTO> documentParties;

  @Valid private List<ReferenceTO> references;

  /**
   * Pull the carrierBookingReference from cargo items into the ShippingInstruction if possible
   *
   * <p>If the CargoItems all have the same carrierBookingReference, the value is moved up to this
   * ShippingInstruction and cleared from the CargoItems.
   *
   * <p>This is useful on output to "prettify" the resulting ShippingInstruction to avoid
   * unnecessary "per CargoItem" booking references. The method is idempotent.
   *
   * <p>This is more or less the logical opposite of {@link
   * #pushCarrierBookingReferenceIntoCargoItemsIfNecessary()}.
   *
   * @throws IllegalStateException If the ShippingInstruction already has a carrierBookingReference
   *     and it is not exactly the same as it would get after this call.
   * @throws NullPointerException If one or more CargoItemTOs had a null carrierBookingReference AND
   *     one or more of them had a non-null carrierBookingReference. (I.e. either they all must have
   *     a carrierBookingReference or none of them can have one).
   */
  @JsonIgnore
  public void hoistCarrierBookingReferenceIfPossible() {

    List<CargoItemTO> cargoItems = new ArrayList<>();
    for (ShipmentEquipmentTO shipmentEquipmentTO : shipmentEquipments) {
      cargoItems.addAll(shipmentEquipmentTO.getCargoItems());
    }

    String actualCentralBookingReference = this.getCarrierBookingReference();
    String possibleCentralBookingReference =
        cargoItems.isEmpty() ? null : cargoItems.get(0).getCarrierBookingReference();
    Boolean allNull = null;
    for (CargoItemTO cargoItemTO : cargoItems) {
      String cargoBookingReference = cargoItemTO.getCarrierBookingReference();
      if (cargoBookingReference == null) {
        if (allNull == Boolean.FALSE) {
          throw new NullPointerException(
              "One of the CargoItemTOs had a null carrierBookingReference while another did not");
        }
        allNull = Boolean.TRUE;
        continue;
      }

      if (allNull == Boolean.TRUE) {
        throw new NullPointerException(
            "One of the CargoItemTOs had a null carrierBookingReference while another did not");
      }
      allNull = Boolean.FALSE;
      if (!cargoBookingReference.equals(possibleCentralBookingReference)) {
        possibleCentralBookingReference = null;
        break;
      }
    }
    if (actualCentralBookingReference != null
        && !actualCentralBookingReference.equals(possibleCentralBookingReference)) {
      throw new IllegalStateException(
          "Internal error: ShippingInstruction had booking reference "
              + this.getCarrierBookingReference()
              + " but it should have been: "
              + actualCentralBookingReference);
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
   * <p>This is useful on input to "normalize" the ShippingInstruction so the code can always assume
   * that the booking reference will appear on the cargoItems. The method is idempotent.
   *
   * <p>This is more or less the logical opposite of {@link
   * #hoistCarrierBookingReferenceIfPossible()}.
   *
   * @throws IllegalStateException If the ShippingInstruction and one of its CargoItem both have a
   *     non-null carrierBookingReference.
   */
  @JsonIgnore
  public void pushCarrierBookingReferenceIntoCargoItemsIfNecessary() {
    if (this.carrierBookingReference != null && this.shipmentEquipments == null) return;

    if (this.carrierBookingReference == null
        && ( this.shipmentEquipments == null || this.shipmentEquipments.stream()
            .map(
                x ->
                    x.getCargoItems().stream()
                        .allMatch(y -> y.getCarrierBookingReference() == null))
            .collect(Collectors.toList())
            .contains(true))) {
      throw new IllegalStateException(
              "CarrierBookingReference needs to be defined on either ShippingInstruction or CargoItemTO level.");
    }

    String centralBookingReference = this.getCarrierBookingReference();
    if (centralBookingReference != null) {
      for (ShipmentEquipmentTO shipmentEquipmentTO : this.shipmentEquipments) {
        for (CargoItemTO cargoItemTO : shipmentEquipmentTO.getCargoItems()) {
          String cargoBookingReference = cargoItemTO.getCarrierBookingReference();
          if (cargoBookingReference != null) {
            throw new IllegalStateException(
                "CarrierBookingReference defined on both ShippingInstruction and CargoItemTO level.");
          }
          cargoItemTO.setCarrierBookingReference(centralBookingReference);
        }
      }
      this.setCarrierBookingReference(null);
    }
  }
}
