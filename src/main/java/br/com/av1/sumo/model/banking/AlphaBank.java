package br.com.av1.sumo.model.banking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import com.google.gson.Gson;
import br.com.av1.sumo.model.ResponseDTO;
import br.com.av1.sumo.util.EncryptData;
import br.com.av1.sumo.util.RunnerInterface;

public class AlphaBank extends Thread implements RunnerInterface {

    private Socket client;
    private static EncryptData encryptData = new EncryptData();
    private static Gson gson = new Gson();
    private static ArrayList<Account> accounts = new ArrayList<>();

    public AlphaBank(Socket client) {
        this.client = client;
    }

    public AlphaBank() {
    }

    public void setSocket(Socket socket) {
        this.client = socket;
    }

    private synchronized UUID createAccount(double balance) {
        Account account = new Account(balance);
        accounts.add(account);
        return account.getId();
    }

    private Account getAccountByUUID(UUID accountId) {
        return accounts.stream().filter(account -> account.getId().equals(accountId)).findFirst().get();
    }

    private Double getBalance(UUID accountId) {
        return getAccountByUUID(accountId).getBalance();
    }

    private boolean transferFunds(UUID fromAccount, UUID toAccount, double amount) {
        Account originAccout = getAccountByUUID(fromAccount);
        Account destinationAccount = getAccountByUUID(toAccount);

        synchronized (originAccout) {
            synchronized (destinationAccount) {
                if (originAccout.getBalance() >= amount) {
                    originAccout.withdraw(amount);
                    destinationAccount.deposit(amount);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void run() {
        while (!client.isClosed()) {
            try (ObjectInputStream data = new ObjectInputStream(client.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

                Object object = data.readObject();
                if (object instanceof String) {
                    processRequest((String) object, out);
                } else {
                    throw new Exception("Invalid data type");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void processRequest(String encryptedJson, ObjectOutputStream out) throws IOException {
        try {
            String decryptedJson = encryptData.decrypt(encryptedJson);
            BankingDTO jsonObject = gson.fromJson(decryptedJson, BankingDTO.class);

            ResponseDTO response;

            switch (jsonObject.getAction()) {
                case CREATE_ACCOUNT:
                    response = new ResponseDTO(true, createAccount(jsonObject.getValue()).toString());
                    break;
                case TRANSFER:
                    boolean success = transferFunds(jsonObject.getFromAccount(), jsonObject.getToAccount(),
                            jsonObject.getValue());
                    response = new ResponseDTO(success, success ? "Transfer successful" : "Insufficient balance");
                    break;
                case GET_BALANCE:
                    response = new ResponseDTO(true, getBalance(jsonObject.getFromAccount()).toString());
                    break;
                default:
                    throw new Exception("Invalid action");
            }

            String responseJson = gson.toJson(response);
            String encryptedResponseJson = encryptData.encrypt(responseJson);

            out.writeObject(encryptedResponseJson);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
