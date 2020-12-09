package org.dcsa.ebl.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.ebl.model.CargoItem;
import org.dcsa.ebl.model.CargoLineItem;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Data
public class CargoItemTO extends CargoItem {
    private List<CargoLineItem> cargoLineItems;

    @JsonIgnore
    @Size(max = 15)
    private String equipmentReference;

    public CargoItem getCargoItem() {
        CargoItem cargoItem = new CargoItem();

        cargoItem.setId(getId());
        cargoItem.setShipmentID(getShipmentID());
        cargoItem.setDescriptionOfGoods(getDescriptionOfGoods());
        cargoItem.setHsCode(getHsCode());
        cargoItem.setWeight(getWeight());
        cargoItem.setWeightUnit(getWeightUnit());
        cargoItem.setVolume(getVolume());
        cargoItem.setVolumeUnit(getVolumeUnit());
        cargoItem.setNumberOfPackages(getNumberOfPackages());
        cargoItem.setPackageCode(getPackageCode());
//        ...missing properties

        return cargoItem;
    }
}
