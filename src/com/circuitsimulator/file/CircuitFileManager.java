package com.circuitsimulator.file;

import com.circuitsimulator.circuit.Circuit;
import java.io.*;

public class CircuitFileManager {

    public static boolean saveCircuit(Circuit circuit, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            oos.writeObject(circuit);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving circuit: " + e.getMessage());
            return false;
        }
    }

    public static Circuit loadCircuit(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {
            Circuit circuit = (Circuit) ois.readObject();
            return circuit;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading circuit: " + e.getMessage());
            return null;
        }
    }

    public static void exportAsText(Circuit circuit, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=== CIRCUIT: " + circuit.getCircuitName() + " ===\n");

            writer.println("COMPONENTS:");
            for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
                writer.println("  " + comp.getId() + ": " + comp.toString());
                writer.println("    Type: " + comp.getComponentType());
                writer.println("    Inputs: " + comp.getInputNodes().size());
                writer.println("    Outputs: " + comp.getOutputNodes().size());
            }

            writer.println("\nNODES:");
            for (com.circuitsimulator.circuit.Node node : circuit.getAllNodes().values()) {
                writer.println("  " + node.toString());
            }

            writer.println("\nCONNECTIONS:");
            for (com.circuitsimulator.circuit.Connection conn : circuit.getConnections()) {
                writer.println("  " + conn.toString() + " (R=" + conn.getResistance() + ")");
            }

        } catch (IOException e) {
            System.err.println("Error exporting circuit: " + e.getMessage());
        }
    }
}
