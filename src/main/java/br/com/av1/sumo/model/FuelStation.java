package br.com.av1.sumo.model;

import br.com.av1.sumo.model.mobility.Driver;
import br.com.av1.sumo.services.BankingService;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class FuelStation extends Thread {
    private static FuelStation instance = null;

    private UUID account;
    private static final double FUELPRICE = 5.87;
    private Semaphore pumps = new Semaphore(2);
    private BlockingQueue<Pair<Pair<Driver, Integer>, CompletableFuture<Boolean>>> carRefuelQueue = new LinkedBlockingQueue<>();

    private BankingService bankingService = new BankingService();

    private FuelStation() {
        try {
            this.account = bankingService.createAccount(0d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FuelStation getInstance() {
        if (instance == null) {
            instance = new FuelStation();
        }
        return instance;
    }

    public CompletableFuture<Boolean> getInRefuelQueue(Driver driver, Integer amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Pair<Driver, Integer> innerPair = new Pair<>(driver, amount);
        Pair<Pair<Driver, Integer>, CompletableFuture<Boolean>> pair = new Pair<>(innerPair, future);
        carRefuelQueue.add(pair);

        return future;
    }

    private boolean chargeDriver(Driver driver, int amount) throws Exception {
        return this.bankingService.transferFunds(driver.getAccount(), this.account, amount * FUELPRICE);
    }

    private boolean refuelCar(Driver driver, int amount) throws Exception {
        boolean success = false;
        try {
            pumps.acquire();
            System.out.println("abastecendo");
            if (chargeDriver(driver, amount)) {
                driver.refuelCar(amount);
                success = true;
            }
        } finally {
            pumps.release();
        }
            return success;
    }

    public double getFuelPrice() {
        return FUELPRICE;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Pair<Pair<Driver,Integer>,CompletableFuture<Boolean>> qeueItem = carRefuelQueue.take();
                Pair<Driver,Integer> carInLine = qeueItem.getFirst();
                new Thread(() -> {
                    try {
                        qeueItem.getSecond().complete(this.refuelCar(carInLine.getFirst(), carInLine.getSecond()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

             } catch( InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Fuel station closed.");
    }
}
