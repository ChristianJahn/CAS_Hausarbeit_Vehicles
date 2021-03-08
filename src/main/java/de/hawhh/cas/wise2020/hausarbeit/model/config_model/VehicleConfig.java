package de.hawhh.cas.wise2020.hausarbeit.model.config_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleConfig {

    private Integer distance;

    private Integer count;

    private Boolean isCooperative; 
}
