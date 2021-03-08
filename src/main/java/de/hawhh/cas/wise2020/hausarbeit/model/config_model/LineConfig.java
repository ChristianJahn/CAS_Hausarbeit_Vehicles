package de.hawhh.cas.wise2020.hausarbeit.model.config_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineConfig {

    private String name;

    private Integer id;

    private VehicleConfig vehicleConfig;

    private List<Integer> linksDirectionA;

    private List<Integer> linksDirectionB;

    private int pauseTime;
}
