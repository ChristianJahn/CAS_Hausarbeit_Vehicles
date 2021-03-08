package de.hawhh.cas.wise2020.hausarbeit.simulation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hawhh.cas.wise2020.hausarbeit.model.*;
import de.hawhh.cas.wise2020.hausarbeit.model.config_model.Config;
import de.hawhh.cas.wise2020.hausarbeit.model.config_model.LineConfig;
import de.hawhh.cas.wise2020.hausarbeit.model.config_model.VehicleConfig;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ConfigReader {

    private Network network;

    private List<Vehicle> vehicles;

    private Time timeObj;

    private List<Line> lines;

    private Config config;

    public void readConfigFromFile(){
        try {
            FileInputStream inputStream = new FileInputStream(new File(getClass().getResource("/config.json").toURI()));
            Config config = new ObjectMapper().readValue(inputStream, Config.class);
            this.config = config;
            Time time = Time.builder().currentTime(LocalDateTime.now()).build();
            timeObj = time;
            List<Station> stations = config.getStations().stream().map(stationConfig -> {
                return Station.builder()
                        .name(stationConfig.getName())
                        .time(time)
                        .capacity(stationConfig.getCapacity())
                        .waitingTimeInSeconds(stationConfig.getDefaultWaitingTime())
                        .allowTurn(stationConfig.isTurnAllowed())
                        .waitingTimeByLine(new HashMap<>())
                        .waitingTimeHistory(new HashMap<>())
                        .vehiclesInStation(new ArrayList<>()).id(stationConfig.getId()).build();
            }).collect(Collectors.toCollection(ArrayList::new));
            List<Link> links = config.getLinks().stream().map(linkConfig -> {
                return Link.builder()
                        .vehicles(new ArrayList<>())
                        .from(stations.stream().filter(s -> s.getId() == linkConfig.getFrom()).findFirst().orElse(null))
                        .to(stations.stream().filter(s -> s.getId() == linkConfig.getTo()).findFirst().orElse(null))
                        .DISTANCE(linkConfig.getDistance())
                        .id(linkConfig.getId())
                        .CAPACITY(linkConfig.getCapacity())
                        .build();
            }).collect(Collectors.toCollection(ArrayList::new));
            lines = config.getLines().stream().map(lineConfig -> {
                return Line.builder()
                        .name(lineConfig.getName())
                        .time(timeObj)
                        .start(resolveLinks(lineConfig.getLinksDirectionA(), links).get(0).getFrom())
                        .end(resolveLinks(lineConfig.getLinksDirectionB(), links).get(0).getFrom())
                        .vehiclesOnLineB(new ArrayList<>())
                        .vehiclesOnLineA(new ArrayList<>())
                        .linksDirectionA(resolveLinks(lineConfig.getLinksDirectionA(), links))
                        .linksDirectionB(resolveLinks(lineConfig.getLinksDirectionB(), links))
                        .build();
            }).collect(Collectors.toCollection(ArrayList::new));
            vehicles = new ArrayList<>();
            config.getLines().forEach(lineConfig -> {
                List<Vehicle> rawVehicles = generateVehicles(lineConfig.getVehicleConfig(),lineConfig, lines);
                List<Schedule> schedules = generateSchedules(rawVehicles.size(), lineConfig, lines, config.getRuntimeHours(), 0);
                for(int i = 0; i < rawVehicles.size(); i++){
                    rawVehicles.get(i).setSchedule(schedules.get(i));
                }
                vehicles.addAll(rawVehicles);
            });
            config.getLines().forEach(lineConfig -> {
                List<Vehicle> rawVehicles = generateVehicles(lineConfig.getVehicleConfig(),lineConfig, lines);
                for (Vehicle rawVehicle : rawVehicles) {
                    rawVehicle.setDirection(1);
                }
                List<Schedule> schedules = generateSchedules(rawVehicles.size(), lineConfig, lines, config.getRuntimeHours(), 1);
                for(int i = 0; i < rawVehicles.size(); i++){
                    rawVehicles.get(i).setSchedule(schedules.get(i));
                }
                vehicles.addAll(rawVehicles);
            });
            this.network = Network.builder().links(links).stations(stations).build();
            network.calculateNetworkTopologyInformation();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Schedule> generateSchedules(int size, LineConfig lineConfig, List<Line> lines, Integer runtimeHours, int startDirection) {
        List<Schedule> schedules = new ArrayList<>();
        Line correctLine = lines.stream().filter(l -> l.getName().equals(lineConfig.getName())).findFirst().orElse(null);
        for(int i = 1; i <= size; i++){
            Schedule schedule = Schedule.builder().time(timeObj).build();
            schedule.generateSchedule(correctLine, lineConfig.getPauseTime(),
                    (int) (8024),
                    timeObj.getCurrentTime().plusSeconds(i * lineConfig.getVehicleConfig().getDistance()), startDirection);
            schedules.add(schedule);
        }
        return schedules;
    }

    private List<Vehicle> generateVehicles(VehicleConfig vehicleConfig, LineConfig lineConfig, List<Line> lines) {
        List<Vehicle> result = new ArrayList<>();
        Line correctLine = lines.stream().filter(l -> l.getName().equals(lineConfig.getName())).findFirst().orElse(null);
        for(int i = 0; i < vehicleConfig.getCount(); i++){
            assert correctLine != null;
            result.add(Vehicle.builder().cooperative(lineConfig.getVehicleConfig().getIsCooperative())
                    .plannedHeadway(lineConfig.getVehicleConfig().getDistance()).name("FZ_"+(vehicles.size() + i+1)+"_L"+correctLine.getName()).time(timeObj).line(correctLine).state(State.PAUSE).build());
        }
        return result;
    }

    private List<Link> resolveLinks(List<Integer> linksDirection, List<Link> links) {
        List<Link> result = new ArrayList<>();
        for (Integer idOfLink : linksDirection) {
            result.add(links.stream().filter(l -> l.getId() == idOfLink).findAny().orElse(null));
        }
        return result;
    }
}
