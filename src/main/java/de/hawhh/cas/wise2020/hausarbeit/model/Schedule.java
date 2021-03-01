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
                direction = 1;
            }else {
                for(Link link : route.getLinksDirectionB()){
                    time = generateSchedule(time, journey, link);
                }
                direction = 0;
            }
            journey.setDirection(direction);
            journey.setFrom(route.getStart());
            journey.setTo(route.getEnd());
            this.journeyList.add(journey);
        }
        currentJourney = this.journeyList.get(currentJourneyPointer++);
    }

    private LocalDateTime generateSchedule(LocalDateTime time, Journey journey, Link link) {
        Station from = link.getFrom();
        Station to = link.getTo();
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
       LocalDateTime departure = currentJourney.getTimetable().get(0).get(currentStation);
       return departure.isEqual(currentTime) || departure.isBefore(currentTime);
    }

    public int calcDeviation(Station currentStation, Time time) {
        log.info("Deviation {} and planned {}", time.getCurrentTime(), this.currentJourney.getTimetable().get(0).get(currentStation));
        return (int) ChronoUnit.SECONDS.between( this.currentJourney.getTimetable().get(0).get(currentStation), time.getCurrentTime());
    }


    public void arrived() {
        this.currentJourney.getTimetable().remove(0);
    }

    public boolean canAttendJourney(Time time, Station station) {
        LocalDateTime departure = currentJourney.getTimetable().get(0).get(station);
        return ChronoUnit.SECONDS.between(departure, time.getCurrentTime()) <= 1 || departure.isBefore(time.getCurrentTime());
    }

    public void finishedJourney() {
        this.journeyList.remove(0);
        this.currentJourney = journeyList.get(0);
    }
}
