package org.dcsa.ebl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.core.model.AuditBase;
import org.dcsa.core.model.GetId;
import org.dcsa.ebl.model.enums.TemperatureUnit;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@Data
public class ActiveReeferSettings extends AuditBase implements GetId<UUID> {
    @JsonProperty("shipmentEquipmentID")
    @Column("shipment_equipment_id")
    private UUID id;

    @Column("temperature_min")
    private Float temperatureMin;

    @Column("temperature_max")
    private Float temperatureMax;

    @Column("temperature_unit")
    @Size(max = 3)
    private TemperatureUnit temperatureUnit;

    public void setTemperatureUnit(@NotNull String temperatureUnit) {
        this.temperatureUnit = TemperatureUnit.valueOf(temperatureUnit);
    }

    public void setTemperatureUnit(@NotNull TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }
    
    @Column("humidity_min")
    private Float humidityMin;

    @Column("humidity_max")
    private Float humidityMax;

    @Column("ventilation_min")
    private Float ventilationMin;

    @Column("ventilation_max")
    private Float ventilationMax;
}
