package com.circuitsimulator.components;

import com.circuitsimulator.circuit.Node;
import java.util.List;


public abstract class LogicGate extends Component {
    private static final long serialVersionUID = 1L;

    public LogicGate(String id, String name) {
        super(id, name);
    }
    
    protected void initializeNodes(int inputCount, int outputCount) {
        
        for (int i = 0; i < inputCount; i++) {
            Node inNode = new Node(getNextNodeId());
            inNode.setLabel("In_" + i);
            inputNodes.add(inNode);
        }
        for (int i = 0; i < outputCount; i++) {
            Node outNode = new Node(getNextNodeId());
            outNode.setLabel("Out_" + i);
            outputNodes.add(outNode);
        }
    }

    protected boolean evaluateAND(List<Node> inputs) {
        for (Node node : inputs) {
            if (!node.getState()) return false;
        }
        return true;
    }

    protected boolean evaluateOR(List<Node> inputs) {
        for (Node node : inputs) {
            if (node.getState()) return true;
        }
        return false;
    }

    protected boolean evaluateNOT(boolean input) {
        return !input;
    }

    protected boolean evaluateXOR(List<Node> inputs) {
        int count = 0;
        for (Node node : inputs) {
            if (node.getState()) count++;
        }
        return count % 2 == 1;
    }

    protected boolean evaluateNAND(List<Node> inputs) {
        // NAND = NOT(AND) - outputs 0 only when ALL inputs are 1
        if (inputs.isEmpty()) return true; // Safety: no inputs = output 1
        for (Node node : inputs) {
            if (!node.getState()) return true; // If any input is 0, output is 1
        }
        return false; // All inputs are 1, so output is 0
    }

    protected boolean evaluateNOR(List<Node> inputs) {
        // NOR = NOT(OR) - outputs 1 only when ALL inputs are 0
        if (inputs.isEmpty()) return true; // Safety: no inputs = output 1
        for (Node node : inputs) {
            if (node.getState()) return false; // If any input is 1, output is 0
        }
        return true; // All inputs are 0, so output is 1
    }

    protected boolean evaluateXNOR(List<Node> inputs) {
        return !evaluateXOR(inputs);
    }
}
