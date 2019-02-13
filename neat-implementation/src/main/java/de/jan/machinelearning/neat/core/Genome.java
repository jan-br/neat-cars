package de.jan.machinelearning.neat.core;

import com.google.common.util.concurrent.ListeningExecutorService;
import de.jan.machinelearning.neat.data.ListHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by DevTastisch on 23.01.2019
 */
public class Genome implements IGenome {

    private final ListHashMap<NodeGeneType, INodeGene> iNodeGenes = new ListHashMap<>();
    private final Random random = new Random();

    private final float PROBABILITY_PERTURBING = 0.09f;
    private float score;

    public Genome() {
    }

    private Genome(Genome genome) {
        genome.getNodeGenes().forEach(iNodeGene -> this.iNodeGenes.add(iNodeGene.getType(), iNodeGene.clone()));
        this.score = genome.score;
    }

    public Genome(ListeningExecutorService executorService, int inputNodes, int outputNodes) {
        for (int i = 0; i < inputNodes; i++) {
            this.iNodeGenes.add(NodeGeneType.INPUT, new NodeGene(NodeGeneType.INPUT));
        }
        for (int i = 0; i < outputNodes; i++) {
            this.iNodeGenes.add(NodeGeneType.OUTPUT, new NodeGene(NodeGeneType.OUTPUT));
        }
        this.getNodeGenes(executorService, NodeGeneType.INPUT)
                .forEach(
                        inputNode ->
                                this.getNodeGenes(executorService, NodeGeneType.OUTPUT)
                                        .forEach(
                                                outputNode -> inputNode.addConnection(outputNode, (float) Math.random())));
    }

    public List<INodeGene> getNodeGenes() {
        return this.iNodeGenes.asList();
    }

    public List<INodeGene> getNodeGenes(ListeningExecutorService executorService, NodeGeneType nodeGeneType) {
        return new LinkedList<>(this.iNodeGenes.get(nodeGeneType));
    }

    public float[] request(ListeningExecutorService executorService, float... inputs) {

        List<INodeGene> inputGenes = this.getNodeGenes(executorService, NodeGeneType.INPUT);
        for (int i = 0; i < inputGenes.size(); i++) {
            inputGenes.get(i).input(executorService, inputs[i], this);
        }

        List<INodeGene> outputGenes = this.getNodeGenes(executorService, NodeGeneType.OUTPUT);
        float[] outputs = new float[outputGenes.size()];

        for (int i = 0; i < outputGenes.size(); i++) {
            outputs[i] = outputGenes.get(i).getOutput();
        }
        this.getNodeGenes().forEach(INodeGene::reset);
        return outputs;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public IGenome clone() {
        return new Genome(this);
    }

    public void addNodeGene(INodeGene nodeGene) {
        this.iNodeGenes.add(nodeGene.getType(), nodeGene);
    }

    public INodeGene getNodeGeneById(int id) {
        return this.iNodeGenes.stream()
                .filter(nodeGene -> nodeGene.getId() == id)
                .findAny()
                .orElse(null);
    }

    public void mutation() {
        for (INodeConnection con :
                this.iNodeGenes.stream()
                        .flatMap(iNodeGene -> iNodeGene.getNodeConnections().stream())
                        .collect(Collectors.toList())) {
            if (this.random.nextFloat() < PROBABILITY_PERTURBING) { // uniformly perturbing weights
                con.setWeight(con.getWeight() * (this.random.nextFloat() * 4f - 2f));
            } else { // assigning new weight
                con.setWeight(this.random.nextFloat() * 4f - 2f);
            }
        }
    }

    public void addNodeMutation() {
        INodeConnection con =
                this.iNodeGenes.stream()
                        .flatMap(iNodeGene -> iNodeGene.getNodeConnections().stream())
                        .findAny()
                        .get();

        INodeGene inNode = this.getNodeGeneById(con.getInputNodeGeneId());
        INodeGene outNode = this.getNodeGeneById(con.getOutputNodeGeneId());

        con.disable();
        this.getNodeGenes().stream().forEach(nodeGene -> nodeGene.getNodeConnections().forEach(iNodeConnection -> {
            if (!iNodeConnection.isEnabled()) {
                nodeGene.getNodeConnections().remove(iNodeConnection);
            }
        }));

        NodeGene newNode = new NodeGene(NodeGeneType.HIDDEN);
        INodeConnection inToNew = new NodeConnection(inNode.getId(), newNode.getId(), this.random.nextFloat() * (this.random.nextBoolean() ? -1 : 1), true);
        INodeConnection newToOut =
                new NodeConnection(newNode.getId(), outNode.getId(), con.getWeight(), true);

        this.iNodeGenes.add(newNode.getType(), newNode);
        this.getNodeGeneById(inToNew.getInputNodeGeneId()).addConnection(this.getNodeGeneById(inToNew.getOutputNodeGeneId()), inToNew.getWeight());
        this.getNodeGeneById(newToOut.getInputNodeGeneId()).addConnection(this.getNodeGeneById(newToOut.getOutputNodeGeneId()), newToOut.getWeight());
    }

    public void addConnectionMutation(int maxAttempts) {
        int tries = 0;
        boolean success = false;
        while (tries < maxAttempts && success == false) {
            tries++;

            INodeGene node1 = this.iNodeGenes.asList().get(this.random.nextInt(this.iNodeGenes.size() - 1));
            INodeGene node2 = this.iNodeGenes.asList().get(this.random.nextInt(this.iNodeGenes.size() - 1));
            float weight = this.random.nextFloat() * (this.random.nextBoolean() ? -1 : 1);

            boolean reversed = false;
            if (node1.getType() == NodeGeneType.HIDDEN && node2.getType() == NodeGeneType.INPUT) {
                reversed = true;
            } else if (node1.getType() == NodeGeneType.OUTPUT && node2.getType() == NodeGeneType.HIDDEN) {
                reversed = true;
            } else if (node1.getType() == NodeGeneType.OUTPUT && node2.getType() == NodeGeneType.INPUT) {
                reversed = true;
            }

            boolean connectionImpossible = false;
            if (node1.getType() == NodeGeneType.INPUT && node2.getType() == NodeGeneType.INPUT) {
                connectionImpossible = true;
            } else if (node1.getType() == NodeGeneType.OUTPUT && node2.getType() == NodeGeneType.OUTPUT) {
                connectionImpossible = true;
            }

            boolean connectionExists = false;
            for (INodeConnection con :
                    this.iNodeGenes.stream()
                            .flatMap(iNodeGene -> iNodeGene.getNodeConnections().stream())
                            .collect(Collectors.toList())) {
                if (con.getInputNodeGeneId() == node1.getId()
                        && con.getOutputNodeGeneId() == node2.getId()) { // existing connection
                    connectionExists = true;
                    break;
                } else if (con.getInputNodeGeneId() == node2.getId()
                        && con.getOutputNodeGeneId() == node1.getId()) { // existing connection
                    connectionExists = true;
                    break;
                }
            }

            if (connectionExists || connectionImpossible) {
                continue;
            }

            this.getNodeGeneById(reversed ? node2.getId() : node1.getId())
                    .addConnection(this.getNodeGeneById(reversed ? node1.getId() : node2.getId()), weight);

            success = true;
        }
    }
}
