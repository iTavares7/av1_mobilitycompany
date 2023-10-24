package br.com.av1.sumo.model.banking;

import java.util.UUID;

import br.com.av1.sumo.enumerators.BankingActions;

public class BankingDTO {
    private BankingActions action;
    private UUID fromAccount;
    private UUID toAccount;

    private double value;

    public BankingDTO(BankingActions action, UUID fromAccount, UUID toAccount, double value) {
        this.action = action;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.value = value;
    }

    public BankingDTO(BankingActions action, double value) {
        this.action = action;
        this.value = value;
        this.fromAccount = null;
        this.toAccount = null;
    }

     public BankingDTO(BankingActions action, UUID account) {
        this.action = action;
        this.fromAccount = account;
    }

    public BankingDTO() {
    }

    public UUID getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(UUID fromAccount) {
        this.fromAccount = fromAccount;
    }

    public UUID getToAccount() {
        return toAccount;
    }

    public void setToAccount(UUID toAccount) {
        this.toAccount = toAccount;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public BankingActions getAction() {
        return action;
    }

    public void setAction(BankingActions action) {
        this.action = action;
    }
}
