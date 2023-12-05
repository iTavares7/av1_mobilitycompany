package br.com.av1.sumo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import br.com.av1.sumo.model.FuelStation;
import br.com.av1.sumo.model.banking.AlphaBank;
import br.com.av1.sumo.model.mobility.Driver;
import br.com.av1.sumo.model.mobility.MobilityCompany;
import br.com.av1.sumo.model.mobility.RoutesDTO;
import br.com.av1.sumo.model.mobility.testdrive.Routes;
import br.com.av1.sumo.model.mobility.testdrive.TestDrive;
import br.com.av1.sumo.services.BankingService;
import br.com.av1.sumo.util.RunnerInterface;
import br.com.av1.sumo.util.ServerRunner;

import org.eclipse.sumo.libtraci.Simulation;

import org.eclipse.sumo.libtraci.StringVector;

public class Application {

    private static final int N_DRIVERS = 0;

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

        Routes routes = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Routes.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            routes = (Routes) unmarshaller.unmarshal(new File("./map/single.rou.xml"));
        } catch (JAXBException e) {
            System.out.println("Falha ao carregar rotas.");
            System.exit(1);
        }

        int testDrivers = 0;

        if (routes != null) {
            testDrivers = routes.vehicles.size();
        }

        int alphaPort = 12345;
        int mobilityPort = 54321;

        ServerRunner<RunnerInterface> alphaBankServer = null;
        ServerRunner<RunnerInterface> mobilityCompany = null;
        FuelStation fuelStation = null;

        try {
            alphaBankServer = new ServerRunner<>(alphaPort, new AlphaBank());
            alphaBankServer.start();
            BankingService.setBankServicePort(alphaPort);

            MobilityCompany.init();

            mobilityCompany = new ServerRunner<>(mobilityPort, new MobilityCompany());
            mobilityCompany.start();

            fuelStation = FuelStation.getInstance();
            fuelStation.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar serviços.");
            System.exit(1);
        }

        List<Driver> carpool = new ArrayList<>();
        List<TestDrive> testDriverPool = new ArrayList<>();

        AtomicBoolean controler = new AtomicBoolean(true);

        CyclicBarrier simulationBarrier = new CyclicBarrier(N_DRIVERS * 2 + testDrivers + 1);
        CyclicBarrier threadBarrier = new CyclicBarrier(N_DRIVERS * 2 + testDrivers + 1);

        for (int i = 0; i < N_DRIVERS; i++) {
            Driver driver = new Driver(mobilityPort, threadBarrier, simulationBarrier);
            carpool.add(driver);
            driver.start();
        }

        for (int i = 0; i < testDrivers; i++) {
            TestDrive testDrive = new TestDrive(routes.vehicles.get(i), threadBarrier, simulationBarrier);
            testDriverPool.add(testDrive);
            testDrive.start();
        }

        while (controler.get()) {
            testDriverPool.forEach(testDrive -> {
                if (testDrive.getState().equals(Thread.State.TERMINATED)) {
                    new Thread(() -> {
                        try {
                            threadBarrier.await();
                            simulationBarrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });

            carpool.forEach(driver -> {
                if (driver.getState().equals(Thread.State.TERMINATED)) {
                    new Thread(() -> {
                        try {
                            threadBarrier.await();
                            simulationBarrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });

            try {
                threadBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Simulation.step();
            new Thread(() -> {
                try {
                    Thread.sleep(25);
                    simulationBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            if (testDriverPool.stream().allMatch(testDrive -> testDrive.getState().equals(Thread.State.TERMINATED))
                    && carpool.stream().allMatch(driver -> driver.getState().equals(Thread.State.TERMINATED))) {
                controler.set(false);
            }
        }

        try {
            fuelStation.interrupt();
            fuelStation.join();
            mobilityCompany.interrupt();
            mobilityCompany.join();
            alphaBankServer.interrupt();
            alphaBankServer.join();

            Simulation.close();
            System.out.println("Simulação encerrada.");

        } catch (Exception e) {
            System.out.println("Error ao finalizar threads: " + e.getMessage());
            System.exit(1);
        }
    }
}
