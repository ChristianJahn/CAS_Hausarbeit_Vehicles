package de.hawhh.cas.wise2020.hausarbeit.simulation;

import de.hawhh.cas.wise2020.hausarbeit.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
                e.printStackTrace();
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
//        for(Vehicle vehicle : vehicles){
//            if(vehicle.getName().equals("FZ_1_LS3")) {
//                System.out.println(vehicle.getDelayOverTime().keySet());
//                System.out.println("Delay of vehicle (times) " + vehicle + " is " + vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1).collect(Collectors.toList()));
//                System.out.println("Delay of vehicle " + vehicle + " is " + vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1).map(l -> vehicle.getDelayOverTime().get(l)).collect(Collectors.toList()));
//                System.out.println("Delay of vehicle (min)" + vehicle + " is " + vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1).map(l -> vehicle.getDelayOverTime().get(l)).sorted(Comparator.naturalOrder()).collect(Collectors.toList()).get(0));
//                System.out.println("Delay of vehicle (total)" + vehicle + " is " + vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1)
//                        .map(l -> vehicle.getDelayOverTime().get(l) ).filter(s -> s != 0)
//                        .sorted(Comparator.naturalOrder()).mapToInt(i -> i).sum());
//                System.out.println("Delay of vehicle (avg)" + vehicle + " is " + vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1)
//                        .map(l -> vehicle.getDelayOverTime().get(l)).filter(s -> s != 0)
//                        .sorted(Comparator.naturalOrder()).mapToInt(i -> i).sum() /  vehicle.getDelayOverTime().keySet().stream().sorted((l,l2) -> l.isBefore(l2) ? -1 : 1)
//                        .map(l -> vehicle.getDelayOverTime().get(l)).filter(s -> s != 0)
//                        .sorted(Comparator.naturalOrder()).collect(Collectors.toList()).size() * 1.0);
//
//            }
//        }
        exportStatistics(configReader, runtime, network, vehicles);
    }

    private void exportStatistics(ConfigReader configReader, int runtime, Network network, List<Vehicle> vehicles) {
        try {
            for (Vehicle vehicle : vehicles) {
                File myObj = new File("./results/delayCourveOfVehicle_"+vehicle.getName()+"_from_" + new Date() + ".csv");
                myObj.createNewFile();
                PrintWriter writer = new PrintWriter(myObj);
                List<LocalDateTime> sortedDates = vehicle.getDelayOverTime().keySet().stream().sorted().collect(Collectors.toList());
                int time = 0;
                for(LocalDateTime date : sortedDates){
                    writer.write(time+";"+vehicle.getDelayOverTime().get(date)+";"+vehicle.getSkippedStop().get(date)+"\n");
                    time +=60;
                }
                writer.close();
            }
           for(Line line : configReader.getLines()){
               File myObj = new File("./results/waitingTimeByStation_Line_"+line.getName()+"_from_" + new Date() + ".csv");
               myObj.createNewFile();
               Map<String, Long> avgWaitingTimeByStation = new HashMap<>();
               for(Link link : line.getLinksDirectionA()){
                   Station from = link.getFrom();
                   Long sum = 0L;
                   int numberOf = 0;
                   for(Map<String, Long> waitingTime : from.getWaitingTimeHistory().values()){
                       if(waitingTime.containsKey(line.getName())) {
                           sum += waitingTime.getOrDefault(line.getName(), 0L);
                           numberOf++;
                       }
                   }
                   if(numberOf > 0) {
                       avgWaitingTimeByStation.putIfAbsent(from.getName(), sum / numberOf);
                   }
               }
               PrintWriter writer = new PrintWriter(myObj);
               for(String station : avgWaitingTimeByStation.keySet()){
                   writer.write(station+";"+avgWaitingTimeByStation.get(station)+"\n");
               }
               writer.close();
           }

               File myObj = new File("./results/avgDelayByLine_from_" + new Date() + ".csv");
               myObj.createNewFile();
                PrintWriter writer = new PrintWriter(myObj);
               Map<String, Long> avgDelayByLine = new HashMap<>();
               Map<String, Integer> vehiclesByLine = new HashMap<>();
                for(Vehicle vehicle : vehicles){
                    long avg = vehicle.getDelayOverTime().values().stream().mapToInt(i -> i).filter(i -> i < 1500).sum() / vehicle.getDelayOverTime().values().stream().filter(i -> i < 1500).count();
                    avgDelayByLine.putIfAbsent(vehicle.getLine().getName(), 0L);
                    avgDelayByLine.put(vehicle.getLine().getName(), avgDelayByLine.get(vehicle.getLine().getName()) + avg);
                    vehiclesByLine.putIfAbsent(vehicle.getLine().getName(), 0);
                    vehiclesByLine.put(vehicle.getLine().getName(), vehiclesByLine.get(vehicle.getLine().getName()) + 1);
                }
                for(String line: avgDelayByLine.keySet()){
                    writer.write(line+";"+avgDelayByLine.get(line) / vehiclesByLine.get(line)+"\n");
                }
                writer.close();
        }catch (Exception e ){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Simulator().run();
    }
}
