package com.circuitsimulator.components;

public class XORGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public XORGate(String id, String name, int inputCount) {
        super(id, name);
        componentTypeId = 13;
        initializeNodes(2, 1);  // 2 inputs, 1 output
        evaluate();  // Set initial output state
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || inputNodes.size() < 2) return;
        if (outputNodes.isEmpty()) return;
        
        // XOR outputs 1 when inputs are different
        boolean result = evaluateXOR(inputNodes);
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "XOR";
    }
}
