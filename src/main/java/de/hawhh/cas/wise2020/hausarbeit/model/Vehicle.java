package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Vehicle {
    private Strategy strategy;

    private String name;

    private Line line;

    private State state;

    private Station currentStation;

    private Link currentLink;

    private Time time;

    private int deviation;

    private Schedule schedule;

    private int direction = 0;

    private int DEVIATION_THRESHHOLD;


    public void doNextAction() {
        if(state.equals(State.ON_STOP)){ // is on a station
            if(schedule.canProceedFromStop(time.getCurrentTime(), currentStation)) { // check if the time has come
                Link nextLink = direction == 0 ? line.getNextLinkA(currentStation) : line.getNextLinkB(currentStation);
                if (nextLink.hasCapacity()) {
                    nextLink.goToLink(this);
                    currentLink = nextLink;
                    state = State.TRAVELING;
                }
            }
        }else { // Is on a trip
            //TODO check if travel time has passed
            Station nextStation = currentLink.getTo();
            if(currentLink.canProceedToNextStation(this)){
                currentLink.unregisterFirst();
                currentStation = nextStation;
                state = State.ON_STOP;
            }
        }
    }
}
