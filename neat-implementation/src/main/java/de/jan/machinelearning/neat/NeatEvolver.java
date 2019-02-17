package de.jan.machinelearning.neat;

import com.google.common.util.concurrent.ListeningExecutorService;
import de.jan.machinelearning.neat.core.*;
import de.jan.machinelearning.neat.util.TriFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorCompletionService;
import java.util.stream.Collectors;

/**
 * Created by DevTastisch on 23.01.2019
 */
public class NeatEvolver<G> implements INeatEvolver {

    private final ListeningExecutorService executorService;
    private final CompletionService<Object> completionService;
    private final List<IGenome> genomes = new CopyOnWriteArrayList<>();
    private final Callable<G> spawnGenomeModel;
    private final TriFunction<G, IGenome, Integer, Float> scoreFunction;
    private final Random random = new Random();

    private final File genomeDb;
    private final int populationSize = 100;
    private final float mutationRate = 0.3f;
    private final float addConnectionRate = 0.3f;
    private final float addNodeRate = 0.3f;

    private int counter;

    public NeatEvolver(File genomeDb, int inputNodes, int outputNodes, ListeningExecutorService executorService, Callable<G> spawnGenomeModel, TriFunction<G, IGenome, Integer, Float> scoreFunction) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        this.executorService = executorService;
        this.completionService = new ExecutorCompletionService<>(executorService);
        this.spawnGenomeModel = spawnGenomeModel;
        this.scoreFunction = scoreFunction;
        this.genomeDb = genomeDb;

        Genome genome = new Genome(this.executorService, inputNodes, outputNodes);

        if (genomeDb.exists()) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(genomeDb));
                genome = (Genome) objectInputStream.readObject();
                NodeGene.setIdCounter(objectInputStream.readInt());
                NodeConnection.setIdCounter(objectInputStream.readInt());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < this.populationSize; i++) {
            this.genomes.add(genome.clone());
        }
        this.addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> GenomePrinter.printGenome(this.getFittestGenome(), "./out/latest.png", this.genomeDb)));
    }

    public List<IGenome> getGenomes() {
        return this.genomes.stream()
                .sorted((one, two) -> Float.compare(two.getScore(), one.getScore()))
                .collect(Collectors.toList());
    }

    public IGenome getFittestGenome() {
        return this.genomes.stream().max(Comparator.comparing(IGenome::getScore)).orElse(null);
    }

    public void evolve() {

        CopyOnWriteArrayList<Map.Entry<Integer, Map.Entry<G, IGenome>>> genomeList = new CopyOnWriteArrayList<>();

        for (int i = 0; i < this.genomes.size(); i++) {
            IGenome iGenome = this.genomes.get(i);
            try {
                genomeList.add(new AbstractMap.SimpleEntry<>(i, new AbstractMap.SimpleEntry<>(this.spawnGenomeModel.call(), iGenome)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while (!genomeList.isEmpty()) {
            for (Map.Entry<Integer, Map.Entry<G, IGenome>> integerEntryEntry : genomeList) {
                int id = integerEntryEntry.getKey();
                Map.Entry<G, IGenome> entry = integerEntryEntry.getValue();
                IGenome genome = entry.getValue();
                G model = entry.getKey();

                float score = this.scoreFunction.apply(model, genome, id);
                if (score != -1) {
                    genomeList.remove(integerEntryEntry);
                    genome.setScore(score);
                }
            }
        }


        IGenome fittestGenome = this.getFittestGenome().clone();
        System.out.println("Highest Score is " + fittestGenome.getScore());
        List<IGenome> nextGenomes = new CopyOnWriteArrayList<>();

        while (nextGenomes.size() < populationSize - 2) {
            IGenome p1 = this.getRandomGenomeBiasedAdjustedFitness();
            IGenome p2 = this.getRandomGenomeBiasedAdjustedFitness();

            Genome child;
            if (p1.getScore() >= p2.getScore()) {
                child = this.crossover(p1, p2);
            } else {
                child = this.crossover(p2, p1);
            }
            if (random.nextFloat() < this.mutationRate) {
                child.mutation();
            }
            if (random.nextFloat() < this.addConnectionRate) {
                child.addConnectionMutation(10);
            }
            if (random.nextFloat() < this.addNodeRate) {
                child.addNodeMutation();
            }
            nextGenomes.add(child);
        }
        for (int i = 0; i < 2; i++) {
            nextGenomes.add(fittestGenome.clone());
        }

        this.genomes.clear();
        this.genomes.addAll(nextGenomes);
        if (counter++ % 10 == 0) {
            GenomePrinter.printGenome(getFittestGenome(), "out/" + (counter) + ".png", this.genomeDb);
        }
    }

    /**
     * @param parent1 More fit parent
     * @param parent2 Less fit parent
     */
    public Genome crossover(IGenome parent1, IGenome parent2) {
        Genome child = new Genome();

        for (INodeGene parent1Node : parent1.getNodeGenes()) {
            child.addNodeGene(parent1Node.clone());
        }

        for (INodeConnection parent1Node :
                parent1.getNodeGenes().stream()
                        .flatMap(nodeGene -> nodeGene.getNodeConnections().stream())
                        .collect(Collectors.toList())) {
            if (parent2.getNodeGenes().stream()
                    .flatMap(nodeGene -> nodeGene.getNodeConnections().stream())
                    .anyMatch(search -> parent1Node.getId() == search.getId())) { // matching gene

                INodeConnection childConGene =
                        this.random.nextBoolean()
                                ? parent1Node.clone()
                                : parent2.getNodeGenes().stream()
                                .flatMap(nodeGene -> nodeGene.getNodeConnections().stream())
                                .filter(search -> search.getId() == parent1Node.getId())
                                .findAny()
                                .get()
                                .clone();

                child
                        .getNodeGeneById(childConGene.getInputNodeGeneId())
                        .addConnection(
                                child.getNodeGeneById(childConGene.getOutputNodeGeneId()),
                                childConGene.getWeight());
            } else { // disjoint or excess gene
                child
                        .getNodeGeneById(parent1Node.getInputNodeGeneId())
                        .addConnection(
                                child.getNodeGeneById(parent1Node.getOutputNodeGeneId()), parent1Node.getWeight());
            }
        }

        return child;
    }

    private IGenome getRandomGenomeBiasedAdjustedFitness() {
        double completeWeight =
                0.0; // sum of probablities of selecting each genome - selection is more probable for
        // genomes with higher fitness
        for (IGenome genome : this.getGenomes()) {
            completeWeight += genome.getScore();
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (IGenome genome : this.getGenomes()) {
            countWeight += genome.getScore();
            if (countWeight >= r) {
                return genome;
            }
        }
        throw new RuntimeException(
                "Couldn't find a genome... Number is genomes in selæected species is "
                        + this.populationSize
                        + ", and the total adjusted fitness is "
                        + completeWeight);
    }

}
