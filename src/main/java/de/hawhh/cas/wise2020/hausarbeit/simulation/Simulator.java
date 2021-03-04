package de.hawhh.cas.wise2020.hausarbeit.simulation;

import de.hawhh.cas.wise2020.hausarbeit.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Simulator {

    private List<Vehicle> vehicles;

    private Network network;

    private int runtime;

    private Time time;


    public void run(){
        time = Time.builder().build();
        time.setCurrentTime(LocalDateTime.now());
        time = Time.builder().currentTime(LocalDateTime.now()).build();
        Station stationA =  Station.builder().time(time).name("1").capacity(6).waitingTimeInSeconds(300).waitingTimeByLine(new HashMap<>()).waitingTimeHistory(new HashMap<>()).vehiclesInStation(new ArrayList<>()).build();
        Station stationB =  Station.builder().time(time).name("2").capacity(2).waitingTimeInSeconds(300).waitingTimeByLine(new HashMap<>()).waitingTimeHistory(new HashMap<>()).vehiclesInStation(new ArrayList<>()).build();
        Station stationC = Station.builder().time(time).name("3").capacity(6).waitingTimeInSeconds(300).waitingTimeByLine(new HashMap<>()).waitingTimeHistory(new HashMap<>()).vehiclesInStation(new ArrayList<>()).build();
        network = Network.builder().stations(List.of(
               stationA,
                stationB,
                stationC
                )
        ).links(
                List.of(
                        Link.builder().from(stationA).to(stationB).DISTANCE(30).CAPACITY(6).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationB).to(stationC).DISTANCE(30).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationC).to(stationB).DISTANCE(30).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                        Link.builder().from(stationB).to(stationA).DISTANCE(30).CAPACITY(6).vehicles(new ArrayList<>()).build()
                )
        ).build();
        Line lineA = Line.builder().linksDirectionA(List.of(
                Link.builder().from(stationA).to(stationB).DISTANCE(30).vehicles(new ArrayList<>()).CAPACITY(6).build(),
                Link.builder().from(stationB).to(stationC).DISTANCE(30).vehicles(new ArrayList<>()).CAPACITY(6).build())).linksDirectionB(
                        List.of(Link.builder().from(stationC).to(stationB).DISTANCE(30).CAPACITY(1).vehicles(new ArrayList<>()).build(),
                                Link.builder().from(stationB).to(stationA).DISTANCE(30).CAPACITY(1).vehicles(new ArrayList<>()).build())
        ).time(time).vehiclesOnLineA(new ArrayList<>()).vehiclesOnLineB(new ArrayList<>()).start(stationA).end(stationC)
                .name("LineA").build();
        network.calculateNetworkTopologyInformation();
        runtime = 5000000;
        Schedule schedule = Schedule.builder().build();
        schedule.generateSchedule(lineA, 0, 10, time.getCurrentTime());
        Schedule schedule2 = Schedule.builder().build();
        schedule2.generateSchedule(lineA, 2, 10, time.getCurrentTime().plusMinutes(10));
        Schedule schedule3 = Schedule.builder().build();
        schedule3.generateSchedule(lineA, 2, 10, time.getCurrentTime().plusMinutes(25));
        Schedule schedule4 = Schedule.builder().build();
        schedule4.generateSchedule(lineA, 2, 10, time.getCurrentTime().plusMinutes(40));
        Schedule schedule5 = Schedule.builder().build();
        schedule5.generateSchedule(lineA, 2, 10, time.getCurrentTime().plusMinutes(45));
        Schedule schedule6 = Schedule.builder().build();
        schedule6.generateSchedule(lineA, 2, 10, time.getCurrentTime().plusMinutes(60));
        lineA.setShedules(List.of(schedule, schedule2, schedule3, schedule4, schedule5, schedule6));
        vehicles = List.of(
                Vehicle.builder().name("FZA").time(time).line(lineA).state(State.PAUSE).schedule(schedule).build(),
                Vehicle.builder().name("FZB").time(time).line(lineA).state(State.PAUSE).schedule(schedule2).build(),
                Vehicle.builder().name("FZC").time(time).line(lineA).state(State.PAUSE).schedule(schedule3).build(),
                Vehicle.builder().name("FZD").time(time).line(lineA).state(State.PAUSE).schedule(schedule4).build()
        );
        for(int i = 0; i < runtime; i++){
            time.tick();
            try {
                for (Vehicle vehicle : vehicles) {
                    vehicle.doNextAction();
                }
                for (Station station : network.getStations()) {
                    station.tick();
                }
            }catch (Exception e){
                i = runtime;
            }
        }
        for (Station station : network.getStations()){
            System.out.println(station.getWaitingTimeHistory());
        }
    }

    public static void main(String[] args) {
        new Simulator().run();
    }
}
