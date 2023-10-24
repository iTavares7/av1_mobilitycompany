package br.com.av1.sumo.util;

import java.net.Socket;

public interface RunnerInterface extends Runnable {
     void setSocket(Socket socket);
     void start();
}
