package de.hawhh.cas.wise2020.hausarbeit.simulation;

import de.hawhh.cas.wise2020.hausarbeit.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulator {

    private List<Vehicle> vehicles;

    private Network network;

    private int runtime;

    private Time time;


    public void run(){
        ConfigReader configReader = new ConfigReader();
        configReader.readConfigFromFile();
        runtime = configReader.getConfig().getRuntimeHours() * 60 * 60;
        time = configReader.getTimeObj();
        network = configReader.getNetwork();
        vehicles = configReader.getVehicles();
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
            long smallestwaitingtime = Long.MAX_VALUE;
            long biggestWaitingTime = Long.MIN_VALUE;
            for( Map<String, Long> values : station.getWaitingTimeHistory().values()){
                long sum = values.values().stream().mapToLong(s -> s).sum();
                if(sum > biggestWaitingTime) biggestWaitingTime = sum;
                if(sum < smallestwaitingtime) smallestwaitingtime = sum;
            }
            System.out.println("Station "+station.getName()+" Smallest: "+smallestwaitingtime+" biggest: "+biggestWaitingTime+ " avg " );
            System.out.println("Station "+station.getName()+" maxAddedDeviat: "+station.getMaxDeviationAdded()+" biggest: "+biggestWaitingTime);
        }
    }

    public static void main(String[] args) {
        new Simulator().run();
    }
}
