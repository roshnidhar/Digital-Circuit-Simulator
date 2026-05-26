package com.circuitsimulator.validator;

import com.circuitsimulator.circuit.Circuit;
import com.circuitsimulator.components.Switch;
import com.circuitsimulator.components.LogicGate;
import com.circuitsimulator.components.LED;
import java.util.*;


public class TruthTableGenerator {
    private Circuit circuit;

    public TruthTableGenerator(Circuit circuit) {
        this.circuit = circuit;
    }

    public String[][] generateTruthTable() {
        return generateTruthTable(null);
    }

    public String[][] generateTruthTable(com.circuitsimulator.circuit.Node targetNode) {
        List<Switch> switches;
        
        if (targetNode == null) {
            // Original behavior: use all switches for full circuit truth table
            switches = getSwitches();
        } else {
            // New behavior: find only switches that affect the target node
            switches = getRelevantSwitchesForNode(targetNode);
        }
        
        if (switches.isEmpty()) {
            return new String[][] {{"No switches in circuit"}};
        }

        int inputCount = switches.size();
        int rowCount = (int) Math.pow(2, inputCount);
        int outputCount = targetNode == null ? getLEDs().size() : 1;
        
        String[][] truthTable = new String[rowCount + 1][inputCount + outputCount];

        // Header row
        int col = 0;
        for (int i = 0; i < inputCount; i++) {
            Switch sw = switches.get(i);
            String name = sw != null ? sw.getName() : null;
            truthTable[0][col++] = (name != null && !name.trim().isEmpty()) ? name : ("S" + (i + 1));
        }
        if (targetNode == null) {
            List<com.circuitsimulator.components.LED> leds = getLEDs();
            for (int i = 0; i < leds.size(); i++) {
                com.circuitsimulator.components.LED led = leds.get(i);
                String name = led != null ? led.getName() : null;
                truthTable[0][col++] = (name != null && !name.trim().isEmpty()) ? name : ("OUT" + (i + 1));
            }
        } else {
            truthTable[0][col++] = targetNode.getLabel() + " (N" + targetNode.getNodeNumber() + ")";
        }

        // Truth table rows
        for (int row = 1; row <= rowCount; row++) {
            col = 0;
            
            // Set switch states based on binary representation
            for (int i = 0; i < inputCount; i++) {
                boolean bit = ((row - 1) & (1 << (inputCount - 1 - i))) != 0;
                switches.get(i).setState(bit);
                truthTable[row][col++] = bit ? "1" : "0";
            }

            // Simulate the circuit
            circuit.simulate();

            if (targetNode == null) {
                List<com.circuitsimulator.components.LED> leds = getLEDs();
                for (com.circuitsimulator.components.LED led : leds) {
                    truthTable[row][col++] = led.isLit() ? "1" : "0";
                }
            } else {
                truthTable[row][col++] = targetNode.getState() ? "1" : "0";
            }
        }

        return truthTable;
    }


    public String getTruthTableAsString() {
        String[][] table = generateTruthTable();
        StringBuilder sb = new StringBuilder();

        for (String[] row : table) {
            for (String cell : row) {
                sb.append(String.format("%8s", cell != null ? cell : ""));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String getTruthTableFormatted() {
        String[][] table = generateTruthTable();
        StringBuilder sb = new StringBuilder();

        int cols = table[0].length;
        int[] colWidths = new int[cols];

        // Calculate column widths
        for (String[] row : table) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
        }

        // Print header
        printSeparator(sb, colWidths);
        printRow(sb, table[0], colWidths);
        printSeparator(sb, colWidths);

        // Print data rows
        for (int i = 1; i < table.length; i++) {
            printRow(sb, table[i], colWidths);
        }
        printSeparator(sb, colWidths);

        return sb.toString();
    }

    public String getTruthTableFormatted(com.circuitsimulator.circuit.Node targetNode) {
        String[][] table = generateTruthTable(targetNode);
        StringBuilder sb = new StringBuilder();

        int cols = table[0].length;
        int[] colWidths = new int[cols];

        // Calculate column widths
        for (String[] row : table) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
        }

        // Print header
        printSeparator(sb, colWidths);
        printRow(sb, table[0], colWidths);
        printSeparator(sb, colWidths);

        // Print data rows
        for (int i = 1; i < table.length; i++) {
            printRow(sb, table[i], colWidths);
        }
        printSeparator(sb, colWidths);

        return sb.toString();
    }

    private void printSeparator(StringBuilder sb, int[] colWidths) {
        for (int width : colWidths) {
            sb.append("+");
            for (int i = 0; i < width + 2; i++) {
                sb.append("-");
            }
        }
        sb.append("+\n");
    }

    private void printRow(StringBuilder sb, String[] row, int[] colWidths) {
        for (int i = 0; i < row.length; i++) {
            sb.append("| ");
            sb.append(String.format("%-" + colWidths[i] + "s", row[i] != null ? row[i] : ""));
            sb.append(" ");
        }
        sb.append("|\n");
    }

    private List<Switch> getRelevantSwitchesForNode(com.circuitsimulator.circuit.Node targetNode) {
        Set<com.circuitsimulator.circuit.Node> visited = new HashSet<>();
        Set<Switch> relevantSwitches = new HashSet<>();
        
        // Start backward traversal from target node
        traverseBackward(targetNode, visited, relevantSwitches);
        
        // Return switches in a consistent order (by component ID)
        return relevantSwitches.stream()
            .sorted((s1, s2) -> s1.getId().compareTo(s2.getId()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    private void traverseBackward(com.circuitsimulator.circuit.Node node, Set<com.circuitsimulator.circuit.Node> visited, Set<Switch> switches) {
        if (visited.contains(node)) {
            return; // Avoid cycles
        }
        visited.add(node);
        
        // Find all connections where this node is the destination (input to some component)
        for (com.circuitsimulator.circuit.Connection conn : circuit.getConnections()) {
            if (conn.getToNode().equals(node)) {
                com.circuitsimulator.circuit.Node sourceNode = conn.getFromNode();
                
                // Find which component owns the source node
                for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
                    if (comp.getOutputNodes().contains(sourceNode)) {
                        if (comp instanceof Switch) {
                            switches.add((Switch) comp);
                        } else if (comp instanceof LogicGate || comp instanceof LED) {
                            // Continue traversal through logic gates and LEDs
                            for (com.circuitsimulator.circuit.Node inputNode : comp.getInputNodes()) {
                                traverseBackward(inputNode, visited, switches);
                            }
                        }
                        // VCC and GND don't have inputs, so they terminate the traversal
                        break;
                    }
                }
            }
        }
    }

    private List<Switch> getSwitches() {
        List<Switch> switches = new ArrayList<>();
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof Switch) {
                switches.add((Switch) comp);
            }
        }
        return switches;
    }

    private List<com.circuitsimulator.components.LED> getLEDs() {
        List<com.circuitsimulator.components.LED> leds = new ArrayList<>();
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof com.circuitsimulator.components.LED) {
                leds.add((com.circuitsimulator.components.LED) comp);
            }
        }
        return leds;
    }
}
