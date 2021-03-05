package de.hawhh.cas.wise2020.hausarbeit.model.config_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {

    private Integer runtimeHours;

    private List<StationConfig> stations;

    private List<LinkConfig> links;

    private List<LineConfig> lines;
}
