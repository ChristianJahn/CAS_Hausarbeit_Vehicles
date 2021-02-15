package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Station {
    private final boolean allowTurn;

    private final String name;

    private final int capacity;

    private final int waitingTimeInSeconds;

    private List<Vehicle> vehiclesInStation;

    public boolean hasCapacity() {
        return vehiclesInStation.size() < capacity;
    }
}
