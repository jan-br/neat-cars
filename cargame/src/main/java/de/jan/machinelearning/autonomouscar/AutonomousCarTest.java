package de.jan.machinelearning.autonomouscar;

import com.google.common.util.concurrent.ListeningExecutorService;
import de.jan.machinelearning.autonomouscar.control.VehicleControlKey;
import de.jan.machinelearning.autonomouscar.render.GamePanel;
import de.jan.machinelearning.autonomouscar.render.Physics2D;
import de.jan.machinelearning.autonomouscar.render.Renderable;
import de.jan.machinelearning.autonomouscar.render.model.Vehicle;
import de.jan.machinelearning.neat.NeatRunner;
import de.jan.machinelearning.neat.core.IGenome;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by DevTastisch on 26.01.2019
 */
@SuppressWarnings("ALL")
public class AutonomousCarTest extends JFrame {


    private final File dbFile;
    private final GamePanel gamePanel;
    private final ListeningExecutorService executorService;

    public AutonomousCarTest(File dbFile, BufferedImage bufferedImage, ListeningExecutorService executorService) {
        super("Autonomous Cars");
        this.executorService = executorService;
        this.dbFile = dbFile;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.gamePanel = new GamePanel(bufferedImage, this);
        this.setContentPane(this.gamePanel);
        this.setVisible(true);
        this.initMainTick();
        this.initNeat();
    }

    private void initMainTick() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                for (Renderable renderable : ((GamePanel) getContentPane()).getRenderables().stream()
                        .filter(renderable -> renderable instanceof Physics2D)
                        .collect(Collectors.toList())) {
                    ((Physics2D) renderable).tick();
                }
            }
        }, 0, 40);

    }

    private void initNeat() {
        NeatRunner neatRunner = new NeatRunner(executorService, this.dbFile, iGenome -> {
           this.neatLoop(iGenome, ((GamePanel) this.getContentPane()).spawnVehicle());
        });
        this.runnerLoop(neatRunner);
    }

    private void runnerLoop(NeatRunner neatRunner){
        neatRunner.run();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.runnerLoop(neatRunner);
    }

    private void neatLoop(IGenome iGenome, Vehicle vehicle){
        if(vehicle.isDead()) return;
        float completeFront = (float) (vehicle.getEnvInputs()[0] + vehicle.getEnvInputs()[1]);
        float correctionFront = (float) (vehicle.getEnvInputs()[0] - vehicle.getEnvInputs()[1]);
        float resultFront = correctionFront / completeFront;

        float completeBack = (float) (vehicle.getEnvInputs()[2] + vehicle.getEnvInputs()[3]);
        float correctionBack = (float) (vehicle.getEnvInputs()[2] - vehicle.getEnvInputs()[3]);
        float resultBack = correctionBack / completeBack;


        float[] request = iGenome.request(
                this.executorService,
                resultBack,
                resultFront,
                ((float) vehicle.getEnvInputs()[4])
        );
        vehicle.getVehicleController().updateKey(VehicleControlKey.FORWARD, 1.0f);

        vehicle.getVehicleController().updateKey(VehicleControlKey.LEFT, request[1] * 4);
        vehicle.getVehicleController().updateKey(VehicleControlKey.RIGHT, request[0] * 4);
        this.neatLoop(iGenome, vehicle);
    }

}
