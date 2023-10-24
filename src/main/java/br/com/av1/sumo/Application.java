package br.com.av1.sumo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import br.com.av1.sumo.model.FuelStation;
import br.com.av1.sumo.model.banking.AlphaBank;
import br.com.av1.sumo.model.mobility.Driver;
import br.com.av1.sumo.model.mobility.MobilityCompany;
import br.com.av1.sumo.model.mobility.RoutesDTO;
import br.com.av1.sumo.services.BankingService;
import br.com.av1.sumo.util.RunnerInterface;
import br.com.av1.sumo.util.ServerRunner;

import org.eclipse.sumo.libtraci.Simulation;

import org.eclipse.sumo.libtraci.StringVector;

public class Application {

    private static final int N_DRIVERS = 100;

    public static void main(String[] args) {

        System.loadLibrary("libtracijni");
        Simulation.start(new StringVector(new String[] { "sumo-gui", "-n", "./map/map2.net.xml", "--quit-on-end" }));

        try {
            File file = new File("./map/trips.trips.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(RoutesDTO.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            RoutesDTO routes = (RoutesDTO) jaxbUnmarshaller.unmarshal(file);
            MobilityCompany.loadRoutes(routes);
        } catch (JAXBException e) {
            System.out.println("Falha ao carregar rotas.");
            System.exit(1);
        }
        int alphaPort = 12345;
        int mobilityPort = 54321;
        
        try {
            CyclicBarrier barrier = new CyclicBarrier(N_DRIVERS * 2, () -> {
                Simulation.step();
               // System.out.println("Step");
            });

            // MobilityCompany.setBarrier(barrier);

            ServerRunner<RunnerInterface> alphaBankServer = new ServerRunner<>(alphaPort, new AlphaBank());
            alphaBankServer.start();
            BankingService.setBankServicePort(alphaPort);

            MobilityCompany.init();

            ServerRunner<RunnerInterface> mobilityCompany = new ServerRunner<>(mobilityPort, new MobilityCompany());
            mobilityCompany.start();

            FuelStation fuelStation = FuelStation.getInstance();
            fuelStation.start();

            List<Driver> carpool = new ArrayList<>();

            for (int i = 0; i < N_DRIVERS; i++) {
                Driver driver = new Driver(mobilityPort, barrier);
                driver.start();
                carpool.add(driver);
            }

        
            carpool.stream().forEach(driver -> {    
                try {
                    driver.join();
                } catch (InterruptedException e) {
                    driver.interrupt();
                    System.out.println("Erro ao dar JOIN");
                    e.printStackTrace();
                }
            });


            fuelStation.interrupt();
            fuelStation.join();
            mobilityCompany.interrupt();
            mobilityCompany.join();
            alphaBankServer.interrupt();
            alphaBankServer.join();

            Simulation.close();
            System.out.println("Simulação encerrada.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
