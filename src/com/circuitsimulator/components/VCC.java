package com.circuitsimulator.components;

import com.circuitsimulator.circuit.Node;


public class VCC extends Component {
    private static final long serialVersionUID = 1L;

    public VCC(String id, String name) {
        super(id, name);
        componentTypeId = 10;
        Node outNode = new Node(getNextNodeId());
        outNode.setLabel("VCC_Out");
        outputNodes.add(outNode);
        evaluate();
    }

    @Override
    public void evaluate() {
        if (!outputNodes.isEmpty()) {
            outputNodes.get(0).setState(true);
        }
    }

    @Override
    public String getComponentType() {
        return "VCC";
    }
}
