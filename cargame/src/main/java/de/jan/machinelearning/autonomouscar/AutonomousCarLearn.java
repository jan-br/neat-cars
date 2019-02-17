package de.jan.machinelearning.autonomouscar;

import com.google.common.util.concurrent.ListeningExecutorService;
import de.jan.machinelearning.autonomouscar.control.VehicleControlKey;
import de.jan.machinelearning.autonomouscar.render.GamePanel;
import de.jan.machinelearning.autonomouscar.render.Physics2D;
import de.jan.machinelearning.autonomouscar.render.Renderable;
import de.jan.machinelearning.neat.NeatEvolver;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by DevTastisch on 24.01.2019
 */
public class AutonomousCarLearn extends JFrame {

    private final File dbFile;
    private final GamePanel gamePanel;
    private final ListeningExecutorService executorService;

    public AutonomousCarLearn(File dbFile, BufferedImage bufferedImage, ListeningExecutorService executorService) {
        super("Autonomous Cars");
        this.dbFile = dbFile;
        this.gamePanel = new GamePanel(bufferedImage, this);
        this.executorService = executorService;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.setContentPane(this.gamePanel);
        this.setVisible(true);
        this.initMainTick();
        this.initNeat();
    }

    private void initNeat() {
        NeatEvolver neatEvolver = new NeatEvolver<>(this.dbFile, 3, 2, executorService,
                () -> ((GamePanel) this.getContentPane()).spawnVehicle(),

                (vehicle, iGenome, id) -> {
                    if (!vehicle.isDead()) {
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

                        vehicle.getVehicleController().updateKey(VehicleControlKey.LEFT, request[1] * 3);
                        vehicle.getVehicleController().updateKey(VehicleControlKey.RIGHT, request[0] * 3);
                        return -1f;
                    }
                    return vehicle.getScore();
                });

        new Timer().schedule(new TimerTask() {
            public void run() {
                neatEvolver.evolve();
            }
        }, 200, 1000);
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
        new Timer().schedule(new TimerTask() {
            public void run() {
                repaint();
            }
        }, 0, 1000 / 24);

    }
}
