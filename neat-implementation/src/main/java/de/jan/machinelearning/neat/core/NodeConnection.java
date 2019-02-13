package de.jan.machinelearning.neat.core;

/** Created by DevTastisch on 23.01.2019 */
public class NodeConnection implements INodeConnection {

  private static int idCounter;
  private final int id;
  private final int inputNodeGeneId;
  private final int outputNodeGeneId;
  private float weight;
  private boolean active;

  private NodeConnection(NodeConnection nodeConnection){
    this.id = nodeConnection.id;
    this.inputNodeGeneId = nodeConnection.inputNodeGeneId;
    this.outputNodeGeneId = nodeConnection.outputNodeGeneId;
    this.weight = nodeConnection.weight;
    this.active = nodeConnection.active;
  }

  public NodeConnection(int inputNodeGeneId, int outputNodeGeneId, float weight, boolean active) {
    this.id = idCounter++;
    this.inputNodeGeneId = inputNodeGeneId;
    this.outputNodeGeneId = outputNodeGeneId;
    this.weight = weight;
    this.active = active;
  }

  public int getId() {
    return id;
  }

  public int getInputNodeGeneId() {
    return this.inputNodeGeneId;
  }

  public int getOutputNodeGeneId() {
    return this.outputNodeGeneId;
  }

  public float getWeight() {
    return weight;
  }

  public void setWeight(float weight) {
    this.weight = weight;
  }

  public boolean isEnabled() {
    return this.active;
  }

  public void disable() {
    this.active = false;
  }

  public INodeConnection clone() {
    return new NodeConnection(this);
  }

  public static int getIdCounter() {
    return idCounter;
  }

  public static void setIdCounter(int idCounter) {
    NodeConnection.idCounter = idCounter;
  }
}
