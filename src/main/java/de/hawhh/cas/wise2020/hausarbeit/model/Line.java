package de.hawhh.cas.wise2020.hausarbeit.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class Line {

    private String name;

    private List<Link> linksDirectionA;

    private List<Link> linksDirectionB;

    private Station start;

    private Station end;

    public boolean isAtEndA(Station currentStation){
        return currentStation.equals(end);
    }

    public boolean isAtEndB(Station currentStation){
        return currentStation.equals(start);
    }

    public Link getNextLinkB(Station currentStation) {
        return linksDirectionB.stream()
                .filter(l -> l.getFrom().getName().equals(currentStation.getName()))
                .findFirst()
                .orElse(null);
    }

    public Link getNextLinkA(Station currentStation) {
        return linksDirectionA.stream()
                .filter(l -> l.getFrom().getName().equals(currentStation.getName()))
                .findFirst()
                .orElse(null);
    }
}
