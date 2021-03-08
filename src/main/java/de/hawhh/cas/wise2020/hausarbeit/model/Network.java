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

}
