package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class Link {

    private final int CAPACITY;

    private List<Vehicle> vehicles;

    private final int DISTANCE;

    private final Station from;

    private final Station to;

    private Map<Route, Double> delayByRoute;

    public boolean hasCapacity() {
        return vehicles.size() < CAPACITY;
    }

    public void goToLink(Vehicle vehicle){
        vehicles.add(vehicle);
    }

    public void unregisterFirst(){
        vehicles.remove(0);
    }

    public boolean canProceedToNextStation(Vehicle vehicle){
        return vehicles.get(0).equals(vehicle) && to.hasCapacity();
    }
}
