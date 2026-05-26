package com.circuitsimulator.circuit;

import java.io.Serializable;


public class Connection implements Serializable {
    private static final long serialVersionUID = 1L;
    private Node fromNode;
    private Node toNode;
    private double resistance; // default 0, detecting 0 means short circuit

    public Connection(Node fromNode, Node toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.resistance = 0; // ideal connection
    }

    public Connection(Node fromNode, Node toNode, double resistance) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.resistance = resistance;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public boolean isShortCircuit() {
        return resistance == 0;
    }

    @Override
    public String toString() {
        return fromNode.getLabel() + " -> " + toNode.getLabel();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Connection)) return false;
        Connection other = (Connection) obj;
        return this.fromNode.equals(other.fromNode) && this.toNode.equals(other.toNode);
    }

    @Override
    public int hashCode() {
        return (fromNode.hashCode() * 31) + toNode.hashCode();
    }
}
