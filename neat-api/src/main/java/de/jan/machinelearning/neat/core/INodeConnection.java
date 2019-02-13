package de.jan.machinelearning.neat.core;

import java.io.Serializable;

/**
 * Created by DevTastisch on 23.01.2019
 */
public interface INodeConnection extends Serializable {

    int getId();

    int getInputNodeGeneId();

    int getOutputNodeGeneId();

    float getWeight();

    INodeConnection clone();

    boolean isEnabled();

    void setWeight(float weight);

    void disable();



}
