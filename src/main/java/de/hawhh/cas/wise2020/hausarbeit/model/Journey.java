package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Journey {

    private int direction;

    private boolean isDeparture = true;

    private List<Map<Station, LocalDateTime>> timetable;

    private Station from;

    private Station to;


}
