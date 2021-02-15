package de.hawhh.cas.wise2020.hausarbeit.simulation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Time {

    private LocalDateTime currentTime;

    public void setTime(LocalDateTime time){
        this.currentTime = time;
    }

    public void tick(){
        this.currentTime = currentTime.plusSeconds(1);
    }

    public void tickSeconds(int seconds){
        this.currentTime = currentTime.plusSeconds(seconds);
    }

    public void tickHours(int hours){

    }
}
