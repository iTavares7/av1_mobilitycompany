package br.com.av1.sumo.model.mobility.testdrive;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DataAnalysis {

    DescriptiveStatistics speedStats = new DescriptiveStatistics();
    DescriptiveStatistics distanceStats = new DescriptiveStatistics();
    DescriptiveStatistics travelTime = new DescriptiveStatistics();
    DescriptiveStatistics fuelConsumption = new DescriptiveStatistics();

    public void addSpeed(double speed) {
        speedStats.addValue(speed);
    }

    public void addDistance(double distance) {
        distanceStats.addValue(distance);
    }

    public void addTravelTime(double time) {
        if (travelTime.getN() > 0) {
            travelTime.addValue(time - travelTime.getElement((int) travelTime.getN() - 1));
        } else {
            travelTime.addValue(time);
        }
    }

    public void addFuelConsumption(double fuel) {
        fuelConsumption.addValue(fuel);
    }

    public double getBiasSpeed(double trueSpeed) {
        return speedStats.getMean() - trueSpeed;
    }

    public double getPrecisionSpeed() {
        return speedStats.getStandardDeviation();
    }

    public double getUncertaintySpeed() {
        return speedStats.getStandardDeviation() / Math.sqrt(speedStats.getN());
    }

    public double getBiasDistance(double trueDistance) {
        return distanceStats.getMean() - trueDistance;
    }

    public double getPrecisionDistance() {
        return distanceStats.getStandardDeviation();
    }

    public double getUncertaintyDistance() {
        return distanceStats.getStandardDeviation() / Math.sqrt(distanceStats.getN());
    }

    public double getBiasTravelTime(double trueTravelTime) {
        return travelTime.getMean() - trueTravelTime;
    }

    public double getPrecisionTravelTime() {
        return travelTime.getStandardDeviation();
    }

    public double getUncertaintyTravelTime() {
        return travelTime.getStandardDeviation() / Math.sqrt(travelTime.getN());
    }

    public double getBiasFuelConsumption(double trueFuelConsumption) {
        return fuelConsumption.getMean() - trueFuelConsumption;
    }

    public double getPrecisionFuelConsumption() {
        return fuelConsumption.getStandardDeviation();
    }

    public double getUncertaintyFuelConsumption() {
        return fuelConsumption.getStandardDeviation() / Math.sqrt(fuelConsumption.getN());
    }

    public double getTotalTravelTime() {
        return travelTime.getSum();
    }

    public void print(double trueSpeed, double trueDistance, double trueTravelTime, double trueFuelConsumption) {
        System.out.println("Speed: bias = " + getBiasSpeed(trueSpeed) + ", precision = " + getPrecisionSpeed()
                + ", uncertainty = " + getUncertaintySpeed());
        System.out.println("Distance: bias = " + getBiasDistance(trueDistance) + ", precision = "
                + getPrecisionDistance() + ", uncertainty = " + getUncertaintyDistance());
        System.out.println("Travel time: bias = " + getBiasTravelTime(trueTravelTime) + ", precision = "
                + getPrecisionTravelTime() + ", uncertainty = " + getUncertaintyTravelTime());
        System.out.println("Fuel consumption: bias = " + getBiasFuelConsumption(trueFuelConsumption) + ", precision = "
                + getPrecisionFuelConsumption() + ", uncertainty = " + getUncertaintyFuelConsumption());
    }

    public void print() {
        System.out.println("Speed: mean = " + getBiasSpeed(0) + ", precision = " + getPrecisionSpeed()
                + ", uncertainty = " + getUncertaintySpeed());
        System.out.println("Distance: mean = " + getBiasDistance(0) + ", precision = " + getPrecisionDistance()
                + ", uncertainty = " + getUncertaintyDistance());
        System.out.println("Travel time: mean = " + getBiasTravelTime(0) + ", precision = " + getPrecisionTravelTime()
                + ", uncertainty = " + getUncertaintyTravelTime());
        System.out.println("Fuel consumption: mean = " + getBiasFuelConsumption(0) + ", precision = "
                + getPrecisionFuelConsumption() + ", uncertainty = " + getUncertaintyFuelConsumption());
    }
}
