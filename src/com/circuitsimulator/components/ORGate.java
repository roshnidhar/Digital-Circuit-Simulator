package com.circuitsimulator.components;

public class ORGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public ORGate(String id, String name, int inputCount) {
        super(id, name);
        componentTypeId = 11;
        initializeNodes(2, 1);  // 2 inputs, 1 output
        evaluate();  // Set initial output state
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || inputNodes.size() < 2) return;
        if (outputNodes.isEmpty()) return;
        
        // OR outputs 1 when ANY input is 1
        boolean result = evaluateOR(inputNodes);
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "OR";
    }
}
