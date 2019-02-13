package de.jan.machinelearning.neat.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by DevTastisch on 23.01.2019
 */
public interface INodeGene extends Serializable {

    void input(ExecutorService executorService, float value, IGenome genome);

    NodeGeneType getType();

    int getId();

    float getOutput();

    void addConnection(INodeGene targetNode, float weight);

    void reset();

    Set<INodeConnection> getNodeConnections();

    INodeGene clone();
}
