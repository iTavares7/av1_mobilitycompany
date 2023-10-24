package br.com.av1.sumo.model.mobility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;

import br.com.av1.sumo.util.EncryptData;
import com.google.gson.Gson;

import br.com.av1.sumo.enumerators.MobilityActions;
import br.com.av1.sumo.model.FuelStation;
import br.com.av1.sumo.model.banking.PaymentOrder;
import br.com.av1.sumo.services.BankingService;

public class Driver extends Thread {

    private static Gson gson = new Gson();
    private static EncryptData encryptData = new EncryptData();

    private static FuelStation fuelStation = FuelStation.getInstance();

    private Car car;

    private UUID account;
    private Trip currentRoute;
    private List<Trip> completedRoutes;
    private BankingService bankingService = new BankingService();

    private int mobilityPort;
    private Socket mobilitySocket;
    private Socket newRouteSocket;

    private CyclicBarrier barrier;

    private double lastDistance = 0;

    private double pendingDistance = 0;

    public Driver(int mobilityPort, CyclicBarrier barrier) {
        try {
            this.barrier = barrier;
            this.account = bankingService.createAccount(100d);
            this.mobilityPort = mobilityPort;
            this.currentRoute = requestNewRoute();
            this.car = new Car(mobilityPort, barrier);
            car.start();
        } catch (Exception e) {
            System.out.println("Erro ao criar conta");
            this.interrupt();
        }
        this.completedRoutes = new ArrayList<>();
    }

    public double driverBalance() {
        try {
            return bankingService.getBalance(this.account);
        } catch (Exception e) {
            System.out.println("Erro ao consultar saldo");
            return 0d;
        }
    }

    public void refuelCar(int amount) {
        this.car.refuel(amount);
    }

    public synchronized void markRouteAsCompleted() {
        completedRoutes.add(currentRoute);
    }

    public UUID getAccount() {
        return this.account;
    }

    public Trip getCurrentRoute() {
        return this.currentRoute;
    }

    private void requestPayment(int kmRodados) {
        try {
            if (mobilitySocket == null || mobilitySocket.isClosed()) {
                this.mobilitySocket = new Socket("localhost", this.mobilityPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(this.mobilitySocket.getOutputStream());
            MobilityDTO<PaymentOrder> request = new MobilityDTO<>(MobilityActions.PAYMENT_REQUEST,
                    new PaymentOrder(kmRodados, this.account));
            String json = gson.toJson(request);
            String encryptedData = encryptData.encrypt(json);
            out.writeObject(encryptedData);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private Trip requestNewRoute() {

        try {
            if (newRouteSocket == null || newRouteSocket.isClosed()) {
                this.newRouteSocket = new Socket("localhost", this.mobilityPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Trip();
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(this.newRouteSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.newRouteSocket.getInputStream());

            MobilityDTO<Trip> request;

            if (this.currentRoute != null && this.currentRoute.getId() != null) {
                request = new MobilityDTO<>(MobilityActions.GET_NEW_ROUTE,
                        (this.currentRoute));
            } else {
                request = new MobilityDTO<>(MobilityActions.GET_NEW_ROUTE,
                        new Trip());
            }

            String json = gson.toJson(request);
            String encryptedData = encryptData.encrypt(json);
            out.writeObject(encryptedData);
            out.flush();

            String encryptedResponse = (String) in.readObject();
            String decryptedResponse = encryptData.decrypt(encryptedResponse);

            return gson.fromJson(decryptedResponse, Trip.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new Trip();
        }
    }

    @Override
    public void run() {
        while (!this.car.isInterrupted()) {

            double distance = this.car.getDistance();
            double traveledDistance = distance - this.lastDistance;
            this.lastDistance = distance;

            if(traveledDistance < 0) {
                traveledDistance = 0;
            }

            this.pendingDistance += traveledDistance / 10;

            if ((int) pendingDistance > 0) {
                try {
                    this.requestPayment((int) pendingDistance);
                    this.pendingDistance -= (int) pendingDistance;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (this.car.needsRefuel()) {
                int amoutToRefuel = (int) (this.driverBalance() /
                        fuelStation.getFuelPrice());
                if (amoutToRefuel > 0) {
                    this.car.pauseThread();
                    try {
                        CompletableFuture<Boolean> refueled = fuelStation.getInRefuelQueue(this, amoutToRefuel);
                        while (!refueled.isDone()) {
                            barrier.await();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.car.resumeThread();
                } else {
                    this.car.interrupt();

                    try {
                        this.car.join();
                    } catch (Exception e) {
                        System.out.println("Error joining car thread");
                    }
                    this.interrupt();
                    System.out.println("Driver " + this.getId() + " ran out of gas");
                    break;
                }
            }

            if (!this.car.hasRoute()) {
                if (this.currentRoute != null) {
                    this.markRouteAsCompleted();
                }
                this.currentRoute = requestNewRoute();
                if (currentRoute.getId() != null) {
                    this.car.setRoute(currentRoute);
                } else {
                    this.car.interrupt();
                }
            }

            try {
                barrier.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            this.mobilitySocket.close();
            this.newRouteSocket.close();
        } catch (Exception e) {
            System.out.println("Error closing socket");
        }

        try {
            this.car.interrupt();
            this.car.join();
        } catch (InterruptedException e) {
            this.car.interrupt();
        } catch (Exception e) {
            System.out.println("Error joining car thread");
        }
        System.out.println("Driver " + this.getId() + " finished");
    }
}
