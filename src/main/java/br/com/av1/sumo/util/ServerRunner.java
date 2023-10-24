package br.com.av1.sumo.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner<T extends RunnerInterface> extends Thread {
    private ServerSocket serverSocket;
    private T runner;

    public ServerRunner(int port, T runner) throws IOException {
        serverSocket = new ServerSocket(port);
        this.runner = runner;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                T newRunner = (T) this.runner.getClass().getConstructor().newInstance();
                newRunner.setSocket(clientSocket);
                newRunner.start();
            } catch (Exception e) {
                System.out.println("Falha ao criar novo runner.");
            }
        }
    }
}
