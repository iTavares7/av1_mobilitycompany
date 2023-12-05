package br.com.av1.sumo.model.mobility.testdrive;

import java.util.ArrayList;
import java.util.List;

import br.com.av1.sumo.model.mobility.VehicleData;

public class Reports {
    public String runId;
    public List<VehicleData> reports = new ArrayList<>();
    public DataAnalysis dataAnalysis = new DataAnalysis();
    public double initialoffset = 0;

    public Reports(String id) {
        this.runId = id;
    }

    public void addTravelTime(double time) {
        dataAnalysis.addTravelTime(time - initialoffset);
    }

    public void addToReports(VehicleData pointData) {
        dataAnalysis.addDistance(pointData.getDistance());
        dataAnalysis.addSpeed(pointData.getSpeed());
        dataAnalysis.addDistance(pointData.getDistance());
        dataAnalysis.addFuelConsumption(pointData.getFuelConsumption());
        reports.add(pointData);
    }

}
