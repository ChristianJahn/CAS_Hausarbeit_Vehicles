package de.hawhh.cas.wise2020.hausarbeit.simulation;

import de.hawhh.cas.wise2020.hausarbeit.model.Network;
import de.hawhh.cas.wise2020.hausarbeit.model.Vehicle;

import java.time.LocalDateTime;
import java.util.List;

public class Simulator {

    private List<Vehicle> vehicles;

    private Network network;

    private int runtime;

    private Time time;


    public void run(){
        time = Time.builder().build();
        time.setCurrentTime(LocalDateTime.now());
        for(int i = 0; i < runtime; i++){
            time.tick();
            for(Vehicle vehicle : vehicles){
                vehicle.doNextAction();
            }
        }
    }
}
