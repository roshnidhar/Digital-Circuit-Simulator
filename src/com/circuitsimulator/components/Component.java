package com.circuitsimulator.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class Component implements Serializable {
    private static final long serialVersionUID = 1L;
    
   
    private static int nextNodeId = 10000;
    
    protected String id;
    protected String name;
    protected int xPos, yPos;
    protected List<com.circuitsimulator.circuit.Node> inputNodes;
    protected List<com.circuitsimulator.circuit.Node> outputNodes;
    protected int componentTypeId;

    public Component(String id, String name) {
        this.id = id;
        this.name = name;
        this.xPos = 0;
        this.yPos = 0;
        this.inputNodes = new ArrayList<>();
        this.outputNodes = new ArrayList<>();
    }
    
    
    protected static synchronized int getNextNodeId() {
        return nextNodeId++;
    }

    public abstract void evaluate();
    public abstract String getComponentType();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setPosition(int x, int y) {
        this.xPos = x;
        this.yPos = y;
    }

    public List<com.circuitsimulator.circuit.Node> getInputNodes() {
        return inputNodes;
    }

    public List<com.circuitsimulator.circuit.Node> getOutputNodes() {
        return outputNodes;
    }

    public void addInputNode(com.circuitsimulator.circuit.Node node) {
        if (!inputNodes.contains(node)) {
            inputNodes.add(node);
        }
    }

    public void addOutputNode(com.circuitsimulator.circuit.Node node) {
        if (!outputNodes.contains(node)) {
            outputNodes.add(node);
        }
    }

    public com.circuitsimulator.circuit.Node getInputNode(int index) {
        return index >= 0 && index < inputNodes.size() ? inputNodes.get(index) : null;
    }

    public com.circuitsimulator.circuit.Node getOutputNode(int index) {
        return index >= 0 && index < outputNodes.size() ? outputNodes.get(index) : null;
    }

    public int getInputCount() {
        return inputNodes.size();
    }

    public int getOutputCount() {
        return outputNodes.size();
    }

    @Override
    public String toString() {
        return name + " (" + getComponentType() + ")";
    }
}
