package com.circuitsimulator.circuit;

import com.circuitsimulator.components.*;
import java.io.Serializable;
import java.util.*;


public class Circuit implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String circuitName;
    private List<Component> components;
    private List<Connection> connections;
    private Map<Integer, Node> nodesMap; // Map of node number to Node object

    public Circuit(String name) {
        this.circuitName = name;
        this.components = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.nodesMap = new HashMap<>();
    }

    
    public Node createNode(int nodeNumber) {
        if (nodesMap.containsKey(nodeNumber)) {
            return nodesMap.get(nodeNumber);
        }
        Node node = new Node(nodeNumber);
        nodesMap.put(nodeNumber, node);
        return node;
    }

    
    public Node getNode(int nodeNumber) {
        return nodesMap.get(nodeNumber);
    }

    
    public void addComponent(Component component) {
        if (!components.contains(component)) {
            components.add(component);
        }
    }

    
    public void removeComponent(Component component) {
        components.remove(component);
        // Remove connections related to this component
        connections.removeIf(conn -> 
            isNodeBelongingToComponent(conn.getFromNode(), component) ||
            isNodeBelongingToComponent(conn.getToNode(), component)
        );
    }

    
    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }

    
    public void connect(Node fromNode, Node toNode) {
        connect(fromNode, toNode, 0); // default resistance 0
    }

    public void connect(Node fromNode, Node toNode, double resistance) {
        Connection conn = new Connection(fromNode, toNode, resistance);
        if (!connections.contains(conn)) {
            connections.add(conn);
        }
    }

   
    public void simulate() {
        
        resetAllNodesToLow();

        
        // 1) Constant sources drive their outputs
        for (Component comp : components) {
            if (comp instanceof VCC || comp instanceof GND) {
                comp.evaluate();
            }
        }
        propagateSignals();

        // 2) Switch outputs depend on their input pins
        for (Component comp : components) {
            if (comp instanceof Switch) {
                comp.evaluate();
            }
        }
        propagateSignals();

        // 3) Gates compute outputs from their inputs
        for (Component comp : components) {
            if (comp instanceof LogicGate) {
                comp.evaluate();
            }
        }
        propagateSignals();

        // 4) Outputs (LEDs) evaluate from their input pins
        for (Component comp : components) {
            if (comp instanceof LED) {
                comp.evaluate();
            }
        }
    }

    private void resetAllNodesToLow() {
        
        Set<Node> all = new HashSet<>();

        for (Component comp : components) {
            all.addAll(comp.getInputNodes());
            all.addAll(comp.getOutputNodes());
        }
        for (Connection conn : connections) {
            if (conn.getFromNode() != null) all.add(conn.getFromNode());
            if (conn.getToNode() != null) all.add(conn.getToNode());
        }
        for (Node node : all) {
            if (node != null) {
                node.setState(false);
            }
        }
    }
    
    private void propagateSignals() {
        
        for (Connection conn : connections) {
            Node sourceNode = conn.getFromNode();
            Node destNode = conn.getToNode();
            // Propagate the state from source to destination
            destNode.setState(sourceNode.getState());
        }
    }
    
    private void evaluateAllComponents() {
        
        for (Component comp : components) {
            if (comp instanceof VCC || comp instanceof GND) {
                comp.evaluate();
            }
        }
        
        // Evaluate switches
        for (Component comp : components) {
            if (comp instanceof Switch) {
                comp.evaluate();
            }
        }
        
        // Evaluate gates
        for (Component comp : components) {
            if (comp instanceof LogicGate) {
                comp.evaluate();
            }
        }
        
        // Evaluate outputs (LEDs)
        for (Component comp : components) {
            if (comp instanceof LED) {
                comp.evaluate();
            }
        }
    }


    public List<Node> getInputNodes() {
        List<Node> inputs = new ArrayList<>();
        for (Component comp : components) {
            if (comp instanceof Switch) {
                List<Node> outputNodes = comp.getOutputNodes();
                if (!outputNodes.isEmpty()) {
                    inputs.add(outputNodes.get(0));
                }
            }
        }
        return inputs;
    }

    
    public List<Node> getOutputNodes() {
        List<Node> outputs = new ArrayList<>();
        for (Component comp : components) {
            if (comp instanceof LED) {
                List<Node> inputNodes = comp.getInputNodes();
                if (!inputNodes.isEmpty()) {
                    outputs.add(inputNodes.get(0));
                }
            }
        }
        return outputs;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String name) {
        this.circuitName = name;
    }

    public List<Component> getComponents() {
        return new ArrayList<>(components);
    }

    public List<Connection> getConnections() {
        return new ArrayList<>(connections);
    }

    public Map<Integer, Node> getAllNodes() {
        return new HashMap<>(nodesMap);
    }

    public int getTotalNodes() {
        return nodesMap.size();
    }

    public int getTotalComponents() {
        return components.size();
    }

    public void clear() {
        components.clear();
        connections.clear();
        nodesMap.clear();
    }

    private boolean isNodeBelongingToComponent(Node node, Component comp) {
        return comp.getInputNodes().contains(node) || comp.getOutputNodes().contains(node);
    }

    @Override
    public String toString() {
        return "Circuit[" + circuitName + ": " + components.size() + " components, " + 
               nodesMap.size() + " nodes]";
    }
}
