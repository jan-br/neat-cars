package de.jan.machinelearning.neat;

import com.google.common.util.concurrent.ListeningExecutorService;
import de.jan.machinelearning.neat.core.Genome;
import de.jan.machinelearning.neat.core.IGenome;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Created by DevTastisch on 26.01.2019
 */
public class NeatRunner {

    private final ListeningExecutorService executorService;
    private final List<IGenome> genomes = new CopyOnWriteArrayList<>();
    private final Consumer<IGenome> consumer;

    public NeatRunner(ListeningExecutorService executorService, File genomeDb, Consumer<IGenome> consumer) {
        this.executorService = executorService;
        this.consumer = consumer;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(genomeDb));
            Genome genome = (Genome) objectInputStream.readObject();
            this.genomes.add(genome);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public NeatRunner(IGenome genome, ListeningExecutorService executorService, Consumer<IGenome> consumer) {
        this.executorService = executorService;
        this.consumer = consumer;
        this.genomes.add(genome);
    }

    public void run(){
        try {
            executorService.submit(() -> this.consumer.accept(this.genomes.get(0))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
