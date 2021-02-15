package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Schedule {

    private List<Journey> journeyList;

    private Journey currentJourney;

    public void generateSchedule(Line route, int pauseTime, int journeys, LocalDateTime startTime){
        LocalDateTime time = startTime;
        int direction = 0;
        for(int i = 0; i < journeys; i++){
            if(!journeyList.isEmpty()) time = time.plusSeconds(pauseTime);
            Journey journey = Journey.builder().build();
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
        }
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
}
