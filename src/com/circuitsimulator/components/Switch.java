package com.circuitsimulator.components;

import com.circuitsimulator.circuit.Node;

/**
 * Represents an interactive switch (SPST)
 */
public class Switch extends Component {
    private static final long serialVersionUID = 1L;
    
    private boolean isOpen;

    public Switch(String id, String name) {
        super(id, name);
        this.isOpen = true;
        componentTypeId = 1;

        Node inNode = new Node(getNextNodeId());
        inNode.setLabel("Input");
        inputNodes.add(inNode);

        Node outNode = new Node(getNextNodeId());
        outNode.setLabel("Output");
        outputNodes.add(outNode);

        evaluate();
    }

    public void toggle() {
        isOpen = !isOpen;
        evaluate();
    }

    public void setState(boolean state) {
        this.isOpen = !state;
        evaluate();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isClosed() {
        return !isOpen;
    }

    @Override
    public void evaluate() {
        if (!inputNodes.isEmpty() && !outputNodes.isEmpty()) {
            Node input = inputNodes.get(0);
            Node output = outputNodes.get(0);
            if (isOpen) {
                // Open switch - break connection, output is floating (false)
                output.setState(false);
            } else {
                // Closed switch - pass input signal to output
                output.setState(input.getState());
            }
        }
    }

    @Override
    public String getComponentType() {
        return "SWITCH";
    }

    @Override
    public String toString() {
        return super.toString() + " [" + (isOpen ? "OPEN" : "CLOSED") + "]";
    }
}
