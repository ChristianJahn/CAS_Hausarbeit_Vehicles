package de.hawhh.cas.wise2020.hausarbeit.model.config_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkConfig {

    private Integer id;

    private Integer from;

    private Integer to;

    private Integer distance;

    private Integer capacity;
}
