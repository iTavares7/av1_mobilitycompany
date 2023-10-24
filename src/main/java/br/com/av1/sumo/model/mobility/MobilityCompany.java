package br.com.av1.sumo.model.mobility;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import br.com.av1.sumo.util.EncryptData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import br.com.av1.sumo.model.banking.BotPayment;
import br.com.av1.sumo.model.banking.PaymentOrder;
import br.com.av1.sumo.services.BankingService;
import br.com.av1.sumo.util.RunnerInterface;
import java.util.concurrent.ConcurrentLinkedQueue;
// import java.util.concurrent.CyclicBarrier;

import org.eclipse.sumo.libtraci.Route;
import org.eclipse.sumo.libtraci.StringVector;

public class MobilityCompany extends Thread implements RunnerInterface {

    private static Gson gson = new Gson();
    private static EncryptData encryptData = new EncryptData();
    
    private static ConcurrentLinkedQueue<VehicleData> vehicleDataQueue = new ConcurrentLinkedQueue<>();

    private static List<Trip> routesToExecute = new ArrayList<>();
    private static List<Trip> runningRoutes = new ArrayList<>();
    private static List<Trip> completedRoutes = new ArrayList<>();

    private static final double KM_FEE = 3.25d;
    private static BankingService bankingService = new BankingService();

    private static BotPayment botPayment;
    private static UUID accout;

    // private static CyclicBarrier barrier;

    private Socket client;

    public static void loadRoutes(RoutesDTO routePool) {
        for (int i = 0; i < 900; i++) {
            Trip trip = routePool.geTrip(i);
            Route.add(trip.getId(), new StringVector(new String[] { trip.getFrom(), trip.getTo() }));
            routesToExecute.add(trip);
        }
    }

    public static void init() {
        try {
            accout = bankingService.createAccount(100000000d);
        } catch (Exception e) {
            System.out.println("Erro ao criar conta");
            System.exit(1);
        }
        botPayment = new BotPayment(accout);
        botPayment.start();
    }

    // public static void setBarrier(CyclicBarrier barrier) {
    //     MobilityCompany.barrier = barrier;
    // }

    public static Queue<VehicleData> getVehicleDataQueue() {
        return vehicleDataQueue;
    }

    private synchronized Trip sendNextRouteToExecute() {
        if (!routesToExecute.isEmpty()) {
            Trip nextRoute = routesToExecute.remove(0);
            runningRoutes.add(nextRoute);
            return nextRoute;
        } else {
            if (completedRoutes.size() == 900) {

                // barrier.reset();
                this.interrupt();
            }
        }
        return new Trip();
    }

    private synchronized void markRouteAsCompleted(Trip route) {
        System.out.println("Rota " + route.getId() + " finalizada");
        runningRoutes.remove(route);
        completedRoutes.add(route);
    }

    @Override
    public void setSocket(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        while (!this.client.isClosed() && !this.isInterrupted()) {
            try {
                ObjectInputStream data = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                Object request = data.readObject();
                String requestJson = encryptData.decrypt(request.toString());

                MobilityDTO<?> requestDTO = gson.fromJson(requestJson, MobilityDTO.class);
                switch (requestDTO.getAction()) {
                    case REPORT:
                        Type type = new TypeToken<MobilityDTO<VehicleData>>() {
                        }.getType();
                        MobilityDTO<VehicleData> vehicleDataDTO = gson.fromJson(requestJson, type);
                        vehicleDataQueue.add(vehicleDataDTO.getPayload());
                        break;
                    case GET_NEW_ROUTE:
                        Type type2 = new TypeToken<MobilityDTO<Trip>>() {
                        }.getType();
                        MobilityDTO<Trip> oldTrip = gson.fromJson(requestJson, type2);
                        if (oldTrip.getPayload().getId() != null) {
                            markRouteAsCompleted(oldTrip.getPayload());
                        }
                        Trip nextRoute = sendNextRouteToExecute();
                        if (nextRoute.getId() != null) {
                            output.writeObject(encryptData.encrypt(gson.toJson(nextRoute)));
                        } else {
                            output.writeObject(encryptData.encrypt(gson.toJson(new Trip())));
                        }
                        output.flush();
                        break;
                    case PAYMENT_REQUEST:
                        Type type3 = new TypeToken<MobilityDTO<PaymentOrder>>() {
                        }.getType();
                        MobilityDTO<PaymentOrder> paymentOrder = gson.fromJson(requestJson, type3);
                        botPayment.addPayment(paymentOrder.getPayload().getDestinationAccount(),
                                paymentOrder.getPayload().getKmDriven() * KM_FEE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected value: " + requestDTO.getAction());
                }

            } catch (Exception e) {
                break;
            }
        }
        System.out.println("Mobility Company finalizada.");
        try {
            this.client.close();
        } catch (Exception e) {
            System.out.println("Erro ao fechar socket");
        }

        try {
            botPayment.interrupt();
            botPayment.join();
        } catch (InterruptedException e) {
            botPayment.interrupt();
            System.out.println("Erro ao fechar botPayment");
        }
    }
}
