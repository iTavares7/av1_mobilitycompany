package br.com.av1.sumo.model.mobility;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.google.gson.Gson;
import br.com.av1.sumo.util.EncryptData;

import br.com.av1.sumo.enumerators.MobilityActions;
import br.com.av1.sumo.util.AutoIncrement;

import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;

public class Car extends Thread {

    private static Gson gson = new Gson();
    private static EncryptData encryptData = new EncryptData();

    private String vehicleId;
    private FuelTank fuelTank;

    private int mobilityPort;
    private Socket mobilitySocket;

    private CyclicBarrier threadBarrier;
    private CyclicBarrier simulationBarrier;

    public Car(int mobilityPort, CyclicBarrier threadBarrier, CyclicBarrier simulationBarrier) {
        this.threadBarrier = threadBarrier;
        this.simulationBarrier = simulationBarrier;
        this.vehicleId = AutoIncrement.getNextId().toString();
        this.fuelTank = new FuelTank(20d);
        this.mobilityPort = mobilityPort;
    }

    public double getDistance() {
        try {
            return Vehicle.getDistance(this.vehicleId);
        } catch (Exception e) {
            return 0.0d;
        }
    };

    public synchronized boolean hasRoute() {
        try {
            return Vehicle.getIDList().contains(this.vehicleId);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void setRoute(Trip trip) {
        if (!this.hasRoute()) {
            try {
                Vehicle.add(this.vehicleId, trip.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public boolean needsRefuel() {
        return fuelTank.getFuelLevel() <= 3.0d;
    }

    public Double getFuelLevel() {
        return fuelTank.getFuelLevel();
    }

    public void refuel(double amount) {
        this.fuelTank.refuelFuelTank(amount);
    }

    private void sendDataToMobility() {
        if (mobilitySocket == null || mobilitySocket.isClosed()) {
            try {
                this.mobilitySocket = new Socket("localhost", this.mobilityPort);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            TraCIPosition position = Vehicle.getPosition(this.vehicleId);
            VehicleData vehicleData = new VehicleData(Instant.now().toString(), this.vehicleId,
                    Vehicle.getRouteID(this.vehicleId), Vehicle.getSpeed(this.vehicleId),
                    Vehicle.getDistance(this.vehicleId), Vehicle.getFuelConsumption(this.vehicleId), "gasoline",
                    Vehicle.getCO2Emission(this.vehicleId), position.getX(), position.getY());
            MobilityDTO<VehicleData> request = new MobilityDTO<>(MobilityActions.REPORT, vehicleData);
            String json = gson.toJson(request);
            String encryptedData = encryptData.encrypt(json);
            ObjectOutputStream out = new ObjectOutputStream(mobilitySocket.getOutputStream());
            out.writeObject(encryptedData);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {

            try {
                if (this.hasRoute()) {
                    this.fuelTank.consumeFuel(Vehicle.getFuelConsumption(this.vehicleId) / 2500);
                    sendDataToMobility();
                }
                
                this.threadBarrier.await();
                this.simulationBarrier.await();
                

            } catch (BrokenBarrierException | InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        try {
            this.mobilitySocket.close();
        } catch (Exception e) {
            System.out.println("Error closing socket");
        }
        System.out.println("Car " + this.vehicleId + " stopped.");
    }
}
