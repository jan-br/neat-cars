package de.jan.machinelearning.neat;

import de.jan.machinelearning.neat.core.IGenome;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by DevTastisch on 23.01.2019
 */
public interface INeatEvolver extends Serializable {

    Collection<IGenome> getGenomes();

    IGenome getFittestGenome();



}
