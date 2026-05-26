package com.circuitsimulator.components;

public class XNORGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public XNORGate(String id, String name, int inputCount) {
        super(id, name);
        componentTypeId = 16;
        initializeNodes(2, 1);  // 2 inputs, 1 output
        evaluate();  // Set initial output state
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || inputNodes.size() < 2) return;
        if (outputNodes.isEmpty()) return;
        
        // XNOR outputs 1 when inputs are same
        boolean result = evaluateXNOR(inputNodes);
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "XNOR";
    }
}
