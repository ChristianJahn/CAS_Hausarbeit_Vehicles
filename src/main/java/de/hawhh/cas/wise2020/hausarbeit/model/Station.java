package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(of = {"name"})
public class Station {
    private final boolean allowTurn;

    private final String name;

    private final int capacity;

    private final int waitingTimeInSeconds;

    private List<Vehicle> vehiclesInStation;

    private Map<Line, Long> waitingTimeByLine;

    private Map<Line, Map<LocalDateTime, Long>> waitingTimeHistory;

    private LocalDateTime arrivalTimePreviousVehicle;

    private Map<Vehicle, Integer> deviationByVehicle;

    public boolean hasCapacity() {
        return vehiclesInStation.size() < capacity;
    }

    public void unregister(Vehicle vehicle) {
        vehiclesInStation.remove(vehicle);
    }

    public void register(Vehicle vehicle) {
        vehiclesInStation.add(vehicle);
    }
}
