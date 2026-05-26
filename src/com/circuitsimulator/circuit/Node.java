package com.circuitsimulator.circuit;

import java.io.Serializable;

/**
 * Represents a node (pin) in the circuit with a unique ID
 */
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private int nodeNumber;
    private boolean state; // true = 1 (HIGH/VCC), false = 0 (LOW/GND)
    private String label;

    public Node(int nodeNumber) {
        this.nodeNumber = nodeNumber;
        this.state = false; // default to LOW
        this.label = "Node_" + nodeNumber;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + " (" + nodeNumber + ") = " + (state ? "1" : "0");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.nodeNumber == other.nodeNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(nodeNumber);
    }
}
