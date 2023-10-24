package br.com.av1.sumo.model.mobility;

import java.util.List;

public class Route {
    
    private String routeId = "1";

    private List<String> points;

    public Route(List<String> points) {
        this.points = points;
    }

    // Getters e setters
    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }

    public String getRouteId() {
        return routeId;
    }
}