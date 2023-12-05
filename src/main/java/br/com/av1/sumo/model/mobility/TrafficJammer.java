package br.com.av1.sumo.model.mobility;

import org.eclipse.sumo.libtraci.Vehicle;

public class TrafficJammer {

    private Integer jammerCounter = 0;

    private synchronized Integer getJammer() {
        return jammerCounter++;
    }
    
    public void addJam() {
        for (int i = 0; i < 0; i++) {
            Long value = Math.round((Math.random() * 899));
            Vehicle.add("jam" + getJammer().toString(), value.toString());
        }
    }

}