package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    private boolean cooperative;

    private int position;

    private Time time;

    private int deviation;

    private Schedule schedule;

    private int plannedHeadway;

    private Map<LocalDateTime, Integer> delayOverTime;


    private LocalDateTime currentStationArrivalTime;

    private Map<LocalDateTime, Integer> skippedStop;

    private boolean skipNextStop = false;

    private int lastStationSkipped;

    private int direction = 0;

    private boolean cooperativeStrategy;

    private int traveledTime = 0;

    private int dwellTime = 60;

    private int DEVIATION_THRESHOLD = 60; // 1 minute is default

    private int remainingWaitingTime = 0;

    private int holdingTime = 0;

    private int lastTimeStatistics;

    private int travelDistanceRemaining = 0;

    private int optimalHeadway;

    public void init(){

    }


    public void doNextAction() {
       decideOnStrategy();
        if(direction == 0){
            this.line.getVehiclesOnLineA().sort((v1, v2) -> Integer.compare(v2.getPosition(), v1.getPosition()));
        }else {
            this.line.getVehiclesOnLineB().sort((v1, v2) -> Integer.compare(v2.getPosition(), v1.getPosition()));
        }
        if(delayOverTime == null) delayOverTime = new HashMap<>();
        if(skippedStop == null) this.skippedStop = new HashMap<>();
        if(lastTimeStatistics % 60 == 0){
            delayOverTime.put(time.getCurrentTime(), deviation);
            skippedStop.put(time.getCurrentTime(), 0);
        }
        lastTimeStatistics++;
        if(holdingTime > 0) holdingTime--;
        if(state.equals(State.ON_STOP)){ // is on a station
            this.deviation = schedule.calcDeviation(currentStation, time, this);
            remainingWaitingTime--;
            if(remainingWaitingTime <= 0) { // check if the time has come
                Link nextLink = direction == 0 ? line.getNextLinkA(currentStation) : line.getNextLinkB(currentStation);
                if (nextLink.hasCapacity() && holdingTime <= 0 && hasHighestDemand(nextLink)) {
                    dwellTime = 0;
                    this.deviation = schedule.calcDeviation(currentStation, time, this);
                    if(skipNextStop){
                        skipNextStop = false;
                        skippedStop.put(time.getCurrentTime(), 100);
                    }
                    currentStation.unregister(this);
                    position++;
                    nextLink.goToLink(this);
                    log.info("Vehicle {} departs from stop (allowed) {} on link from {} to {} with capacity {} and has deviation {} (planned {} actual {})", this.name,
                            currentStation.getName(),
                            nextLink.getFrom().getName(), nextLink.getTo().getName(), nextLink.getCAPACITY() - nextLink.getVehicles().size(), deviation, schedule.getCurrentJourney().getNextTimetableTime().get(currentStation), time.getCurrentTime());
                    currentLink = nextLink;
                    schedule.departed();
                    travelDistanceRemaining = currentLink.getDISTANCE();
                    if(new Random().nextBoolean()){
                        travelDistanceRemaining *= 3;
                    }
                    state = State.TRAVELING;
                }
            }
        }else if(state.equals(State.TRAVELING)){ // Is on a trip
            Station nextStation = currentLink.getTo();
            this.deviation = schedule.calcDeviation(nextStation, time, this) + (travelDistanceRemaining - traveledTime);
            if(currentLink.canProceedToNextStation(this) && traveledTime >= travelDistanceRemaining && !skipNextStop){
                currentLink.unregisterFirst();
                currentStation = nextStation;
                state = State.ON_STOP;
                position++;
                currentStationArrivalTime = time.getCurrentTime();
                this.dwellTime = currentStation.getWaitingTimeInSeconds() ;
                lastStationSkipped++;
                this.deviation = schedule.calcDeviation(currentStation, time, this);
                if(new Random().nextBoolean()){
                    dwellTime *= 3;
                }else {
                    dwellTime *= 0.8;
                }
                currentStation.register(this);
                schedule.arrived();
                if(direction == 0 && line.isAtEndA(currentStation)){
                    finishedTrip();
                }else if (direction == 1 && line.isAtEndB(currentStation)){
                   finishedTrip();
                }
                if(!state.equals(State.PAUSE)) {
                    log.info("Vehicle {} arrived at station {} planned: {} actual {} deviation {}", name, nextStation.getName(), schedule.getCurrentJourney().getNextTimetableTime().get(nextStation), time.getCurrentTime(), deviation);
                    remainingWaitingTime = schedule.getPlannedWaitingTime(time.getCurrentTime(), currentStation);
                    if(remainingWaitingTime < dwellTime){
                        remainingWaitingTime = dwellTime;
                    }
                }
            }else if(currentLink.canProceedToNextStation(this) && traveledTime >= travelDistanceRemaining && skipNextStop && !(direction == 0 && line.isAtEndA(currentLink.getTo()) || direction == 1 && line.isAtEndB(currentLink.getTo()))){
                currentLink.unregisterFirst();
                currentStation = nextStation;
                state = State.ON_STOP;
                position++;
                currentStationArrivalTime = time.getCurrentTime();
                this.deviation = schedule.calcDeviation(currentStation, time, this);
                this.dwellTime = 0;
                skippedStop.put(time.getCurrentTime(), 100);
                schedule.arrived();
                if(!state.equals(State.PAUSE)) {
                    log.info("Vehicle {} arrived at station {} planned: {} actual {}", name, nextStation.getName(), schedule.getCurrentJourney().getNextTimetableTime().get(nextStation), time.getCurrentTime());
                    remainingWaitingTime = 3;
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
                    this.line.getVehiclesOnLineA().sort((v1, v2) -> Integer.compare(v2.getPosition(), v1.getPosition()));
                }else {
                    this.line.getVehiclesOnLineB().add(this);
                    this.line.getVehiclesOnLineB().sort((v1, v2) -> Integer.compare(v2.getPosition(), v1.getPosition()));
                }
                currentStation.register(this);
                this.schedule.setLastArrival(new HashMap<>());
                this.schedule.getLastArrival().put(currentStation, time.getCurrentTime());
                this.deviation = schedule.calcDeviation(currentStation, time, this);
                remainingWaitingTime = schedule.getPlannedWaitingTime(time.getCurrentTime(), currentStation);
                this.dwellTime = currentStation.getWaitingTimeInSeconds();
                if(remainingWaitingTime < dwellTime){
                    remainingWaitingTime = dwellTime;
                }
            }
        }
    }

    private boolean hasHighestDemand(Link nextLink) {
        if(!this.isCooperative() || (nextLink.getCAPACITY() - nextLink.getVehicles().size()) >= currentStation.getVehiclesInStation().size()) return true;
        for(Vehicle vehicle : currentStation.getVehiclesInStation()){
            if(vehicle.getDeviation()  > this.deviation){
                return false;
            }
        }
        return true;
    }

    private Strategy decideOnStrategy() {
        Strategy strategy = null;
        if(deviation > getAverageDelay() && !skipNextStop && deviation > DEVIATION_THRESHOLD){
            //Holding i5no option
            // Skip stop comes into concideration
            if(state.equals(State.TRAVELING)) {
                skipNextStop = true;
                lastStationSkipped = 0;
                log.info("Vehicle {} (delayed by ) skips next stop {} with waiting time there {} ",name, currentLink.getTo().getName(),  currentLink.getTo().getWaitingTimeByLine().getOrDefault(line.getName(), 0L));
            }else {
                skipNextStop = false;
            }
        }else if(state.equals(State.ON_STOP) && !isAtEnd(direction, currentStation) && !isAtStart(direction, currentStation) && !skipNextStop && line.getVehiclesOnLineA().size() > 2
        && (line.getVehiclesOnLineA().size() == 0 || !line.getVehiclesOnLineA().get(0).equals(this))){
//            // hold tactic only makes sense on stops
//            // On last stop it would only block traffic
            strategy = Strategy.HOLD_TACT;
           Long headwayHoldingTime = calculateTimeOfPreviousVehicle();
           Long succeededHoldingTime = calculateSucceededHoldingTime();
           Long SRT = 0L;
           if(direction == 0) {
               SRT = (long) line.getNextLinkA(currentStation).getDISTANCE();
           }else {
               SRT = (long) line.getNextLinkB(currentStation).getDISTANCE();
           }
           long ET = (long) Math.max(Math.min(succeededHoldingTime + (headwayHoldingTime + SRT - succeededHoldingTime)/2, succeededHoldingTime + 0.85 * plannedHeadway), schedule.getLastArrival().get(currentStation).plusSeconds(dwellTime).toEpochSecond(ZoneOffset.UTC));
           LocalDateTime dateTime = LocalDateTime.ofEpochSecond(ET, 0, ZoneOffset.UTC);
               remainingWaitingTime = (int) ChronoUnit.SECONDS.between(time.getCurrentTime(), dateTime);
        }
        return strategy;
    }

    private double getAverageDelay() {
        if(direction == 0){
            return this.line.getVehiclesOnLineA().stream().mapToInt(Vehicle::getDeviation).sum() / (this.line.getVehiclesOnLineA().size() * 1.0);
        }else {
            return this.line.getVehiclesOnLineB().stream().mapToInt(Vehicle::getDeviation).sum() / (this.line.getVehiclesOnLineB().size() * 1.0);
        }
    }

    private Long calculateTimeOfPreviousVehicle(){
        Vehicle beforeVehicle;
        if(direction == 0){
            int indexOfThisVehicle = line.getVehiclesOnLineA().indexOf(this);
            if(indexOfThisVehicle  < line.getVehiclesOnLineA().size() - 2 && indexOfThisVehicle != -1){
                beforeVehicle = line.getVehiclesOnLineA().get(indexOfThisVehicle + 1);
                LocalDateTime arrivalTimeAtThisStation = calculateEstimatedArrivalTimeAtStation(beforeVehicle);
                return arrivalTimeAtThisStation.toEpochSecond(ZoneOffset.UTC);
            }else {
                return 0L;
            }
        }else {
            int indexOfThisVehicle = line.getVehiclesOnLineB().indexOf(this);
            if(indexOfThisVehicle  < line.getVehiclesOnLineB().size() - 2 && indexOfThisVehicle != -1){
                beforeVehicle = line.getVehiclesOnLineB().get(indexOfThisVehicle + 1);
                LocalDateTime arrivalTimeAtThisStation = calculateEstimatedArrivalTimeAtStation(beforeVehicle);
                return arrivalTimeAtThisStation.toEpochSecond(ZoneOffset.UTC);
            }else {
                return 0L;
            }
        }

    }

    private LocalDateTime calculateEstimatedArrivalTimeAtStation(Vehicle beforeVehicle) {
        LocalDateTime arrivalTime = time.getCurrentTime();
        if(beforeVehicle.getState().equals(State.TRAVELING)){
            arrivalTime = time.getCurrentTime().plusSeconds(beforeVehicle.getTravelDistanceRemaining() - beforeVehicle.getTraveledTime());
            int stationPointerPrevVehicle = beforeVehicle.getSchedule().getCurrentJourney().getStationPointer();
            Link actualLink = null;
            Station actualStation = new ArrayList<>(beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(beforeVehicle.getSchedule().getCurrentJourney().getStationPointer()).keySet()).get(0);
            while(!actualStation.equals(currentStation)){
                stationPointerPrevVehicle++;
                LocalDateTime departureTime = beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(stationPointerPrevVehicle).get(actualStation);
                if(ChronoUnit.SECONDS.between(arrivalTime.plusSeconds(actualStation.getWaitingTimeInSeconds()), departureTime) > 0){
                    arrivalTime = departureTime;
                }
                if(direction == 0){
                    actualLink = beforeVehicle.getLine().getNextLinkA(actualStation);
                }else {
                    actualLink = beforeVehicle.getLine().getNextLinkB(actualStation);
                }
                actualStation = new ArrayList<>(beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(++stationPointerPrevVehicle).keySet()).get(0);
                arrivalTime = arrivalTime.plusSeconds(actualLink.getDISTANCE());
            }
            return arrivalTime;
        }else {
            int stationPointerPrevVehicle = beforeVehicle.getSchedule().getCurrentJourney().getStationPointer();
            arrivalTime = beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(stationPointerPrevVehicle).get(beforeVehicle.getCurrentStation());
            Link actualLink = null;
            Station actualStation = new ArrayList<>(beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(beforeVehicle.getSchedule().getCurrentJourney().getStationPointer()).keySet()).get(0);
            while(!actualStation.equals(currentStation)){
                LocalDateTime departureTime = beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(stationPointerPrevVehicle).get(actualStation);
                stationPointerPrevVehicle++;
                if(ChronoUnit.SECONDS.between(arrivalTime.plusSeconds(actualStation.getWaitingTimeInSeconds()), departureTime) > 0){
                    arrivalTime = departureTime;
                }
                if(direction == 0){
                    actualLink = beforeVehicle.getLine().getNextLinkA(actualStation);
                }else {
                    actualLink = beforeVehicle.getLine().getNextLinkB(actualStation);
                }
                actualStation = new ArrayList<>(beforeVehicle.getSchedule().getCurrentJourney().getTimetable().get(stationPointerPrevVehicle).keySet()).get(0);
                arrivalTime = arrivalTime.plusSeconds(actualLink.getDISTANCE());
            }
            return arrivalTime;
        }
    }


    private boolean isAtStart(int direction, Station currentStation) {
        if(direction == 0){
            return line.getLinksDirectionA().get(0).getFrom().equals(currentStation);
        }else {
            return line.getLinksDirectionB().get(0).getFrom().equals(currentStation);
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
        Vehicle beforeVehicle;
        if(direction == 0){
            int indexOfThisVehicle = line.getVehiclesOnLineA().indexOf(this);
            if(indexOfThisVehicle != 0&& indexOfThisVehicle != -1){
                beforeVehicle = line.getVehiclesOnLineA().get(indexOfThisVehicle - 1);
                LocalDateTime arrivalTimeAtThisStation = beforeVehicle.getSchedule().getLastArrival().get(currentStation);
                if(arrivalTimeAtThisStation != null) {
                    return arrivalTimeAtThisStation.toEpochSecond(ZoneOffset.UTC);
                }
            }else {
                return 0L;
            }
        }else {
            int indexOfThisVehicle = line.getVehiclesOnLineB().indexOf(this);
            if(indexOfThisVehicle  != 0 && indexOfThisVehicle != -1){
                beforeVehicle = line.getVehiclesOnLineB().get(indexOfThisVehicle - 1);
                LocalDateTime arrivalTimeAtThisStation = beforeVehicle.getSchedule().getLastArrival().get(currentStation);
                if(arrivalTimeAtThisStation != null) {
                    return arrivalTimeAtThisStation.toEpochSecond(ZoneOffset.UTC);
                }
            }else {
                return 0L;
            }
        }
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
        position = 0;
        currentStation.unregister(this);
        if(direction == 0){
            line.getVehiclesOnLineA().remove(this);
            currentStation = line.getLinksDirectionB().get(0).getFrom();
        }else {
            line.getVehiclesOnLineB().remove(this);
            currentStation = line.getLinksDirectionA().get(0).getFrom();
        }
        direction = direction == 0 ? 1 : 0;
    }
}
