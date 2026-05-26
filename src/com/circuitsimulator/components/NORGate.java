package com.circuitsimulator.components;

public class NORGate extends LogicGate {
    private static final long serialVersionUID = 1L;

    public NORGate(String id, String name, int inputCount) {
        super(id, name);
        componentTypeId = 15;
        initializeNodes(2, 1);  // 2 inputs, 1 output
        evaluate();  
    }

    @Override
    public void evaluate() {
        if (inputNodes.isEmpty() || inputNodes.size() < 2) return;
        if (outputNodes.isEmpty()) return;
        
        // NOR outputs 1 only when ALL inputs are 0
        boolean result = evaluateNOR(inputNodes);
        outputNodes.get(0).setState(result);
    }

    @Override
    public String getComponentType() {
        return "NOR";
    }
}
