package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationConfig {

    private boolean shortTurnAllowed;

    private boolean skipAllowed;


}
