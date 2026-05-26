package com.circuitsimulator.components;

public class NOTGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public NOTGate(String id, String name) {
        super(id, name);
        componentTypeId = 12;
        initializeNodes(1, 1);  // 1 input, 1 output
        evaluate();  // Set initial output state
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || outputNodes.isEmpty()) return;
        boolean result = evaluateNOT(inputNodes.get(0).getState());
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "NOT";
    }
}
