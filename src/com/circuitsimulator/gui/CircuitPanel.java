package com.circuitsimulator.gui;

import com.circuitsimulator.circuit.Circuit;
import com.circuitsimulator.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;
import java.util.HashMap;
import java.util.Map;

public class CircuitPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Circuit circuit;
    private com.circuitsimulator.components.Component selectedComponent;
    private com.circuitsimulator.circuit.Connection selectedConnection;
    private com.circuitsimulator.circuit.Node selectedNode;
    private com.circuitsimulator.components.Component popupTargetComponent;
    private JPopupMenu componentPopupMenu;
    private Point lastMousePos;
    private Terminal potentialWireStartTerminal;
    private Point wirePressPoint;
    private boolean isDragStarted = false;
    private static final int GRID_SIZE = 20;
    private static final int COMPONENT_SIZE = 50;
    private static final int TERMINAL_SIZE = 10;
    private static final int TRUTH_TABLE_MIN_WIDTH = 360;
    private static final int TRUTH_TABLE_MIN_HEIGHT = 220;
    private Map<com.circuitsimulator.components.Component, ComponentRenderer> renderers;
    
    private Point originalDragPosition;
    private boolean isDraggingComponent = false;
    private Point previewPosition; // Shows where component will snap to
    
    private boolean isPlacementMode = false;
    private String placementComponentType = null;
    private Point placementPreviewPosition = null;
    
    private static class Terminal {
        com.circuitsimulator.components.Component component;
        boolean isOutput;
        int portIndex;
        Point position;
        
        Terminal(com.circuitsimulator.components.Component comp, boolean isOutput, int portIndex, Point pos) {
            this.component = comp;
            this.isOutput = isOutput;
            this.portIndex = portIndex;
            this.position = pos;
        }
    }
    
    // Drag-to-connect wires
    private Terminal dragStartTerminal;
    private Terminal dragOverTerminal;
    private Point currentMousePos;
    private boolean isDrawingWire = false;

    public CircuitPanel(Circuit circuit) {
        this.circuit = circuit;
        this.renderers = new HashMap<>();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 700));
        
        initializeEventHandlers();
    }

    private void initializeEventHandlers() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

                if (SwingUtilities.isRightMouseButton(e)) {
                    com.circuitsimulator.components.Component comp = getCompAt(e.getPoint());
                    Terminal terminal = getTerminalAt(e.getPoint());
                    if (comp != null && terminal == null) {
                        popupTargetComponent = comp;
                        createPopupMenuForComponent(comp).show(CircuitPanel.this, e.getX(), e.getY());
                        return;
                    }
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    com.circuitsimulator.circuit.Connection conn = getConnectionAt(e.getPoint());
                    if (conn != null) {
                        selectedConnection = conn;
                        selectedNode = conn.getToNode();
                        selectedComponent = null;
                        repaint();
                        return;
                    }

                    Terminal terminal = getTerminalAt(e.getPoint());
                    if (terminal != null) {
                        potentialWireStartTerminal = terminal;
                        wirePressPoint = e.getPoint();
                        currentMousePos = e.getPoint();
                        isDragStarted = false;
                        return;
                    }

                    com.circuitsimulator.components.Component comp = getCompAt(e.getPoint());
                    if (comp != null && comp instanceof Switch) {
                        ((Switch) comp).toggle();
                        circuit.simulate();
                    }
                    selectedComponent = comp;
                    selectedConnection = null;
                    selectedNode = null;
                    lastMousePos = e.getPoint();
                    
                    if (comp != null) {
                        originalDragPosition = new Point(comp.getXPos(), comp.getYPos());
                        isDraggingComponent = false;
                    }
                    
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingWire && dragStartTerminal != null) {
                    dragOverTerminal = getTerminalAt(e.getPoint());
                    if (dragOverTerminal != null && dragOverTerminal.component != dragStartTerminal.component) {
                        if (dragStartTerminal.isOutput && !dragOverTerminal.isOutput) {
                            makeConnection(dragStartTerminal, dragOverTerminal);
                        } else if (!dragStartTerminal.isOutput && dragOverTerminal.isOutput) {
                            makeConnection(dragOverTerminal, dragStartTerminal);
                        } else if (dragStartTerminal.isOutput && dragOverTerminal.isOutput) {
                            
                            boolean startIsPower = (dragStartTerminal.component instanceof VCC) || (dragStartTerminal.component instanceof GND);
                            boolean overIsPower = (dragOverTerminal.component instanceof VCC) || (dragOverTerminal.component instanceof GND);
                            if (startIsPower && overIsPower) {
                                makeBidirectionalConnection(dragStartTerminal, dragOverTerminal);
                            }
                        }
                    }
                    isDrawingWire = false;
                    dragStartTerminal = null;
                    dragOverTerminal = null;
                } else if (potentialWireStartTerminal != null && !isDragStarted) {
                    selectedNode = getNodeForTerminal(potentialWireStartTerminal);
                    selectedConnection = null;
                    selectedComponent = potentialWireStartTerminal.component;
                }

                // Handle component drag completion with collision detection
                if (isDraggingComponent && selectedComponent != null && originalDragPosition != null && previewPosition != null) {
                    // First snap to the preview position
                    selectedComponent.setPosition(previewPosition.x, previewPosition.y);
                    
                    // Then check for collision at the snapped position
                    if (hasCollision(selectedComponent)) {
                        // Smoothly revert to original position
                        animateToPosition(selectedComponent, originalDragPosition.x, originalDragPosition.y);
                    }
                }

                potentialWireStartTerminal = null;
                isDragStarted = false;
                isDraggingComponent = false;
                lastMousePos = null;
                originalDragPosition = null;
                previewPosition = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentMousePos = e.getPoint();

                if (potentialWireStartTerminal != null && wirePressPoint != null) {
                    int dx = e.getX() - wirePressPoint.x;
                    int dy = e.getY() - wirePressPoint.y;
                    if (!isDragStarted && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) {
                        dragStartTerminal = potentialWireStartTerminal;
                        isDrawingWire = true;
                        isDragStarted = true;
                    }
                }

                if (isDrawingWire) {
                    dragOverTerminal = getTerminalAt(e.getPoint());
                    repaint();
                } else if (selectedComponent != null && lastMousePos != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;

                    // Allow free movement during dragging for fluid experience
                    int newX = selectedComponent.getXPos() + dx;
                    int newY = selectedComponent.getYPos() + dy;

                    selectedComponent.setPosition(newX, newY);
                    
                    // Calculate and store preview snap position for visual feedback
                    int snapX = Math.round((float) newX / GRID_SIZE) * GRID_SIZE;
                    int snapY = Math.round((float) newY / GRID_SIZE) * GRID_SIZE;
                    previewPosition = new Point(snapX, snapY);
                    
                    isDraggingComponent = true;
                    lastMousePos = e.getPoint();
                    repaint();
                }
                
                // Handle placement preview
                if (isPlacementMode && !isDrawingWire && selectedComponent == null) {
                    
                    if (getCompAt(e.getPoint()) == null) {
                        // Calculate snapped position for placement preview
                        int snapX = Math.round((float) e.getX() / GRID_SIZE) * GRID_SIZE;
                        int snapY = Math.round((float) e.getY() / GRID_SIZE) * GRID_SIZE;
                        placementPreviewPosition = new Point(snapX, snapY);
                        repaint();
                    } else {
                        // Clear preview when hovering over existing component
                        if (placementPreviewPosition != null) {
                            placementPreviewPosition = null;
                            repaint();
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Handle placement preview during hover
                if (isPlacementMode && !isDrawingWire && selectedComponent == null) {
                    // Only show preview if not hovering over an existing component
                    if (getCompAt(e.getPoint()) == null) {
                        // Calculate snapped position for placement preview
                        int snapX = Math.round((float) e.getX() / GRID_SIZE) * GRID_SIZE;
                        int snapY = Math.round((float) e.getY() / GRID_SIZE) * GRID_SIZE;
                        placementPreviewPosition = new Point(snapX, snapY);
                        repaint();
                    } else {
                        // Clear preview when hovering over existing component
                        if (placementPreviewPosition != null) {
                            placementPreviewPosition = null;
                            repaint();
                        }
                    }
                } else if (!isPlacementMode) {
                    // Clear placement preview when not in placement mode
                    if (placementPreviewPosition != null) {
                        placementPreviewPosition = null;
                        repaint();
                    }
                }
            }
        });

        setFocusable(true);
    }
    
    private void makeConnection(Terminal fromTerminal, Terminal toTerminal) {
        try {
            java.util.List<com.circuitsimulator.circuit.Node> fromNodes = fromTerminal.component.getOutputNodes();
            java.util.List<com.circuitsimulator.circuit.Node> toNodes = toTerminal.component.getInputNodes();
            
            if (fromTerminal.portIndex < fromNodes.size() && toTerminal.portIndex < toNodes.size()) {
                com.circuitsimulator.circuit.Node fromNode = fromNodes.get(fromTerminal.portIndex);
                com.circuitsimulator.circuit.Node toNode = toNodes.get(toTerminal.portIndex);
                
                circuit.connect(fromNode, toNode);
                circuit.simulate();
                JOptionPane.showMessageDialog(this, 
                    "✓ Connected!\nNode " + fromNode.getNodeNumber() + " → Node " + toNode.getNodeNumber(),
                    "Wire Connected", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect: " + ex.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeBidirectionalConnection(Terminal terminalA, Terminal terminalB) {
        try {
            java.util.List<com.circuitsimulator.circuit.Node> aNodes = terminalA.isOutput
                ? terminalA.component.getOutputNodes()
                : terminalA.component.getInputNodes();
            java.util.List<com.circuitsimulator.circuit.Node> bNodes = terminalB.isOutput
                ? terminalB.component.getOutputNodes()
                : terminalB.component.getInputNodes();

            if (terminalA.portIndex < aNodes.size() && terminalB.portIndex < bNodes.size()) {
                com.circuitsimulator.circuit.Node aNode = aNodes.get(terminalA.portIndex);
                com.circuitsimulator.circuit.Node bNode = bNodes.get(terminalB.portIndex);

                // Model a wire between two terminals as bidirectional signal propagation.
                circuit.connect(aNode, bNode);
                circuit.connect(bNode, aNode);
                circuit.simulate();
                JOptionPane.showMessageDialog(this,
                    "✓ Connected!\nNode " + aNode.getNodeNumber() + " ↔ Node " + bNode.getNodeNumber(),
                    "Wire Connected", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect: " + ex.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPopupMenu createPopupMenuForComponent(com.circuitsimulator.components.Component comp) {
        JPopupMenu menu = new JPopupMenu();
        if (comp instanceof LED) {
            JMenu colorMenu = new JMenu("LED Color");
            for (LED.LedColor ledColor : LED.LedColor.values()) {
                JMenuItem colorItem = new JMenuItem(ledColor.name());
                colorItem.addActionListener(e -> {
                    ((LED) comp).setLedColor(ledColor);
                    circuit.simulate();
                    repaint();
                });
                colorMenu.add(colorItem);
            }
            menu.add(colorMenu);
        }

        if (comp instanceof Switch) {
            Switch sw = (Switch) comp;
            JMenuItem toggleItem = new JMenuItem(sw.isOpen() ? "Close Switch" : "Open Switch");
            toggleItem.addActionListener(e -> {
                sw.toggle();
                circuit.simulate();
                repaint();
            });
            menu.add(toggleItem);
        }

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            circuit.removeComponent(comp);
            if (comp == selectedComponent) {
                selectedComponent = null;
            }
            if (comp == popupTargetComponent) {
                popupTargetComponent = null;
            }
            selectedConnection = null;
            selectedNode = null;
            circuit.simulate();
            repaint();
        });
        menu.add(deleteItem);
        return menu;
    }
    
    private Terminal getTerminalAt(Point p) {
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof TruthTableComponent) {
                continue; // Truth table has no terminals
            }
            int x = comp.getXPos();
            int y = comp.getYPos();
            int size = COMPONENT_SIZE;
            int width = getComponentWidth(comp);
            
            // Check input ports (left side)
            java.util.List<com.circuitsimulator.circuit.Node> inputs = comp.getInputNodes();
            for (int i = 0; i < inputs.size(); i++) {
                int portX, portY;
                if (comp instanceof ANDGate) {
                    portX = x - width/2 - 20;
                    if (i == 0) {
                        portY = y - size/2 + 10;
                    } else {
                        portY = y + size/2 - 10;
                    }
                } else {
                    portY = y - size/2 + (i+1) * (size / (inputs.size() + 1));
                    portX = x - width/2 - 15;
                }
                // Check if within port bounds (16x16 pixel circle, so radius of ~8)
                if (Math.abs(p.x - portX) < 15 && Math.abs(p.y - portY) < 15) {
                    return new Terminal(comp, false, i, new Point(portX, portY));
                }
            }
            
            // Check output ports (right side)
            java.util.List<com.circuitsimulator.circuit.Node> outputs = comp.getOutputNodes();
            for (int i = 0; i < outputs.size(); i++) {
                int portX, portY;
                if (comp instanceof ANDGate) {
                    portX = x + width/2 + 20;
                    portY = y;
                } else {
                    portY = y - size/2 + (i+1) * (size / (outputs.size() + 1));
                    portX = x + width/2 + 15;
                }
                // Check if within port bounds
                if (Math.abs(p.x - portX) < 15 && Math.abs(p.y - portY) < 15) {
                    return new Terminal(comp, true, i, new Point(portX, portY));
                }
            }
        }
        return null;
    }

    private com.circuitsimulator.circuit.Node getNodeForTerminal(Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        if (terminal.isOutput) {
            java.util.List<com.circuitsimulator.circuit.Node> outputs = terminal.component.getOutputNodes();
            if (terminal.portIndex < outputs.size()) {
                return outputs.get(terminal.portIndex);
            }
        } else {
            java.util.List<com.circuitsimulator.circuit.Node> inputs = terminal.component.getInputNodes();
            if (terminal.portIndex < inputs.size()) {
                return inputs.get(terminal.portIndex);
            }
        }
        return null;
    }

    private com.circuitsimulator.circuit.Connection getConnectionAt(Point p) {
        for (com.circuitsimulator.circuit.Connection conn : circuit.getConnections()) {
            Point fromPoint = findNodeEndpoint(conn.getFromNode());
            Point toPoint = findNodeEndpoint(conn.getToNode());
            if (fromPoint == null || toPoint == null) continue;
            if (distancePointToSegment(p, fromPoint, toPoint) < 8) {
                return conn;
            }
        }
        return null;
    }

    private double distancePointToSegment(Point p, Point a, Point b) {
        double px = p.x;
        double py = p.y;
        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;

        double dx = bx - ax;
        double dy = by - ay;
        if (dx == 0 && dy == 0) {
            return p.distance(a);
        }

        double t = ((px - ax) * dx + (py - ay) * dy) / (dx*dx + dy*dy);
        t = Math.max(0, Math.min(1, t));

        double projX = ax + t * dx;
        double projY = ay + t * dy;
        return Point.distance(px, py, projX, projY);
    }

    public boolean deleteSelectedConnection() {
        if (selectedConnection != null) {
            circuit.removeConnection(selectedConnection);
            selectedConnection = null;
            selectedNode = null;
            circuit.simulate();
            repaint();
            return true;
        }
        return false;
    }

    public com.circuitsimulator.circuit.Node getSelectedTargetNode() {
        if (selectedNode != null) {
            return selectedNode;
        }
        if (selectedConnection != null) {
            return selectedConnection.getToNode();
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawConnections(g2);
        drawComponents(g2);
        
        // Draw placement preview when in placement mode
        if (isPlacementMode && placementPreviewPosition != null) {
            drawPlacementPreview(g2);
        }
        
        
        drawPorts(g2);
        
        if (isDrawingWire && dragStartTerminal != null && currentMousePos != null) {
            drawDraggingWire(g2);
        }
    }
    
    private void drawDraggingWire(Graphics2D g) {
        // Draw straight line from start terminal to current mouse position
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 165, 0));  // Orange while dragging
        
        int x1 = dragStartTerminal.position.x;
        int y1 = dragStartTerminal.position.y;
        int x2 = currentMousePos.x;
        int y2 = currentMousePos.y;
        
        // Draw straight line
        g.drawLine(x1, y1, x2, y2);
        
        // Draw start circle
        g.setColor(new Color(255, 200, 0));
        g.fillOval(x1 - 6, y1 - 6, 12, 12);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(x1 - 6, y1 - 6, 12, 12);
        
        // Draw end circle (hover effect)
        if (dragOverTerminal != null) {
            g.setColor(new Color(100, 255, 100));
            g.fillOval(x2 - 7, y2 - 7, 14, 14);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x2 - 7, y2 - 7, 14, 14);
        } else {
            g.setColor(new Color(150, 150, 150));
            g.fillOval(x2 - 7, y2 - 7, 14, 14);
        }
    }
    
    private void drawPreviewPosition(Graphics2D g) {
        if (previewPosition == null || selectedComponent == null) return;
        
        // Save current graphics state
        Composite originalComposite = g.getComposite();
        
        // Set semi-transparent composite for ghost effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        // Create a temporary component at preview position for rendering
        int originalX = selectedComponent.getXPos();
        int originalY = selectedComponent.getYPos();
        
        // Temporarily move component to preview position for rendering
        selectedComponent.setPosition(previewPosition.x, previewPosition.y);
        
        // Draw the ghost component
        int width = getComponentWidth(selectedComponent);
        ComponentRenderer renderer = renderers.computeIfAbsent(selectedComponent,
            c -> new ComponentRenderer(COMPONENT_SIZE, width));
        
        // Draw the component with ghost effect (no selection highlight)
        renderer.draw(g, selectedComponent, false);
        
        // Restore original position
        selectedComponent.setPosition(originalX, originalY);
        
        // Restore original composite
        g.setComposite(originalComposite);
        
        // Draw subtle snap indicator
        g.setColor(new Color(100, 200, 255, 150));
        g.fillOval(previewPosition.x - 2, previewPosition.y - 2, 4, 4);
    }
    
    private void drawPlacementPreview(Graphics2D g) {
        if (placementPreviewPosition == null || placementComponentType == null) return;
        
        // Save current graphics state
        Composite originalComposite = g.getComposite();
        
        // Set semi-transparent composite for ghost effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        
        // Create a temporary component for preview rendering
        com.circuitsimulator.components.Component tempComponent = createTempComponent(placementComponentType);
        if (tempComponent != null) {
            // Position the temporary component at preview location
            tempComponent.setPosition(placementPreviewPosition.x, placementPreviewPosition.y);
            
            // Draw the component
            int width = getComponentWidth(tempComponent);
            ComponentRenderer renderer = new ComponentRenderer(COMPONENT_SIZE, width);
            renderer.draw(g, tempComponent, false);
        }
        
        // Restore original composite
        g.setComposite(originalComposite);
        
        // Draw placement indicator
        g.setColor(new Color(37, 211, 102, 180)); // WhatsApp green with transparency
        g.fillOval(placementPreviewPosition.x - 3, placementPreviewPosition.y - 3, 6, 6);
    }
    
    private com.circuitsimulator.components.Component createTempComponent(String componentType) {
        try {
            String tempId = "temp";
            String tempName = componentType;
            
            switch (componentType) {
                case "AND": return new ANDGate(tempId, tempName, 2);
                case "OR": return new ORGate(tempId, tempName, 2);
                case "NOT": return new NOTGate(tempId, tempName);
                case "XOR": return new XORGate(tempId, tempName, 2);
                case "NAND": return new NANDGate(tempId, tempName, 2);
                case "NOR": return new NORGate(tempId, tempName, 2);
                case "XNOR": return new XNORGate(tempId, tempName, 2);
                case "Switch": return new Switch(tempId, tempName);
                case "LED": return new LED(tempId, tempName);
                case "VCC": return new VCC(tempId, tempName);
                case "GND": return new GND(tempId, tempName);
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private void drawPorts(Graphics2D g) {
        // Draw connection ports on each component - drawn on top of everything
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof TruthTableComponent) {
                continue; // Truth table has no ports
            }
            int x = comp.getXPos();
            int y = comp.getYPos();
            int size = COMPONENT_SIZE;
            int width = getComponentWidth(comp);
            
            // Draw input ports on left side - MUCH LARGER and more visible
            java.util.List<com.circuitsimulator.circuit.Node> inputs = comp.getInputNodes();
            for (int i = 0; i < inputs.size(); i++) {
                int portX, portY;
                if (comp instanceof ANDGate) {
                    // Specific positions for AND gate wires
                    portX = x - width/2 - 20;
                    if (i == 0) {
                        portY = y - size/2 + 10; // upper input
                    } else {
                        portY = y + size/2 - 10; // lower input
                    }
                } else {
                    // General calculation
                    portY = y - size/2 + (i+1) * (size / (inputs.size() + 1));
                    portX = x - width/2 - 15;
                }
                com.circuitsimulator.circuit.Node node = inputs.get(i);
                
                // Fill with blue, but darker if LOW, brighter if HIGH
                if (node.getState()) {
                    g.setColor(new Color(100, 200, 255));  // Bright blue for HIGH
                } else {
                    g.setColor(new Color(50, 100, 150));   // Dark blue for LOW
                }
                g.fillOval(portX - 8, portY - 8, 16, 16);
                
                // Bold black border
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(3));
                g.drawOval(portX - 8, portY - 8, 16, 16);
                
                // Display state (0 or 1) inside the circle
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                String stateChar = node.getState() ? "1" : "0";
                g.drawString(stateChar, portX - 2, portY + 4);
            }
            
            // Draw output ports on right side - MUCH LARGER and more visible
            java.util.List<com.circuitsimulator.circuit.Node> outputs = comp.getOutputNodes();
            for (int i = 0; i < outputs.size(); i++) {
                int portX, portY;
                if (comp instanceof ANDGate) {
                    // Specific position for AND gate output wire
                    portX = x + width/2 + 20;
                    portY = y; // center
                } else {
                    // General calculation
                    portY = y - size/2 + (i+1) * (size / (outputs.size() + 1));
                    portX = x + width/2 + 15;
                }
                com.circuitsimulator.circuit.Node node = outputs.get(i);
                
                // Fill with red, but darker if LOW, brighter if HIGH
                if (node.getState()) {
                    g.setColor(new Color(255, 100, 100));  // Bright red for HIGH
                } else {
                    g.setColor(new Color(150, 50, 50));    // Dark red for LOW
                }
                g.fillOval(portX - 8, portY - 8, 16, 16);
                
                // Bold black border
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(3));
                g.drawOval(portX - 8, portY - 8, 16, 16);
                
                // Display state (0 or 1) inside the circle
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                String stateChar = node.getState() ? "1" : "0";
                g.drawString(stateChar, portX - 2, portY + 4);
            }
        }
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(200, 200, 200, 100)); // Light gray gridlines
        g.setStroke(new BasicStroke(1.0f));
        
        for (int x = 0; x < getWidth(); x += GRID_SIZE) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += GRID_SIZE) {
            g.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawConnections(Graphics2D g) {
        for (com.circuitsimulator.circuit.Connection conn : circuit.getConnections()) {
            com.circuitsimulator.circuit.Node fromNode = conn.getFromNode();
            com.circuitsimulator.circuit.Node toNode = conn.getToNode();

            Point fromPoint = findNodeEndpoint(fromNode);
            Point toPoint = findNodeEndpoint(toNode);

            if (fromPoint != null && toPoint != null) {
                boolean isSelected = conn == selectedConnection;
                if (isSelected) {
                    g.setColor(new Color(255, 230, 120));
                    g.setStroke(new BasicStroke(5));
                } else if (fromNode.getState()) {
                    g.setColor(new Color(255, 50, 50));     // Bright red for HIGH
                    g.setStroke(new BasicStroke(3));        // Thicker when HIGH
                } else {
                    g.setColor(new Color(50, 100, 180));    // Blue for LOW
                    g.setStroke(new BasicStroke(2));
                }
                
                // Draw straight line from input to output
                g.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                
                // Draw junction dots at both ends
                g.setColor(new Color(30, 30, 30));
                g.fillOval(fromPoint.x - 3, fromPoint.y - 3, 6, 6);
                g.fillOval(toPoint.x - 3, toPoint.y - 3, 6, 6);
                
                // Draw node label with background on wire
                g.setColor(new Color(255, 255, 200, 230));
                int labelX = (fromPoint.x + toPoint.x) / 2 - 15;
                int labelY = (fromPoint.y + toPoint.y) / 2 - 10;
                g.fillRoundRect(labelX, labelY, 30, 18, 4, 4);
                
                g.setColor(new Color(0, 0, 0));
                g.setFont(new Font("Arial", Font.BOLD, 10));
                String label = "N" + fromNode.getNodeNumber();
                FontMetrics fm = g.getFontMetrics();
                g.drawString(label, labelX + (30 - fm.stringWidth(label))/2, labelY + 13);
            }
        }
    }

    private void drawComponents(Graphics2D g) {
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            int width = getComponentWidth(comp);
            ComponentRenderer renderer = renderers.computeIfAbsent(comp,
                c -> new ComponentRenderer(COMPONENT_SIZE, width));
            
            renderer.draw(g, comp, comp == selectedComponent);
        }
    }

    private int getComponentWidth(com.circuitsimulator.components.Component comp) {
        if (comp instanceof TruthTableComponent) {
            return Math.max(TRUTH_TABLE_MIN_WIDTH, ((TruthTableComponent) comp).getVisualWidth());
        }
        if (comp instanceof ANDGate || comp instanceof NANDGate) {
            return COMPONENT_SIZE + 24;
        }
        return COMPONENT_SIZE;
    }

    private int getComponentHeight(com.circuitsimulator.components.Component comp) {
        if (comp instanceof TruthTableComponent) {
            return Math.max(TRUTH_TABLE_MIN_HEIGHT, ((TruthTableComponent) comp).getVisualHeight());
        }
        return COMPONENT_SIZE;
    }

    private Point findNodeEndpoint(com.circuitsimulator.circuit.Node node) {
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            int x = comp.getXPos();
            int y = comp.getYPos();
            int size = COMPONENT_SIZE;
            int width = getComponentWidth(comp);
            
            if (comp.getInputNodes().contains(node)) {
                int index = comp.getInputNodes().indexOf(node);
                int portX, portY;
                if (comp instanceof ANDGate) {
                    portX = x - width/2 - 20;
                    if (index == 0) {
                        portY = y - size/2 + 10;
                    } else {
                        portY = y + size/2 - 10;
                    }
                } else {
                    portY = y - size/2 + (index+1) * (size / (comp.getInputNodes().size() + 1));
                    portX = x - width/2 - 15;
                }
                return new Point(portX, portY);
            }
            if (comp.getOutputNodes().contains(node)) {
                int index = comp.getOutputNodes().indexOf(node);
                int portX, portY;
                if (comp instanceof ANDGate) {
                    portX = x + width/2 + 20;
                    portY = y;
                } else {
                    portY = y - size/2 + (index+1) * (size / (comp.getOutputNodes().size() + 1));
                    portX = x + width/2 + 15;
                }
                return new Point(portX, portY);
            }
        }
        return null;
    }

    public com.circuitsimulator.components.Component getCompAt(Point p) {
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            int x = comp.getXPos();
            int y = comp.getYPos();
            int width = getComponentWidth(comp);
            int height = getComponentHeight(comp);
            Rectangle2D bounds = new Rectangle2D.Double(
                x - width / 2,
                y - height / 2,
                width,
                height
            );
            if (bounds.contains(p)) {
                return comp;
            }
        }
        return null;
    }

    public void clearSelection() {
        selectedComponent = null;
        selectedConnection = null;
        selectedNode = null;
    }

    public com.circuitsimulator.components.Component getSelectedComponent() {
        return selectedComponent;
    }

    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
        this.renderers.clear();
    }

    // Component placement preview methods
    public void setPlacementMode(boolean enabled, String componentType) {
        this.isPlacementMode = enabled;
        this.placementComponentType = componentType;
        if (!enabled) {
            placementPreviewPosition = null;
        }
        repaint();
    }

    public boolean isInPlacementMode() {
        return isPlacementMode;
    }

    // Collision detection method
    private boolean hasCollision(com.circuitsimulator.components.Component draggedComponent) {
        Rectangle2D draggedBounds = getComponentBounds(draggedComponent);
        
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp != draggedComponent) {
                Rectangle2D compBounds = getComponentBounds(comp);
                if (draggedBounds.intersects(compBounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Get bounding rectangle for a component
    private Rectangle2D getComponentBounds(com.circuitsimulator.components.Component comp) {
        int x = comp.getXPos();
        int y = comp.getYPos();
        int width = getComponentWidth(comp);
        int height = getComponentHeight(comp);
        return new Rectangle2D.Double(
            x - width / 2.0,
            y - height / 2.0,
            width,
            height
        );
    }

    // Smooth animation to revert position
    private void animateToPosition(com.circuitsimulator.components.Component comp, int targetX, int targetY) {
        final int startX = comp.getXPos();
        final int startY = comp.getYPos();
        final int deltaX = targetX - startX;
        final int deltaY = targetY - startY;
        
        // Simple animation with a few steps
        Timer timer = new Timer(20, null);
        timer.addActionListener(new ActionListener() {
            private int steps = 10;
            private int currentStep = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStep++;
                double progress = (double) currentStep / steps;
                
                // Ease-out animation
                double easedProgress = 1 - Math.pow(1 - progress, 3);
                
                int currentX = (int) (startX + deltaX * easedProgress);
                int currentY = (int) (startY + deltaY * easedProgress);
                
                comp.setPosition(currentX, currentY);
                repaint();
                
                if (currentStep >= steps) {
                    // Ensure final position is exact
                    comp.setPosition(targetX, targetY);
                    repaint();
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private static class ComponentRenderer {
        private int height;
        private int width;

        ComponentRenderer(int height, int width) {
            this.height = height;
            this.width = width;
        }

        void draw(Graphics2D g, com.circuitsimulator.components.Component comp, boolean selected) {
            int x = comp.getXPos();
            int y = comp.getYPos();

            if (selected) {
                g.setColor(new Color(255, 200, 0, 80));
                g.fillRoundRect(x - width / 2 - 8, y - height / 2 - 8, width + 16, height + 16, 18, 18);
            }
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));

            if (comp instanceof Switch) {
                drawSwitch(g, x, y, height, (Switch) comp);
            } else if (comp instanceof LED) {
                drawLED(g, x, y, height, (LED) comp);
            } else if (comp instanceof VCC || comp instanceof GND) {
                drawPowerSource(g, x, y, height, comp);
            } else if (comp instanceof TruthTableComponent) {
                drawTruthTable(g, x, y, (TruthTableComponent) comp);
            } else if (comp instanceof LogicGate) {
                drawGate(g, x, y, height, width, comp);
            }

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            String label = comp.getName().substring(0, Math.min(8, comp.getName().length()));
            g.drawString(label,
                x - fm.stringWidth(label) / 2,
                y + height / 2 + 15);
        }

        private void drawTruthTable(Graphics2D g, int x, int y, TruthTableComponent tt) {
            
            int w = Math.max(TRUTH_TABLE_MIN_WIDTH, tt.getVisualWidth());
            int h = Math.max(TRUTH_TABLE_MIN_HEIGHT, tt.getVisualHeight());
            int left = x - w / 2;
            int top = y - h / 2;

            // Background + border
            g.setColor(new Color(245, 248, 255));
            g.fillRoundRect(left, top, w, h, 14, 14);
            g.setColor(new Color(25, 35, 55));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(left, top, w, h, 14, 14);

            // Title bar
            g.setColor(new Color(14, 30, 67));
            g.fillRoundRect(left, top, w, 28, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String title = tt.getTitle() != null ? tt.getTitle() : "Truth Table";
            g.drawString(title, left + 10, top + 18);

            // Table area (monospace)
            String[][] table = tt.getTable();
            if (table == null || table.length == 0 || table[0] == null) {
                g.setColor(new Color(80, 90, 110));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("No truth table data. Click 'Truth Table' to generate.", left + 10, top + 55);
                return;
            }

            // Clip content to avoid drawing outside the component
            Shape oldClip = g.getClip();
            g.setClip(left + 8, top + 32, w - 16, h - 40);

            int cellW = 58;
            int cellH = 18;
            int maxCols = Math.max(1, (w - 16) / cellW);
            int maxRows = Math.max(1, (h - 44) / cellH);
            int cols = Math.min(table[0].length, maxCols);
            int rows = Math.min(table.length, maxRows);

            g.setFont(new Font("Consolas", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();

            int startX = left + 10;
            int startY = top + 40;

            // Grid + text
            g.setColor(new Color(210, 220, 235));
            for (int r = 0; r <= rows; r++) {
                int yy = startY + r * cellH;
                g.drawLine(startX, yy, startX + cols * cellW, yy);
            }
            for (int c = 0; c <= cols; c++) {
                int xx = startX + c * cellW;
                g.drawLine(xx, startY, xx, startY + rows * cellH);
            }

            // Header row background
            g.setColor(new Color(232, 238, 250));
            g.fillRect(startX + 1, startY + 1, cols * cellW - 1, cellH - 1);

            // Text
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String val = (table[r] != null && c < table[r].length && table[r][c] != null) ? table[r][c] : "";
                    if (r == 0) {
                        g.setColor(new Color(25, 35, 55));
                    } else {
                        g.setColor(new Color(40, 55, 80));
                    }
                    int tx = startX + c * cellW + 6;
                    int ty = startY + r * cellH + (cellH + fm.getAscent()) / 2 - 2;
                    if (val.length() > 10) {
                        val = val.substring(0, 10);
                    }
                    g.drawString(val, tx, ty);
                }
            }

            // Restore clip
            g.setClip(oldClip);

            tt.setVisualSize(cols * cellW + 24, Math.max(TRUTH_TABLE_MIN_HEIGHT, rows * cellH + 70));
        }

        private void drawSwitch(Graphics2D g, int x, int y, int size, Switch sw) {
            // Draw switch in proper electronic notation
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));

            // Draw switch terminals
            g.drawLine(x - 15, y, x - 5, y);      // Left terminal
            g.drawLine(x + 5, y, x + 15, y);      // Right terminal

            if (sw.isOpen()) {
                // Open switch - diagonal line broken
                g.drawLine(x - 5, y, x + 3, y - 8);
                g.setColor(Color.RED);
                g.drawString("OPEN", x - 15, y - 15);
            } else {
                // Closed switch - straight line
                g.drawLine(x - 5, y, x + 5, y);
                g.setColor(Color.GREEN);
                g.drawString("CLOSED", x - 20, y - 15);
            }
        }

        private void drawLED(Graphics2D g, int x, int y, int size, LED led) {
            int radius = size / 3;
            int cx = x;
            int cy = y;

            if (led.isLit()) {
                g.setColor(led.getCurrentColor());
                g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            } else {
                g.setColor(new Color(200, 200, 200));
                g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            }

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            if (led.isLit()) {
                g.setColor(led.getCurrentColor());
                g.setStroke(new BasicStroke(2));
                g.drawLine(cx + radius - 2, cy - 6, cx + radius + 12, cy - 14);
                g.drawLine(cx + radius - 2, cy + 6, cx + radius + 12, cy + 14);
            }
        }

        private void drawPowerSource(Graphics2D g, int x, int y, int size, com.circuitsimulator.components.Component comp) {
            g.setColor(new Color(255, 215, 100));
            g.setStroke(new BasicStroke(3));
            if (comp instanceof VCC) {
                g.drawLine(x, y + size / 2, x, y - size / 2);
                g.drawLine(x - 10, y - size / 2 + 10, x + 10, y - size / 2 + 10);
                g.drawLine(x - 5, y - size / 2 + 20, x + 5, y - size / 2 + 20);
                g.drawString("VCC", x - 12, y + size / 2 + 20);
            } else {
                g.drawLine(x, y - size / 2, x, y + size / 2);
                g.drawLine(x - 10, y + size / 2 - 10, x + 10, y + size / 2 - 10);
                g.drawLine(x - 8, y + size / 2 - 4, x + 8, y + size / 2 - 4);
                g.drawLine(x - 6, y + size / 2 + 2, x + 6, y + size / 2 + 2);
                g.drawString("GND", x - 12, y + size / 2 + 20);
            }
        }

        private void drawGate(Graphics2D g, int x, int y, int height, int width, com.circuitsimulator.components.Component gate) {
            String type = gate.getComponentType();
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(170, 210, 255));

            switch(type) {
                case "AND":
                    drawANDGate(g, x, y, height, width);
                    break;
                case "OR":
                    drawORGate(g, x, y, height, width);
                    break;
                case "NOT":
                    drawNOTGate(g, x, y, height, width);
                    break;
                case "XOR":
                    drawXORGate(g, x, y, height, width);
                    break;
                case "NAND":
                    drawNANDGate(g, x, y, height, width);
                    break;
                case "NOR":
                    drawNORGate(g, x, y, height, width);
                    break;
                case "XNOR":
                    drawXNORGate(g, x, y, height, width);
                    break;
                default:
                    drawDefaultGate(g, x, y, height, width);
            }
        }

        private void drawDefaultGate(Graphics2D g, int x, int y, int height, int width) {
            g.fillRect(x - width / 2, y - height / 2, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x - width / 2, y - height / 2, width, height);
        }

        private void drawANDGate(Graphics2D g, int x, int y, int height, int width) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            int leftX = x - halfWidth;
            int rightX = x + halfWidth;

            
            java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
            path.moveTo(leftX, y - halfHeight); // top left
            path.lineTo(leftX, y + halfHeight); // straight left edge down
            path.quadTo(rightX, y + halfHeight, rightX, y); // bottom-right curve
            path.quadTo(rightX, y - halfHeight, leftX, y - halfHeight); // top-right curve back
            path.closePath();

            g.fill(path);
            g.setColor(Color.BLACK);
            g.draw(path);
        }

        private void drawORGate(Graphics2D g, int x, int y, int height, int width) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
            path.moveTo(x - halfWidth + 8, y - halfHeight);
            path.quadTo(x - halfWidth + 20, y, x - halfWidth + 8, y + halfHeight);
            path.quadTo(x + halfWidth - 10, y + halfHeight, x + halfWidth, y);
            path.quadTo(x + halfWidth - 10, y - halfHeight, x - halfWidth + 8, y - halfHeight);
            path.closePath();

            g.fill(path);
            g.setColor(Color.BLACK);
            g.draw(path);
        }

        private void drawNOTGate(Graphics2D g, int x, int y, int height, int width) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            int[] xPts = {x - halfWidth, x + halfWidth - 5, x - halfWidth};
            int[] yPts = {y - halfHeight, y, y + halfHeight};

            g.fillPolygon(xPts, yPts, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xPts, yPts, 3);
            g.drawOval(x + halfWidth - 8, y - 4, 8, 8);
        }

        private void drawXORGate(Graphics2D g, int x, int y, int height, int width) {
            drawORGate(g, x, y, height, width);
            g.setColor(new Color(100, 150, 255));
            java.awt.geom.GeneralPath extra = new java.awt.geom.GeneralPath();
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            extra.moveTo(x - halfWidth + 4, y - halfHeight);
            extra.quadTo(x - halfWidth + 16, y, x - halfWidth + 4, y + halfHeight);
            g.draw(extra);
        }

        private void drawNANDGate(Graphics2D g, int x, int y, int height, int width) {
            drawANDGate(g, x, y, height, width);
            g.setColor(Color.BLACK);
            g.drawOval(x + width / 2 - 8, y - 4, 8, 8);
        }

        private void drawNORGate(Graphics2D g, int x, int y, int height, int width) {
            drawORGate(g, x, y, height, width);
            g.setColor(Color.BLACK);
            g.drawOval(x + width / 2 - 8, y - 4, 8, 8);
        }

        private void drawXNORGate(Graphics2D g, int x, int y, int height, int width) {
            drawXORGate(g, x, y, height, width);
            g.setColor(Color.BLACK);
            g.drawOval(x + width / 2 - 8, y - 4, 8, 8);
        }
    }
}
