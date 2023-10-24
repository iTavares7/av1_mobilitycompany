package br.com.av1.sumo.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;

import br.com.av1.sumo.enumerators.BankingActions;
import br.com.av1.sumo.model.ResponseDTO;
import br.com.av1.sumo.model.banking.BankingDTO;
import br.com.av1.sumo.util.EncryptData;

public class BankingService {

    private static int bankServicePort;
    private static Gson gson = new Gson();
    private static EncryptData encryptData = new EncryptData();

    public BankingService() {
    }

    private Socket getSocket() throws Exception {
        return new Socket("localhost", bankServicePort);
    }

    private ResponseDTO makeRequest(BankingDTO requestDTO) throws Exception {
        try (Socket socket = getSocket();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream data = new ObjectInputStream(socket.getInputStream())) {

            String requestJson = gson.toJson(requestDTO);
            String encryptedJson = encryptData.encrypt(requestJson);

            oos.writeObject(encryptedJson);
            oos.flush();

            Object response = data.readObject();

            if (response instanceof String) {
                String encryptedJsonResponse = (String) response;
                String decryptedJsonResponse = encryptData.decrypt(encryptedJsonResponse);
                ResponseDTO responseDTO = gson.fromJson(decryptedJsonResponse, ResponseDTO.class);
                return responseDTO;
            }

            throw new Exception("Error making request");

        }
    }

    public boolean transferFunds(UUID fromAccount, UUID toAccount, double value) throws Exception {
        BankingDTO request = new BankingDTO(BankingActions.TRANSFER, fromAccount, toAccount, value);
        ResponseDTO response = this.makeRequest(request);
        return response.getSuccess();
    }

    public UUID createAccount(double balance) throws Exception {
        BankingDTO request = new BankingDTO(BankingActions.CREATE_ACCOUNT, balance);
        ResponseDTO response = this.makeRequest(request);

        if (response.getSuccess()) {
            return UUID.fromString(response.getMessage());
        }

        throw new Exception("Error while Creating Account");
    }

    public double getBalance(UUID fromAccount) throws Exception {
        BankingDTO request = new BankingDTO(BankingActions.GET_BALANCE, fromAccount);
        ResponseDTO response = this.makeRequest(request);
        if(response.getSuccess()){
            return Double.parseDouble(response.getMessage());
        }

        throw new Exception("Error while getting balance");
    }

    public static void setBankServicePort(int servicePort) {
        bankServicePort = servicePort;
    }
}
