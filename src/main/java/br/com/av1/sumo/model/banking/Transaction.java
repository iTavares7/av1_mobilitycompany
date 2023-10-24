package br.com.av1.sumo.model.banking;

public class Transaction {
    private double amount;
    private long timestamp;

    public Transaction(double amount, long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
