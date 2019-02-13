package de.jan.machinelearning.neat.core;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** Created by DevTastisch on 23.01.2019 */
public interface IGenome extends Serializable {

  float[] request(ListeningExecutorService executorService, float... inputs);

  float getScore();

  void setScore(float socre);

  void addNodeGene(INodeGene nodeGene);

  void mutation();

  void addConnectionMutation(int maxAttempts);

  void addNodeMutation();

  IGenome clone();

  List<INodeGene> getNodeGenes();

  List<INodeGene> getNodeGenes(ListeningExecutorService executorService, NodeGeneType nodeGeneType);

  INodeGene getNodeGeneById(int id);

}
