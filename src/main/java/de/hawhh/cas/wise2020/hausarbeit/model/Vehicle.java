package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Data
@Builder
@Slf4j
@EqualsAndHashCode(of = {"name"})
@ToString(of = {"name", "deviation"})
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

    private LocalDateTime currentStationArrivalTime;

    private int direction = 0;

    private int traveledTime = 0;

    private int dwellTime = 60;

    private int DEVIATION_THRESHOLD = 300; // 5 minutes is default

    private int remainingWaitingTime = 0;

    private int holdingTime = 0;

    private int optimalHeadway;

    public void init(){

    }


    public void doNextAction() {
        //decideOnStrategy();
        if(holdingTime > 0) holdingTime--;
        if(state.equals(State.ON_STOP)){ // is on a station
            remainingWaitingTime--;
            if(remainingWaitingTime <= 0) { // check if the time has come
                Link nextLink = direction == 0 ? line.getNextLinkA(currentStation) : line.getNextLinkB(currentStation);
                if (nextLink.hasCapacity() && holdingTime <= 0) {
                    dwellTime = 0;
                    this.deviation = schedule.calcDeviation(currentStation, time);
                    currentStation.unregister(this);
                    nextLink.goToLink(this);
                    log.info("Vehicle {} departs from stop (allowed) {} on link from {} to {} with capacity {} and has deviation {} (planned {} actual {})", this.name,
                            currentStation.getName(),
                            nextLink.getFrom().getName(), nextLink.getTo().getName(), nextLink.getCAPACITY() - nextLink.getVehicles().size(), deviation, schedule.getCurrentJourney().getNextTimetableTime().get(currentStation), time.getCurrentTime());
                    currentLink = nextLink;
                    schedule.departed();
                    state = State.TRAVELING;
                }else if(!nextLink.hasCapacity() ) {
                    log.info("Vehicle {} cannot depart due to missing capacity");
                }
            }
        }else if(state.equals(State.TRAVELING)){ // Is on a trip
            //TODO check if travel time has passed
            Station nextStation = currentLink.getTo();
            if(currentLink.canProceedToNextStation(this) && traveledTime >= currentLink.getDISTANCE()){
                currentLink.unregisterFirst();
                currentStation = nextStation;
                state = State.ON_STOP;
                currentStationArrivalTime = time.getCurrentTime();
                this.dwellTime = currentStation.getWaitingTimeInSeconds() ;
                if(new Random().nextBoolean()){
                    dwellTime *= 1.2;
                }
                currentStation.register(this);
                schedule.arrived();
                if(direction == 0 && line.isAtEndA(currentStation)){
                    finishedTrip();
                }else if (direction == 1 && line.isAtEndB(currentStation)){
                   finishedTrip();
                }
                if(!state.equals(State.PAUSE)) {
                    log.info("Vehicle {} arrived at station {} planned: {} actual {}", name, nextStation.getName(), schedule.getCurrentJourney().getNextTimetableTime().get(nextStation), time.getCurrentTime());
                    remainingWaitingTime = schedule.getPlannedWaitingTime(time.getCurrentTime(), currentStation);
                    if(remainingWaitingTime < dwellTime){
                        remainingWaitingTime = dwellTime;
                    }
                }
            }
            traveledTime++;
        }else {
            if(currentStation == null){
                currentStation = schedule.getJourneyList().get(0).getFrom();
            }
            if(schedule.canAttendJourney(time, currentStation)){
                this.state = State.ON_STOP;
                if(direction == 0){
                    this.line.getVehiclesOnLineA().add(this);
                }else {
                    this.line.getVehiclesOnLineB().add(this);
                }
                remainingWaitingTime = schedule.getPlannedWaitingTime(time.getCurrentTime(), currentStation);
                this.dwellTime = currentStation.getWaitingTimeInSeconds();
                if(remainingWaitingTime < dwellTime){
                    remainingWaitingTime = dwellTime;
                }
            }
        }
    }

    private Strategy decideOnStrategy() {
        Strategy strategy = null;
        if(deviation > 60){
            //Holding is no option
            // Skip stop comes into concideration
        }else if(state.equals(State.ON_STOP) && !isAtEnd(direction, currentStation) && !isAtStart(direction, currentStation)){
            // hold tactic only makes sense on stops
            // On last stop it would only block traffic
            strategy = Strategy.HOLD_TACT;
           Long headwayHoldingTime = calculateHeadwayHoldingTime();
           Long succeededHoldingTime = calculateSucceededHoldingTime();
           if(headwayHoldingTime != null && succeededHoldingTime != null) {
               Long timeToProceed = Math.max(Math.min(headwayHoldingTime, succeededHoldingTime), currentStationArrivalTime.plusSeconds(dwellTime).toEpochSecond(ZoneOffset.UTC));
               holdingTime = (int) ChronoUnit.SECONDS.between(time.getCurrentTime(),  LocalDateTime.ofEpochSecond(timeToProceed, 0, ZoneOffset.UTC));
           }
        }
        return strategy;
    }

    private boolean isAtStart(int direction, Station currentStation) {
        if(direction == 0){
            return line.isAtEndB(currentStation);
        }else {
            return line.isAtEndA(currentStation);
        }
    }

    private boolean isAtEnd(int direction, Station currentStation) {
        if(direction == 0){
            return line.isAtEndA(currentStation);
        }else {
            return line.isAtEndB(currentStation);
        }
    }

    private Long calculateSucceededHoldingTime() {
        return 0L;
    }

    private Long calculateHeadwayHoldingTime() {
        Long scheduledArrivalTimeNextVehicle = line.getNextSheduledArrival(currentStation, schedule, this);
        return scheduledArrivalTimeNextVehicle;
    }

    private void finishedTrip(){
        log.info("Vehicle {} finished trip at station {} with deviation {}", name, currentStation.getName(), deviation);
        this.state = State.PAUSE;
        schedule.finishedJourney();
        currentStation.unregister(this);
        if(direction == 0){
            line.getVehiclesOnLineA().remove(this);
        }else {
            line.getVehiclesOnLineB().remove(this);
        }
        direction = direction == 0 ? 1 : 0;
    }
}
