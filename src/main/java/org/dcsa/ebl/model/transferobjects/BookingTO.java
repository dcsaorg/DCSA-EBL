package org.dcsa.ebl.model.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.Booking;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("")
@NoArgsConstructor
@Data
public class BookingTO extends Booking {
    private List<RequestedEquipmentTO> requestedEquipments;
}
