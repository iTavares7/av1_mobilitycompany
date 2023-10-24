package br.com.av1.sumo.model.banking;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import br.com.av1.sumo.model.Pair;
import br.com.av1.sumo.services.BankingService;

public class BotPayment extends Thread {

    private UUID accountID;
    private BankingService bankingService = new BankingService();
    private LinkedBlockingQueue<Pair<UUID, Double>> payments = new LinkedBlockingQueue<>();

    public BotPayment(UUID accountID) {
        this.accountID = accountID;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Pair<UUID, Double> payment = payments.take();
                try {
                    bankingService.transferFunds(this.accountID, payment.getFirst(), payment.getSecond());
                } catch (Exception e) {
                    System.out.println("Erro ao transferir fundos");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Bot de pagamento finalizado.");
    }

    public void addPayment(UUID destinationAcc, Double paymentValue) {
        payments.add(new Pair<>(destinationAcc, paymentValue));
    }

}
