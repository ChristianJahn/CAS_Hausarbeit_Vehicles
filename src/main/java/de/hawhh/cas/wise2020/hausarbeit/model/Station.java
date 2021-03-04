package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(of = {"name"})
@ToString(exclude = {"waitingTimeByLine", "waitingTimeHistory"})
public class Station {
    private final boolean allowTurn;

    private final String name;

    private final int capacity;

    private final int waitingTimeInSeconds;

    private Time time;

    private List<Line> linesDepartingFromHere;

    private List<Vehicle> vehiclesInStation;

    private Map<String, Long> waitingTimeByLine;

    private Map<LocalDateTime, Map<String, Long>> waitingTimeHistory;

    private LocalDateTime arrivalTimePreviousVehicle;

    private Map<Vehicle, Integer> deviationByVehicle;

    public boolean hasCapacity() {
        return vehiclesInStation.size() < capacity;
    }

    public void unregister(Vehicle vehicle) {
        vehiclesInStation.remove(vehicle);
    }

    public void register(Vehicle vehicle) {
        waitingTimeHistory.put(time.getCurrentTime(), waitingTimeByLine);
        waitingTimeByLine.put(vehicle.getLine().getName(), 0L);
        vehiclesInStation.add(vehicle);
    }

    public void tick(){
        for(String line : new ArrayList<>(this.waitingTimeByLine.keySet())){
            if(waitingTimeByLine.get(line) == null){
                waitingTimeByLine.put(line, 0L);
            }
            Long newTime =  waitingTimeByLine.get(line) + 1;
            waitingTimeByLine.put(line,newTime);
        }

    }
}
