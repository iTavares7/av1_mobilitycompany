package br.com.av1.sumo.model.mobility;

public class FuelTank {
    
    private double fuel;

    public FuelTank(double fuel){
        this.fuel = fuel;
    }

    public void consumeFuel(double amount){
        this.fuel -= amount;
    }

    public double getFuelLevel() {
        return fuel;
    }

    public void  refuelFuelTank(double fuelAmount) {
        this.fuel = this.fuel + fuelAmount;
    }
}
