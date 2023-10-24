package br.com.av1.sumo.model.banking;

import java.util.UUID;

public class PaymentOrder {
    private int kmDriven;
    private UUID destinationAccount;
    
    public PaymentOrder(int kmDriven, UUID destinationAccount) {
        this.kmDriven = kmDriven;
        this.destinationAccount = destinationAccount;
    }

    public UUID getDestinationAccount() {
        return destinationAccount;
    }

    public int getKmDriven() {
        return kmDriven;
    }
}
