package de.jan.machinelearning.autonomouscar;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.jan.machinelearning.autonomouscar.dialog.ChooseFileDialog;
import de.jan.machinelearning.autonomouscar.terminal.Form;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by DevTastisch on 29.01.2019
 */
public class Launcher {

    private final Set<Form> forms = ConcurrentHashMap.newKeySet();
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

    public Launcher() throws IOException {
        Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            System.out.println(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toString());
            String filename = Launcher.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -Xmx300m -Xss1g -jar \"" + filename + "\" && PAUSE && exit"});
        } else {
            this.init();
        }
    }

    private Form createForm(String question, int defaultId) {
        Form form = new Form(question, defaultId);
        this.forms.add(form);
        return form;
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        this.selectBackground(bufferedImage -> {
            this.selectDatabase(databaseFile -> {
                this.selectMode(mode -> {
                    switch (mode) {
                        case DRY:
                            this.drive(bufferedImage, databaseFile);
                            break;
                        case LEARN:
                            this.learn(bufferedImage, databaseFile);
                            break;
                    }
                });
            });
        });
    }

    private BufferedImage unsafeReadIO(File file) {
        try {
            return this.unsafeReadIO(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedImage unsafeReadIO(InputStream inputStream) {
        try {
            return ImageIO.read(Objects.requireNonNull(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void selectMode(Consumer<Mode> modeConsumer) {
        this.createForm("Machine Learning - Select mode", 1)
                .addEntry("Learn mode - Start or continue training a neural network with NEAT", () -> modeConsumer.accept(Mode.LEARN))
                .addEntry("Drive mode - Run neural network from trained network.", () -> modeConsumer.accept(Mode.DRY))
                .print(System.out);
    }

    private void selectBackground(Consumer<BufferedImage> bufferedImageConsumer) {
        this.createForm("Select Background image", 1)
                .addEntry("Default", () -> bufferedImageConsumer.accept(this.unsafeReadIO(Launcher.class.getClassLoader().getResourceAsStream("track.jpg"))))
                .addEntry("Custom", () -> new ChooseFileDialog(new String[]{"jpg"}, "*.jpg", "Select Background Image",
                        backgroundImage -> bufferedImageConsumer.accept(this.unsafeReadIO(backgroundImage)),
                        backgroundImage -> System.err.println("Failed. Please restart Launcher")))
                .print(System.out);
    }

    private void selectDatabase(Consumer<File> fileConsumer) {
        this.createForm("Select or create Genome database?", 2)
                .addEntry("Select existing database", () -> new ChooseFileDialog(new String[]{"db"}, "*.db", "Choose Database",
                        fileConsumer,
                        file -> System.err.println("Failed. Please restart Launcher")))
                .addEntry("Create new database", () -> new ChooseFileDialog(new String[]{"db"}, "*.db", "Choose Database Location",
                        fileConsumer,
                        file -> System.err.println("Failed. Please restart Launcher")))
                .print(System.out);
    }

    private void drive(BufferedImage bufferedImage, File databaseFile){
        new AutonomousCarTest(databaseFile, bufferedImage, executorService);
    }

    private void learn(BufferedImage bufferedImage, File databaseFile) {
        new AutonomousCarLearn(databaseFile, bufferedImage, executorService);
    }

    public static void main(String[] args) throws IOException {
        new Launcher();
    }

}
