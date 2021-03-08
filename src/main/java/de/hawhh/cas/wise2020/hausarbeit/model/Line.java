package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode
@ToString(exclude = {"vehiclesOnLineA", "vehiclesOnLineB"})
public class Line {

    private String name;

    private List<Link> linksDirectionA;

    private List<Schedule> shedules;

    private List<Vehicle> vehiclesOnLineA = new ArrayList<>();

    private List<Vehicle> vehiclesOnLineB = new ArrayList<>();

    private List<Link> linksDirectionB;

    private Station start;

    private Time time;

    private Station end;

    public boolean isAtEndA(Station currentStation){
        return currentStation.equals(linksDirectionA.get(linksDirectionA.size() - 1).getTo());
    }

    public boolean isAtEndB(Station currentStation){
        return currentStation.equals(linksDirectionB.get(linksDirectionB.size() - 1).getTo());
    }

    public Link getNextLinkB(Station currentStation) {
        return linksDirectionB.stream()
                .filter(l -> l.getFrom().getName().equals(currentStation.getName()))
                .findFirst()
                .orElse(null);
    }

    public Link getNextLinkA(Station currentStation) {
        return linksDirectionA.stream()
                .filter(l -> l.getFrom().getName().equals(currentStation.getName()))
                .findFirst()
                .orElse(null);
    }

    public Long getNextSheduledArrival(Station currentStation, Schedule schedule, Vehicle vehicle) {
        int positionOfVehicle = -1;
        if(vehicle.getDirection() == 0){
            positionOfVehicle = vehiclesOnLineA.indexOf(vehicle);
        }else {
            positionOfVehicle = vehiclesOnLineB.indexOf(vehicle);
        }
        if(positionOfVehicle != -1 && positionOfVehicle != 0 && !isAtEndB(currentStation) && !isAtEndA(currentStation)){
            Vehicle beforeVehicle = vehiclesOnLineA.get(positionOfVehicle + 1);
            LocalDateTime estimatedArrivalTime = estimateArrival(beforeVehicle, currentStation);
            if(estimatedArrivalTime == null) return 0L;
            return estimatedArrivalTime.toEpochSecond(ZoneOffset.UTC);
        }
       return 0L;
    }

    private LocalDateTime estimateArrival(Vehicle beforeVehicle, Station currentStation) {
        if(beforeVehicle.getState().equals(State.ON_STOP)){
            LocalDateTime estimatedArrivalTime =time.getCurrentTime();
            Station m = beforeVehicle.getCurrentStation();
            return calculateEstimatedArrivalTime(beforeVehicle, currentStation, estimatedArrivalTime, m);
        }else if (beforeVehicle.getState().equals(State.TRAVELING)){
            int remainingTimeToNextStop = beforeVehicle.getCurrentLink().getDISTANCE() - beforeVehicle.getTraveledTime();
            LocalDateTime estimatedArrivalTime = time.getCurrentTime().plusSeconds(remainingTimeToNextStop);
            Station m = beforeVehicle.getSchedule().getNextStation();
            return calculateEstimatedArrivalTime(beforeVehicle, currentStation, estimatedArrivalTime, m);
        }else {
            return null;
        }
    }

    private LocalDateTime calculateEstimatedArrivalTime(Vehicle beforeVehicle, Station currentStation, LocalDateTime estimatedArrivalTime, Station m) {
        Link link = getLinkFromStation(m, beforeVehicle.getDirection());
        while(!m.equals(currentStation) ){
            Station next = link.getTo();
            estimatedArrivalTime = estimatedArrivalTime.plusSeconds(link.getDISTANCE());
            m = next;
            link = getLinkFromStation(m, beforeVehicle.getDirection());
        }
        return estimatedArrivalTime;
    }

    private Link getLinkFromStation(Station j, int direction) {
        if(direction == 0){
            return linksDirectionA.stream().filter(l -> l.getFrom().equals(j)).findFirst().orElse(null);
        }else {
            return linksDirectionB.stream().filter(l -> l.getFrom().equals(j)).findFirst().orElse(null);
        }
    }

    private Station getSuccessorStation(Station nextStation, int direction) {
        if(direction == 0){
            return linksDirectionA.stream().filter(l -> l.getFrom().equals(nextStation)).map(Link::getTo).findAny().orElse(null);
        }else {
            return linksDirectionB.stream().filter(l -> l.getFrom().equals(nextStation)).map(Link::getTo).findAny().orElse(null);
        }
    }
}
