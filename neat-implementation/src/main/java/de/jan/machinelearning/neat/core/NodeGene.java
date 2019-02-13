package de.jan.machinelearning.neat.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by DevTastisch on 23.01.2019
 */
public class NodeGene implements INodeGene {

    private static int idCounter;

    private final Set<INodeConnection> nodeConnections = ConcurrentHashMap.newKeySet();

    private final NodeGeneType type;
    private final int id;
    private float bias;
    private float value;

    private NodeGene(NodeGene nodeGene) {
        this.type = nodeGene.type;
        this.id = nodeGene.id;
        this.bias = nodeGene.bias;
//    this.value = nodeGene.value;
        nodeGene.getNodeConnections().forEach(iNodeConnection -> this.nodeConnections.add(iNodeConnection.clone()));
    }

    public NodeGene(NodeGeneType type) {
        this.type = type;
        this.id = idCounter++;
        this.bias = 0.15f;
    }

    public void input(ExecutorService executorService, float value, IGenome genome) {
        this.value += value;
        if (this.value - this.bias > 0) {
            for (INodeConnection nodeConnection : this.nodeConnections) {
                genome.getNodeGeneById(nodeConnection.getOutputNodeGeneId())
                        .input(executorService, (this.getOutput() * nodeConnection.getWeight()) / 10f, genome);
            }
        }
    }

    public NodeGeneType getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public float getOutput() {
        return this.sigmoid(this.value);
    }

    private float sigmoid(float x) {
        return (float) (1 / (1 + Math.exp(-x)));
    }

    public void addConnection(INodeGene targetNode, float weight) {

        this.nodeConnections.stream()
                .filter(iNodeConnection -> iNodeConnection.getOutputNodeGeneId() == targetNode.getId())
                .forEach(this.nodeConnections::remove);
        this.nodeConnections.add(new NodeConnection(this.getId(), targetNode.getId(), weight, true));
    }

    public void reset() {
        this.value = 0;
    }

    public INodeGene clone() {
        return new NodeGene(this);
    }

    public Set<INodeConnection> getNodeConnections() {
        return nodeConnections;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        NodeGene.idCounter = idCounter;
    }
}
