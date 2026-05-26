package com.circuitsimulator.validator;

import com.circuitsimulator.circuit.Circuit;
import com.circuitsimulator.circuit.Connection;
import com.circuitsimulator.circuit.Node;
import com.circuitsimulator.components.*;
import java.util.*;

public class CircuitValidator {
    private Circuit circuit;
    private List<String> errors;
    private List<String> warnings;

    public CircuitValidator(Circuit circuit) {
        this.circuit = circuit;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    
    public boolean validate() {
        errors.clear();
        warnings.clear();

        validateComponents();
        validateConnections();
        validateShortCircuits();
        validateDataFlow();

        return errors.isEmpty();
    }

    
    private void validateComponents() {
        for (Component comp : circuit.getComponents()) {
            String type = comp.getComponentType();

            if (comp instanceof Switch) {
                if (comp.getOutputNodes().isEmpty()) {
                    warnings.add("Switch " + comp.getName() + " has no output connection");
                }
            } else if (comp instanceof LED) {
                if (comp.getInputNodes().isEmpty()) {
                    warnings.add("LED " + comp.getName() + " has no input connection");
                }
            } else if (comp instanceof NOTGate) {
                if (comp.getInputNodes().isEmpty()) {
                    errors.add("NOT Gate " + comp.getName() + " missing input");
                }
                if (comp.getOutputNodes().isEmpty()) {
                    errors.add("NOT Gate " + comp.getName() + " missing output");
                }
            } else if (comp instanceof LogicGate) {
                int inputCount = comp.getInputNodes().size();
                if (inputCount < 2) {
                    errors.add(type + " Gate " + comp.getName() + " needs at least 2 inputs");
                }
                if (comp.getOutputNodes().isEmpty()) {
                    errors.add(type + " Gate " + comp.getName() + " missing output");
                }
            }
        }
    }


    private void validateConnections() {
        Set<Integer> seenNodes = new HashSet<>();

        for (Connection conn : circuit.getConnections()) {
            if (conn.getFromNode() == null || conn.getToNode() == null) {
                errors.add("Connection has null nodes");
            }
            seenNodes.add(conn.getFromNode().getNodeNumber());
            seenNodes.add(conn.getToNode().getNodeNumber());
        }

        // Check for unconnected nodes
        Map<Integer, Node> allNodes = circuit.getAllNodes();
        for (Integer nodeNum : allNodes.keySet()) {
            if (!seenNodes.contains(nodeNum)) {
                warnings.add("Node " + nodeNum + " is not connected");
            }
        }
    }

    private void validateShortCircuits() {
        for (Connection conn : circuit.getConnections()) {
            if (conn.isShortCircuit()) {
                Node from = conn.getFromNode();
                Node to = conn.getToNode();
                if (from.getState() && !to.getState()) {
                    errors.add("SHORT CIRCUIT: " + from.getLabel() + 
                             " (HIGH) -> " + to.getLabel() + " (LOW)");
                }
            }
        }

        detectDirectVccToGndShort();
    }

    private void detectDirectVccToGndShort() {
        List<Node> vccNodes = new ArrayList<>();
        List<Node> gndNodes = new ArrayList<>();

        for (Component comp : circuit.getComponents()) {
            if (comp instanceof VCC) {
                vccNodes.addAll(comp.getOutputNodes());
            } else if (comp instanceof GND) {
                gndNodes.addAll(comp.getOutputNodes());
            }
        }

        if (vccNodes.isEmpty() || gndNodes.isEmpty()) {
            return; // Nothing to compare
        }

        Map<Integer, Set<Integer>> adj = new HashMap<>();

        for (Connection conn : circuit.getConnections()) {
            Node a = conn.getFromNode();
            Node b = conn.getToNode();
            if (a == null || b == null) continue;
            int na = a.getNodeNumber();
            int nb = b.getNodeNumber();
            adj.computeIfAbsent(na, k -> new HashSet<>()).add(nb);
            adj.computeIfAbsent(nb, k -> new HashSet<>()).add(na);
        }

        for (Component comp : circuit.getComponents()) {
            if (comp instanceof Switch) {
                Switch sw = (Switch) comp;
                if (sw.isClosed()) {
                    Node a = sw.getInputNode(0);
                    Node b = sw.getOutputNode(0);
                    if (a == null || b == null) continue;
                    int na = a.getNodeNumber();
                    int nb = b.getNodeNumber();
                    adj.computeIfAbsent(na, k -> new HashSet<>()).add(nb);
                    adj.computeIfAbsent(nb, k -> new HashSet<>()).add(na);
                }
            }
        }

        Set<Integer> gndSet = new HashSet<>();
        for (Node g : gndNodes) {
            if (g != null) gndSet.add(g.getNodeNumber());
        }

        ArrayDeque<Integer> q = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        for (Node v : vccNodes) {
            if (v == null) continue;
            int nv = v.getNodeNumber();
            q.add(nv);
            visited.add(nv);
        }

        while (!q.isEmpty()) {
            int cur = q.removeFirst();
            if (gndSet.contains(cur)) {
                warnings.add("SHORT CIRCUIT DETECTED: VCC is connected directly to Ground.");
                return;
            }
            for (int nxt : adj.getOrDefault(cur, Collections.emptySet())) {
                if (visited.add(nxt)) {
                    q.addLast(nxt);
                }
            }
        }
    }

    private void validateDataFlow() {
        // Check for undriven nodes
        Set<Integer> drivenNodes = new HashSet<>();
        for (Component comp : circuit.getComponents()) {
            if (comp instanceof Switch) {
                List<Node> outputs = comp.getOutputNodes();
                for (Node node : outputs) {
                    drivenNodes.add(node.getNodeNumber());
                }
            } else if (!(comp instanceof LED)) {
                List<Node> outputs = comp.getOutputNodes();
                for (Node node : outputs) {
                    drivenNodes.add(node.getNodeNumber());
                }
            }
        }

        // Check gate inputs
        for (Component comp : circuit.getComponents()) {
            if (comp instanceof LogicGate || comp instanceof LED) {
                List<Node> inputs = comp.getInputNodes();
                for (Node node : inputs) {
                    if (!drivenNodes.contains(node.getNodeNumber()) && 
                        !isInputNode(node)) {
                        warnings.add(comp.getName() + " input " + node.getLabel() + 
                                   " may not be driven");
                    }
                }
            }
        }
    }

    private boolean isInputNode(Node node) {
        for (Component comp : circuit.getComponents()) {
            if (comp instanceof Switch) {
                if (comp.getOutputNodes().contains(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public String getValidationReport() {
        StringBuilder report = new StringBuilder();
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            report.append("Circuit is VALID\n");
        } else {
            if (!errors.isEmpty()) {
                report.append("ERRORS:\n");
                for (String error : errors) {
                    report.append("  - ").append(error).append("\n");
                }
            }
            if (!warnings.isEmpty()) {
                report.append("WARNINGS:\n");
                for (String warning : warnings) {
                    report.append("  - ").append(warning).append("\n");
                }
            }
        }
        return report.toString();
    }
}
