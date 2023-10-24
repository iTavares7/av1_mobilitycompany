package br.com.av1.sumo.model.banking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {
    private UUID id;
    private double balance;
    private List<Transaction> transactionHistory;

    public Account() {
        this.id = UUID.randomUUID();
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    public Account(double balance) {
        this.id = UUID.randomUUID();
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public synchronized void deposit(double amount) {
        balance += amount;
        transactionHistory.add(new Transaction(amount, System.nanoTime()));
    }

    public synchronized boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            transactionHistory.add(new Transaction(-amount, System.nanoTime()));
            return true;
        } else {
            return false;
        }
    }

    public UUID getId() {
        return this.id;
    }
}
