package org.dcsa.ebl.model.transferobjects;

import lombok.Value;
import org.dcsa.core.events.model.enums.DCSATransportType;
import org.dcsa.core.events.model.transferobjects.LocationTO;

@Value
public class TransportTO {
  LocationTO loadLocation;
  LocationTO dischargeLocation;
  DCSATransportType modeOfTransport;
  String vesselIMONumber;
  String carrierVoyageNumber;
  boolean isUnderShippersResponsibility;
}
