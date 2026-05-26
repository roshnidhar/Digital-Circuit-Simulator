package com.circuitsimulator.components;

public class ANDGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public ANDGate(String id, String name, int inputCount) {
        super(id, name);
        componentTypeId = 10;
        
        
        initializeNodes(2, 1);  // 2 inputs, 1 output
        evaluate();  // Set initial output state
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || inputNodes.size() < 2) return;
        if (outputNodes.isEmpty()) return;
        
        // AND outputs 1 only when ALL inputs are 1
        boolean result = evaluateAND(inputNodes);
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "AND";
    }
}
