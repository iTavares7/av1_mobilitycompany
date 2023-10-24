package br.com.av1.sumo.model.mobility;

public class VehicleData {
    private String timestamp;
    private String idCar;
    private String idRoute;
    private double speed;
    private double distance;
    private double fuelConsumption;
    private String fuelType;
    private double co2Emission;
    private double longitude;
    private double latitude;

    public VehicleData() {
    }

    public VehicleData(String timestamp, String idCar, String idRoute, double speed, double distance,
            double fuelConsumption, String fuelType, double co2Emission, double longitude, double latitude) {
        this.timestamp = timestamp;
        this.idCar = idCar;
        this.idRoute = idRoute;
        this.speed = speed;
        this.distance = distance;
        this.fuelConsumption = fuelConsumption;
        this.fuelType = fuelType;
        this.co2Emission = co2Emission;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdCar() {
        return idCar;
    }

    public void setIdCar(String idCar) {
        this.idCar = idCar;
    }

    public String getIdRoute() {
        return idRoute;
    }

    public void setIdRoute(String idRoute) {
        this.idRoute = idRoute;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getCo2Emission() {
        return co2Emission;
    }

    public void setCo2Emission(double co2Emission) {
        this.co2Emission = co2Emission;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
