package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class Network {

    private List<Station> stations;

    private List<Link> links;

    private Map<Station, Integer> degreeByStation;

    private Map<Station, Integer> outDegreeStation;

    private Map<Station, Integer> inDegreeStation;

    public void calculateNetworkTopologyInformation(){
        degreeByStation = new HashMap<>();
        inDegreeStation = new HashMap<>();
        outDegreeStation = new HashMap<>();
        for(Station station : stations){
            degreeByStation.put(station, (int) links.stream().filter(l -> l.getFrom().equals(station) || l.getTo().equals(station)).count());
            inDegreeStation.put(station,  (int) links.stream().filter(l ->  l.getTo().equals(station)).count());
            outDegreeStation.put(station, (int) links.stream().filter(l -> l.getFrom().equals(station) ).count());
        }
    }

}
