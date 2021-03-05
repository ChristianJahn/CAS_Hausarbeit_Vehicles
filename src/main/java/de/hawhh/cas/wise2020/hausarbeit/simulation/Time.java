package de.hawhh.cas.wise2020.hausarbeit.simulation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Time {

    private LocalDateTime currentTime;

    public void tick(){
        this.currentTime = currentTime.plusSeconds(1);
    }


    }
