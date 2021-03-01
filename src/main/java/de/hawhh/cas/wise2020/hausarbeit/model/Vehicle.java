package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@EqualsAndHashCode(of = {"name"})
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

    private int traveledTime = 0;

    private int DEVIATION_THRESHOLD = 300; // 5 minutes is default


    public void doNextAction() {
        if(state.equals(State.ON_STOP)){ // is on a station

            if(schedule.canProceedFromStop(time.getCurrentTime(), currentStation)) { // check if the time has come
                Link nextLink = direction == 0 ? line.getNextLinkA(currentStation) : line.getNextLinkB(currentStation);
                if (nextLink.hasCapacity()) {
                    this.deviation = schedule.calcDeviation(currentStation, time);
                    currentStation.unregister(this);
                    nextLink.goToLink(this);
                    log.info("Vehicle {} departs from stop (allowed) {} on link from {} to {} with capacity {} and has deviation {}", this.name,
                            currentStation.getName(),
                            nextLink.getFrom().getName(), nextLink.getTo().getName(), nextLink.getCAPACITY() - nextLink.getVehicles().size(), deviation);
                    currentLink = nextLink;
                    state = State.TRAVELING;
                }
            }
        }else if(state.equals(State.TRAVELING)){ // Is on a trip
            //TODO check if travel time has passed
            Station nextStation = currentLink.getTo();
            if(currentLink.canProceedToNextStation(this) && traveledTime >= currentLink.getDISTANCE()){
                currentLink.unregisterFirst();
                currentStation = nextStation;
                state = State.ON_STOP;
                currentStation.register(this);
                schedule.arrived();
                log.info("Vehicle {} arrived at station {} planned: {} actual {}", name, nextStation.getName(), schedule.getCurrentJourney().getTimetable().get(0).get(nextStation), time.getCurrentTime());
                if(direction == 0 && line.isAtEndA(currentStation)){
                    finishedTrip();
                }else if (direction == 1 && line.isAtEndB(currentStation)){
                   finishedTrip();
                }
            }
            traveledTime++;

        }else {
            if(currentStation == null){
                currentStation = schedule.getJourneyList().get(0).getFrom();
            }
            if(schedule.canAttendJourney(time, currentStation)){
                this.state = State.ON_STOP;
            }
        }
    }

    private void finishedTrip(){
        log.info("Vehicle {} finished trip at station {} with deviation {}", name, currentStation.getName(), deviation);
        this.state = State.PAUSE;
        schedule.finishedJourney();
        currentStation.unregister(this);
        direction = direction == 0 ? 1 : 0;
    }
}
