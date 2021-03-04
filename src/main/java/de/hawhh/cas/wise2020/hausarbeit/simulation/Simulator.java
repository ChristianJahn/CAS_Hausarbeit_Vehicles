package de.hawhh.cas.wise2020.hausarbeit.simulation;

import de.hawhh.cas.wise2020.hausarbeit.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Simulator {

    private List<Vehicle> vehicles;

    private Network network;

    private int runtime;

    private Time time;


    public void run(){
        Station stationA =  Station.builder().name("1").capacity(0).waitingTimeInSeconds(6).vehiclesInStation(new ArrayList<>()).build();
        Station stationB =  Station.builder().name("2").capacity(1).waitingTimeInSeconds(6).vehiclesInStation(new ArrayList<>()).build();
        Station stationC = Station.builder().name("3").capacity(1).waitingTimeInSeconds(60).vehiclesInStation(new ArrayList<>()).build();
        network = Network.builder().stations(List.of(
               stationA,
                stationB,
                stationC
                )
        ).links(
                List.of(
                        Link.builder().from(stationA).to(stationB).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationB).to(stationC).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationC).to(stationB).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationB).to(stationA).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build()
                )
        ).build();
        Line lineA = Line.builder().linksDirectionA(List.of(
                Link.builder().from(stationA).to(stationB).DISTANCE(300).vehicles(new ArrayList<>()).CAPACITY(1).build(),
                Link.builder().from(stationB).to(stationC).DISTANCE(300).vehicles(new ArrayList<>()).CAPACITY(1).build())).linksDirectionB(
                        List.of(Link.builder().from(stationC).to(stationB).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                                Link.builder().from(stationB).to(stationA).DISTANCE(300).CAPACITY(1).vehicles(new ArrayList<>()).build())
        ).start(stationA).end(stationC)
                .name("LineA").build();
        network.calculateNetworkTopologyInformation();
        runtime = 50000;
        time = Time.builder().currentTime(LocalDateTime.now()).build();
        Schedule schedule = Schedule.builder().build();
        schedule.generateSchedule(lineA, 0, 5, time.getCurrentTime());
        Schedule schedule2 = Schedule.builder().build();
        schedule2.generateSchedule(lineA, 200, 5, time.getCurrentTime().plusMinutes(1));
        Schedule schedule3 = Schedule.builder().build();
        schedule3.generateSchedule(lineA, 200, 5, time.getCurrentTime().plusMinutes(2));
        Schedule schedule4 = Schedule.builder().build();
        schedule4.generateSchedule(lineA, 200, 5, time.getCurrentTime().plusMinutes(3));
        Schedule schedule5 = Schedule.builder().build();
        schedule5.generateSchedule(lineA, 200, 5, time.getCurrentTime().plusMinutes(4));
        Schedule schedule6 = Schedule.builder().build();
        schedule6.generateSchedule(lineA, 200, 5, time.getCurrentTime().plusMinutes(5));
        lineA.setShedules(List.of(schedule, schedule2, schedule3, schedule4, schedule5, schedule6));
        time = Time.builder().build();
        vehicles = List.of(
                Vehicle.builder().name("FZA").time(time).line(lineA).state(State.PAUSE).schedule(schedule).build(),
                Vehicle.builder().name("FZB").time(time).line(lineA).state(State.PAUSE).schedule(schedule2).build(),
                Vehicle.builder().name("FZC").time(time).line(lineA).state(State.PAUSE).schedule(schedule3).build(),
                Vehicle.builder().name("FZD").time(time).line(lineA).state(State.PAUSE).schedule(schedule4).build()
        );
        time.setCurrentTime(LocalDateTime.now());
        for(int i = 0; i < runtime; i++){
            time.tick();
            for(Vehicle vehicle : vehicles){
                vehicle.doNextAction();
            }
        }
    }

    public static void main(String[] args) {
        new Simulator().run();
    }
}
