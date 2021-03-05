package de.hawhh.cas.wise2020.hausarbeit.model;

import de.hawhh.cas.wise2020.hausarbeit.simulation.Time;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Slf4j
public class Schedule {

    private int id;

    private List<Journey> journeyList;

    private Journey currentJourney;

    private int currentJourneyPointer = 0;

    public void generateSchedule(Line route, int pauseTime, int journeys, LocalDateTime startTime){
        LocalDateTime time = startTime;
        journeyList = new ArrayList<>();
        int direction = 0;
        for(int i = 0; i < journeys; i++){
            if(!journeyList.isEmpty()) time = time.plusSeconds(pauseTime);
            Journey journey = Journey.builder().timetable(new ArrayList<>()).build();
            if(direction == 0){
                for(Link link : route.getLinksDirectionA()){
                    time = generateSchedule(time, journey, link);
                }
                journey.setFrom(route.getLinksDirectionA().get(0).getFrom());
                journey.setTo(route.getLinksDirectionA().get(route.getLinksDirectionA().size() - 1).getTo());
                direction = 1;
            }else {
                for(Link link : route.getLinksDirectionB()){
                    time = generateSchedule(time, journey, link);
                }
                journey.setFrom(route.getLinksDirectionB().get(0).getFrom());
                journey.setTo(route.getLinksDirectionB().get(route.getLinksDirectionB().size() - 1).getTo());
                direction = 0;
            }
            journey.setDirection(direction);
            this.journeyList.add(journey);
        }
        currentJourney = this.journeyList.get(currentJourneyPointer++);
    }

    private LocalDateTime generateSchedule(LocalDateTime time, Journey journey, Link link) {
        Station from = link.getFrom();
        Station to = link.getTo();
        time = time.plusSeconds(to.getWaitingTimeInSeconds());
        Map<Station, LocalDateTime> departure = new HashMap<>();
        departure.put( from, time);
        Map<Station, LocalDateTime> arrvival = new HashMap<>();
        time = time.plusSeconds(link.getDISTANCE());
        arrvival.put(to, time);
        journey.getTimetable().add(departure);
        journey.getTimetable().add(arrvival);
        return time;
    }

    public boolean canProceedFromStop(LocalDateTime currentTime, Station currentStation) {
       LocalDateTime departure = currentJourney.getNextTimetableTime().get(currentStation);
       return departure.isEqual(currentTime) || departure.isBefore(currentTime);
    }



    public int calcDeviation(Station currentStation, Time time, Vehicle vehicle) {
        log.info("Deviation {} and planned {}", time.getCurrentTime(), this.currentJourney.getNextTimetableTime().get(currentStation));
        int deviation = (int) ChronoUnit.SECONDS.between(  this.currentJourney.getNextTimetableTime().get(currentStation),time.getCurrentTime());
        if(deviation - vehicle.getDeviation() > currentStation.getMaxDeviationAdded()){
            currentStation.setMaxDeviationAdded(deviation - vehicle.getDeviation() );
        }
        return deviation;
    }


    public void arrived() {
        this.currentJourney.arrived();
    }

    public boolean canAttendJourney(Time time, Station station) {
        LocalDateTime departure = currentJourney.getTimetable().get(0).get(station);
        return ChronoUnit.SECONDS.between(departure, time.getCurrentTime()) <= 1 || departure.isBefore(time.getCurrentTime());
    }

    public void finishedJourney() {
        this.currentJourney = journeyList.get(currentJourneyPointer++);
    }

    public Station getNextStation() {
        return getCurrentJourney().getNextTimetableTime().keySet().stream().findAny().orElse(null);
    }

//    public LocalDateTime getEstimatedDeparture() {
//        return
//    }

    public int getPlannedWaitingTime(LocalDateTime currentTime, Station currentStation) {
        LocalDateTime departure = currentJourney.getNextTimetableTime().get(currentStation);
        return (int) ChronoUnit.SECONDS.between(currentTime,  departure);
    }

    public void departed() {
        this.currentJourney.departed();
    }
}
