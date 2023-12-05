package br.com.av1.sumo.model.mobility.testdrive;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.sumo.libtraci.Route;
import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;

import br.com.av1.sumo.model.mobility.TrafficJammer;
import br.com.av1.sumo.model.mobility.VehicleData;

public class TestDrive extends Thread {

    private String vehicleId;

    public List<Reports> runReports = new ArrayList<>();

    public int runnerCounter = 0;

    private CyclicBarrier threadBarrier;
    private CyclicBarrier simulationBarrier;

    public String routeId;

    private TrafficJammer jammer = new TrafficJammer();

    public TestDrive(TestVehicle vehicle, CyclicBarrier threadBarrier, CyclicBarrier simulationBarrier) {
        this.routeId = "route_" + vehicle.id;
        this.vehicleId = vehicle.id;
        this.threadBarrier = threadBarrier;
        this.simulationBarrier = simulationBarrier;
        addRoute(Arrays.asList(vehicle.route.edges.split(("\\s+"))));
        initializeCSV();
    }

    private void addRoute(List<String> routeEdges) {
        Route.add(this.routeId, new StringVector(routeEdges));
    }

    public double getDistance() {
        try {
            return Vehicle.getDistance(this.vehicleId);
        } catch (Exception e) {
            return 0.0d;
        }
    };

    public String getVehicleId() {
        return this.vehicleId;
    }

    public synchronized boolean hasRoute() {
        try {
            return Vehicle.getIDList().contains(this.vehicleId);
        } catch (Exception e) {
            return false;
        }
    }

    public void setNewRoute() {
        jammer.addJam();
        this.runnerCounter++;
        this.runReports.add(new Reports("Run_" + this.runnerCounter));
        this.runReports.get(this.runReports.size() - 1).initialoffset = Simulation.getCurrentTime();
        if(runReports.size() > 1) {
            totalsRealTimeUpdate(runReports.get(runReports.size() - 2));
        }
        Vehicle.add(this.vehicleId, this.routeId);
        if (this.runnerCounter < 10) {
            Vehicle.setMaxSpeed(this.vehicleId, ((int) (this.runnerCounter / 10)) * 10 + 30);
        }
        System.out.println(
                "Run nro " + this.runnerCounter + " route speed " + (((int) (this.runnerCounter / 10)) * 10 + 30));
    }

    private VehicleData logReport() {
        TraCIPosition position = org.eclipse.sumo.libtraci.Vehicle.getPosition(this.vehicleId);
        return new VehicleData(Instant.now().toString(), this.vehicleId,
                org.eclipse.sumo.libtraci.Vehicle.getRouteID(this.vehicleId),
                org.eclipse.sumo.libtraci.Vehicle.getSpeed(this.vehicleId),
                org.eclipse.sumo.libtraci.Vehicle.getDistance(this.vehicleId),
                org.eclipse.sumo.libtraci.Vehicle.getFuelConsumption(this.vehicleId), "gasoline",
                org.eclipse.sumo.libtraci.Vehicle.getCO2Emission(this.vehicleId), position.getX(), position.getY());
    }

    private void initializeCSV() {
        try (FileWriter writer = new FileWriter("realtime.csv", false)) {
            writer.append(
                    "Timestamp,RunNbr,SimTime,MaxSpeed,Speed,Distance,FuelConsumption,FuelType,Co2Emission,Longitude,Latitude\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter writer = new FileWriter("totals.csv", false)) {
        writer.append("MeanSpeed;PrecisionSpeed;UncertaintySpeed;MeanDistance;PrecisionDistance;UncertaintyDistance;MeanFuelConsumption;PrecisionFuelConsumption;UncertaintyFuelConsumption;TotalTravelTime\n");
        writer.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    private void totalsRealTimeUpdate(Reports report) {
        report.dataAnalysis.print();
        try (FileWriter writer = new FileWriter("totals.csv", true)) {
            writer.append("" + report.dataAnalysis.getBiasSpeed(0d)).append(";");
            writer.append(report.dataAnalysis.getPrecisionSpeed() + "").append(";");
            writer.append(report.dataAnalysis.getUncertaintySpeed() + "").append(";");
            writer.append(report.dataAnalysis.getBiasDistance(0d) + "").append(";");
            writer.append(report.dataAnalysis.getPrecisionDistance() + "").append(";");
            writer.append(report.dataAnalysis.getUncertaintyDistance() + "").append(";");
            writer.append(report.dataAnalysis.getBiasFuelConsumption(0d) + "").append(";");
            writer.append(report.dataAnalysis.getPrecisionFuelConsumption() + "").append(";");
            writer.append(report.dataAnalysis.getUncertaintyFuelConsumption() + "").append(";");
            writer.append((report.dataAnalysis.getTotalTravelTime() + "").split(("\\."))[0]).append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void realTimeUpdate(VehicleData vehicleData, int simulationTime) {
        try (FileWriter writer = new FileWriter("realtime.csv", true)) {
            writer.append(vehicleData.getTimestamp()).append(",");
            writer.append("" + this.runnerCounter).append(",");
            writer.append(simulationTime + "").append(",");
            writer.append((((int) (this.runnerCounter / 10)) * 10 + 30) + "").append(",");
            writer.append(String.valueOf(vehicleData.getSpeed())).append(",");
            writer.append(String.valueOf(vehicleData.getDistance())).append(",");
            writer.append(String.valueOf(vehicleData.getFuelConsumption())).append(",");
            writer.append(vehicleData.getFuelType()).append(",");
            writer.append(String.valueOf(vehicleData.getCo2Emission())).append(",");
            writer.append(String.valueOf(vehicleData.getLongitude())).append(",");
            writer.append(String.valueOf(vehicleData.getLatitude())).append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToReports(VehicleData vehicleData) {
        int simulationTime = Simulation.getCurrentTime();
        runReports.get(this.runnerCounter - 1).addToReports(vehicleData);
        runReports.get(this.runnerCounter - 1).addTravelTime(simulationTime);

        realTimeUpdate(vehicleData, simulationTime);
    }

    private boolean isStaged() {
        return Simulation.getPendingVehicles().contains(this.vehicleId);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            if (this.runnerCounter == 100) {
                break;
            }
            boolean routed = hasRoute();
            if (routed) {
                addToReports(logReport());
            } else if (!routed && !isStaged()) {
                setNewRoute();
            }

            try {
                threadBarrier.await();
                simulationBarrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        try {
            threadBarrier.await();
            simulationBarrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
