package com.circuitsimulator.components;

import com.circuitsimulator.circuit.Node;
import java.awt.Color;


public class LED extends Component {
    private static final long serialVersionUID = 1L;
    
    public enum LedColor {
        RED(new Color(255, 80, 80)),
        YELLOW(new Color(255, 210, 50)),
        GREEN(new Color(120, 220, 120));

        private final Color color;

        LedColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private static final Color COLOR_OFF = new Color(120, 120, 120);
    private LedColor ledColor;
    private Color currentColor;
    private boolean isLit;

    public LED(String id, String name) {
        super(id, name);
        this.isLit = false;
        this.ledColor = LedColor.RED;
        this.currentColor = COLOR_OFF;
        componentTypeId = 2;
        // Initialize input node with unique ID
        Node inNode = new Node(getNextNodeId());
        inNode.setLabel("Input");
        inputNodes.add(inNode);
        // Set initial state
        evaluate();
    }

    public void addInputNode(Node node) {
        if (inputNodes.size() > 0) {
            inputNodes.set(0, node);
        } else {
            inputNodes.add(node);
        }
    }

    @Override
    public void evaluate() {
        if (!inputNodes.isEmpty()) {
            Node input = inputNodes.get(0);
            isLit = input.getState();
            currentColor = isLit ? ledColor.getColor() : COLOR_OFF;
        } else {
            isLit = false;
            currentColor = COLOR_OFF;
        }
    }

    public boolean isLit() {
        return isLit;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public LedColor getLedColor() {
        return ledColor;
    }

    public void setLedColor(LedColor ledColor) {
        this.ledColor = ledColor;
        evaluate();
    }

    @Override
    public String getComponentType() {
        return "LED";
    }

    @Override
    public String toString() {
        return super.toString() + " [" + (isLit ? "ON" : "OFF") + "]";
    }
}
