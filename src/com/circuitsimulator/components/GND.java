package com.circuitsimulator.components;

import com.circuitsimulator.circuit.Node;


public class GND extends Component {
    private static final long serialVersionUID = 1L;

    public GND(String id, String name) {
        super(id, name);
        componentTypeId = 11;
        Node outNode = new Node(getNextNodeId());
        outNode.setLabel("GND_Out");
        outputNodes.add(outNode);
        evaluate();
    }

    @Override
    public void evaluate() {
        if (!outputNodes.isEmpty()) {
            outputNodes.get(0).setState(false);
        }
    }

    @Override
    public String getComponentType() {
        return "GND";
    }
}
