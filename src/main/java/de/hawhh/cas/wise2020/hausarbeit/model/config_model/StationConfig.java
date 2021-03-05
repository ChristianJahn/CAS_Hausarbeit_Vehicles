package de.hawhh.cas.wise2020.hausarbeit.model.config_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationConfig {

    private Integer id;

    private String name;

    private Integer capacity;

    private Integer defaultWaitingTime;

    private boolean turnAllowed;
}
