package com.circuitsimulator.gui;

import com.circuitsimulator.circuit.Circuit;
import com.circuitsimulator.circuit.Node;
import com.circuitsimulator.components.*;
import com.circuitsimulator.file.CircuitFileManager;
import com.circuitsimulator.validator.CircuitValidator;
import com.circuitsimulator.validator.TruthTableGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Main application window for the Digital Circuit Simulator
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private Circuit circuit;
    private CircuitPanel circuitPanel;
    private static final Color WHATSAPP_DARK = new Color(5, 51, 96);
    private static final Color WHATSAPP_GREEN = new Color(37, 211, 102);
    private static final Color WHATSAPP_LIGHT = new Color(37, 73, 122);
    private static final Color WHATSAPP_BG = new Color(14, 30, 67);
    private static final Color PANEL_BG = new Color(10, 18, 50);

    private JMenuBar menuBar;
    private JLabel statusLabel;
    private JLabel selectedComponentLabel;
    private String selectedComponentType = "-- Select Component --";
    private boolean readyToPlaceComponent = false;
    private String copiedComponentType = null;
    private Boolean copiedComponentState = null;
    private JButton validateButton;
    private JButton truthTableButton;
    private JTextArea infoPanel;

    public MainWindow() {
        setTitle("Digital Circuit Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        circuit = new Circuit("New Circuit");

        initializeUI();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(WHATSAPP_BG);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(WHATSAPP_BG);

        circuitPanel = new CircuitPanel(circuit);
        circuitPanel.setBackground(Color.WHITE);
        circuitPanel.setFocusable(true);
        installCanvasKeyBindings();
        circuitPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && readyToPlaceComponent) {
                    if (circuitPanel.getCompAt(e.getPoint()) == null) {
                        addComponentAt(e.getPoint());
                        readyToPlaceComponent = false;
                        selectedComponentType = "-- Select Component --";
                        updateSelectedComponentLabel();
                        circuitPanel.setPlacementMode(false, null);
                    }
                }
            }
        });
        bodyPanel.add(new JScrollPane(circuitPanel), BorderLayout.CENTER);
        bodyPanel.add(createDetailsPanel(), BorderLayout.EAST);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHATSAPP_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel leftIcons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftIcons.setOpaque(false);
        leftIcons.add(createIconToolButton("New", e -> newCircuit()));
        leftIcons.add(createIconToolButton("Save", e -> saveCircuit()));
        leftIcons.add(createIconToolButton("Open", e -> openCircuit()));
        leftIcons.add(createIconToolButton("Power", e -> System.exit(0)));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Digital Circuit Simulator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        selectedComponentLabel = new JLabel("Selected: --");
        selectedComponentLabel.setForeground(new Color(215, 235, 225));
        selectedComponentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedComponentLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        titlePanel.add(selectedComponentLabel, BorderLayout.SOUTH);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        header.add(leftIcons, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(rightActions, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout(0, 16));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(WHATSAPP_DARK);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel libraryLabel = new JLabel("Component Library");
        libraryLabel.setForeground(Color.WHITE);
        libraryLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        gridPanel.setOpaque(false);

        gridPanel.add(createComponentButton("Switch"));
        gridPanel.add(createComponentButton("LED"));
        gridPanel.add(createComponentButton("AND"));
        gridPanel.add(createComponentButton("OR"));
        gridPanel.add(createComponentButton("NOT"));
        gridPanel.add(createComponentButton("XOR"));
        gridPanel.add(createComponentButton("NAND"));
        gridPanel.add(createComponentButton("NOR"));
        gridPanel.add(createComponentButton("XNOR"));
        gridPanel.add(createComponentButton("VCC"));
        gridPanel.add(createComponentButton("GND"));

        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(createSidebarControlButton("Validate", e -> validateCircuit()));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(createSidebarControlButton("Truth Table", e -> generateTruthTable()));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(libraryLabel, BorderLayout.NORTH);
        topPanel.add(gridPanel, BorderLayout.CENTER);

        sidebar.add(topPanel, BorderLayout.CENTER);
        sidebar.add(controlPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setBackground(WHATSAPP_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.addActionListener(listener);
        return button;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBackground(WHATSAPP_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Circuit Info");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(220, 230, 255));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        infoPanel = new JTextArea();
        infoPanel.setEditable(false);
        infoPanel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoPanel.setBackground(new Color(7, 15, 35));
        infoPanel.setForeground(new Color(220, 230, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(6, 15, 45));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        statusLabel = new JLabel("Ready | No circuit loaded");
        statusLabel.setForeground(new Color(210, 220, 245));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        return bottomPanel;
    }

    private JButton createHeaderActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(255, 255, 255, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120)),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.addActionListener(listener);
        return button;
    }

    private void styleSidebarButton(JButton button) {
        button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBackground(WHATSAPP_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }

    private void styleActionButton(JButton button) {
        button.setBackground(WHATSAPP_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private JButton createIconToolButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setIcon(createToolbarIcon(text));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(12, 120, 105));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.addActionListener(listener);
        return button;
    }

    private JButton createComponentButton(String type) {
        JButton button = new JButton(type, createComponentIcon(type, 30, 30));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(17, 67, 112));
        button.setFocusPainted(false);
        button.setToolTipText("Click to select and place this component");
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        button.addActionListener(e -> {
            selectedComponentType = type;
            readyToPlaceComponent = true;
            updateSelectedComponentLabel();
            statusLabel.setText(type + " selected. Click on the canvas to place.");
            if (circuitPanel != null) {
                circuitPanel.setPlacementMode(true, type);
                circuitPanel.requestFocusInWindow();
            }
        });
        return button;
    }

    private JButton createSidebarControlButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBackground(WHATSAPP_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.addActionListener(listener);
        return button;
    }

    private void updateSelectedComponentLabel() {
        if (selectedComponentLabel != null) {
            if (readyToPlaceComponent) {
                selectedComponentLabel.setText("Ready to place: " + selectedComponentType);
            } else {
                selectedComponentLabel.setText("Selected: " + selectedComponentType);
            }
        }
    }

    private void addComponentAt(Point point) {
        String selected = selectedComponentType;
        if (selected == null || selected.equals("-- Select Component --")) {
            JOptionPane.showMessageDialog(this, "Please select a component type",
                    "No Component Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = selected.toLowerCase() + "_" + (circuit.getTotalComponents() + 1);
        String compName = defaultName;
        String compId = "C" + circuit.getTotalComponents();
        com.circuitsimulator.components.Component comp = null;

        try {
            comp = createComponentByType(selected, compId, compName, null);
            if (comp != null) {
                comp.setPosition(point.x, point.y);
                circuit.addComponent(comp);
                circuitPanel.repaint();
                updateInfo();
                readyToPlaceComponent = false;
                selectedComponentType = "-- Select Component --";
                updateSelectedComponentLabel();
                circuitPanel.setPlacementMode(false, null);
                statusLabel.setText("Added: " + comp.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding component: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private com.circuitsimulator.components.Component createComponentByType(String type, String compId, String compName, Boolean state) {
        switch (type) {
            case "Switch":
                Switch sw = new Switch(compId, compName);
                if (state != null && state != sw.isOpen()) {
                    sw.toggle();
                }
                return sw;
            case "LED":
                return new LED(compId, compName);
            case "AND":
                return new ANDGate(compId, compName, 2);
            case "OR":
                return new ORGate(compId, compName, 2);
            case "NOT":
                return new NOTGate(compId, compName);
            case "XOR":
                return new XORGate(compId, compName, 2);
            case "NAND":
                return new NANDGate(compId, compName, 2);
            case "NOR":
                return new NORGate(compId, compName, 2);
            case "XNOR":
                return new XNORGate(compId, compName, 2);
            case "VCC":
                return new VCC(compId, compName);
            case "GND":
                return new GND(compId, compName);
            default:
                return null;
        }
    }

    private void copySelectedComponent() {
        com.circuitsimulator.components.Component selected = circuitPanel.getSelectedComponent();
        if (selected == null) {
            statusLabel.setText("No component selected to copy.");
            return;
        }

        copiedComponentType = selected.getComponentType();
        if (selected instanceof Switch) {
            copiedComponentState = ((Switch) selected).isOpen();
        } else if (selected instanceof LED) {
            copiedComponentState = ((LED) selected).isLit();
        } else {
            copiedComponentState = null;
        }
        statusLabel.setText("Copied " + copiedComponentType + ". Press Ctrl+V to paste.");
    }

    private void pasteCopiedComponent(Point point) {
        if (copiedComponentType == null) {
            statusLabel.setText("Clipboard is empty.");
            return;
        }

        String compName = copiedComponentType.toLowerCase() + "_" + (circuit.getTotalComponents() + 1);
        String compId = "C" + circuit.getTotalComponents();
        com.circuitsimulator.components.Component comp = createComponentByType(copiedComponentType, compId, compName, copiedComponentState);
        if (comp != null) {
            comp.setPosition(point.x, point.y);
            circuit.addComponent(comp);
            circuitPanel.repaint();
            updateInfo();
            statusLabel.setText("Pasted: " + comp.toString());
        }
    }

    private Icon createToolbarIcon(String name) {
        return new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)) {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                switch (name) {
                    case "New":
                        g2.fillRect(x + 6, y + 8, 20, 16);
                        g2.setColor(new Color(7, 94, 84));
                        g2.drawLine(x + 6, y + 8, x + 26, y + 8);
                        g2.drawLine(x + 6, y + 8, x + 6, y + 24);
                        break;
                    case "Save":
                        g2.fillRect(x + 6, y + 6, 20, 18);
                        g2.setColor(new Color(7, 94, 84));
                        g2.drawRect(x + 6, y + 6, 20, 18);
                        g2.fillRect(x + 10, y + 10, 12, 8);
                        break;
                    case "Open":
                        g2.drawOval(x + 8, y + 8, 16, 16);
                        g2.drawLine(x + 16, y + 8, x + 16, y + 24);
                        break;
                    case "Power":
                        g2.drawOval(x + 8, y + 6, 16, 16);
                        g2.drawLine(x + 16, y + 6, x + 16, y + 12);
                        break;
                    default:
                        g2.fillRect(x + 10, y + 10, 12, 12);
                }
                g2.dispose();
            }
        };
    }

    private Icon createComponentIcon(String type, int width, int height) {
        return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)) {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bg = new GradientPaint(x, y, new Color(14, 34, 82), x + width, y + height, new Color(8, 18, 48));
                g2.setPaint(bg);
                g2.fillRoundRect(x, y, width, height, 10, 10);

                g2.setColor(new Color(160, 210, 255));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (type) {
                    case "Switch":
                        g2.drawLine(x + 5, y + height / 2, x + width - 5, y + height / 2);
                        g2.drawLine(x + width / 2, y + height / 2, x + width / 2 + 7, y + height / 2 - 9);
                        break;
                    case "LED":
                        g2.fillOval(x + 8, y + 8, 14, 14);
                        g2.setColor(new Color(230, 240, 180));
                        g2.fillOval(x + 10, y + 10, 10, 10);
                        g2.setColor(new Color(160, 210, 255));
                        g2.drawLine(x + 14, y + 6, x + 18, y + 2);
                        g2.drawLine(x + 18, y + 6, x + 22, y + 2);
                        break;
                    case "AND":
                        g2.drawRect(x + 4, y + 6, 10, height - 12);
                        g2.drawArc(x + 6, y + 6, 16, height - 12, -90, 180);
                        break;
                    case "OR":
                        g2.drawArc(x + 2, y + 6, 14, height - 12, -90, 180);
                        g2.drawArc(x + 6, y + 6, 18, height - 12, -90, 180);
                        g2.drawArc(x + 6, y + 6, 18, height - 12, -90, 180);
                        break;
                    case "NOT":
                        int[] xPts = {x + 6, x + width - 8, x + 6};
                        int[] yPts = {y + 6, y + height / 2, y + height - 6};
                        g2.drawPolygon(xPts, yPts, 3);
                        g2.drawOval(x + width - 11, y + height / 2 - 4, 8, 8);
                        break;
                    case "XOR":
                        g2.drawArc(x + 2, y + 6, 14, height - 12, -90, 180);
                        g2.drawArc(x + 6, y + 6, 18, height - 12, -90, 180);
                        g2.drawArc(x + 8, y + 6, 18, height - 12, -90, 180);
                        break;
                    case "NAND":
                        g2.drawRect(x + 4, y + 6, 10, height - 12);
                        g2.drawArc(x + 6, y + 6, 16, height - 12, -90, 180);
                        g2.drawOval(x + width - 10, y + height / 2 - 4, 8, 8);
                        break;
                    case "NOR":
                        g2.drawArc(x + 2, y + 6, 14, height - 12, -90, 180);
                        g2.drawArc(x + 6, y + 6, 18, height - 12, -90, 180);
                        g2.drawOval(x + width - 10, y + height / 2 - 4, 8, 8);
                        break;
                    case "XNOR":
                        g2.drawArc(x + 2, y + 6, 14, height - 12, -90, 180);
                        g2.drawArc(x + 6, y + 6, 18, height - 12, -90, 180);
                        g2.drawArc(x + 8, y + 6, 18, height - 12, -90, 180);
                        g2.drawOval(x + width - 10, y + height / 2 - 4, 8, 8);
                        break;
                    case "VCC":
                        g2.drawLine(x + width / 2, y + 4, x + width / 2, y + height - 6);
                        g2.drawLine(x + width / 2 - 6, y + 8, x + width / 2 + 6, y + 8);
                        g2.drawLine(x + width / 2 - 4, y + 12, x + width / 2 + 4, y + 12);
                        break;
                    case "GND":
                        g2.drawLine(x + width / 2, y + 4, x + width / 2, y + height - 10);
                        g2.drawLine(x + width / 2 - 6, y + height - 10, x + width / 2 + 6, y + height - 10);
                        g2.drawLine(x + width / 2 - 4, y + height - 6, x + width / 2 + 4, y + height - 6);
                        break;
                    case "Wire":
                        g2.drawLine(x + 4, y + height / 2, x + width - 4, y + height / 2);
                        break;
                    default:
                        g2.drawRoundRect(x + 6, y + 6, width - 12, height - 12, 8, 8);
                }
                g2.dispose();
            }
        };
    }

    private void addComponent() {
        String selected = selectedComponentType;
        if (selected == null || selected.equals("-- Select Component --")) {
            JOptionPane.showMessageDialog(this, "Please select a component type",
                    "No Component Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Default component name
        String defaultName = selected.toLowerCase() + "_" + (circuit.getTotalComponents() + 1);
        
        // Show input dialog with parent frame to ensure it appears on top
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter component name:");
        JTextField textField = new JTextField(defaultName, 20);
        panel.add(label);
        panel.add(textField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Add " + selected, JOptionPane.OK_CANCEL_OPTION);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String compName = textField.getText().trim();
        if (compName.isEmpty()) {
            compName = defaultName;
        }

        String compId = "C" + circuit.getTotalComponents();
        com.circuitsimulator.components.Component comp = null;

        try {
            switch (selected) {
                case "Switch":
                    comp = new Switch(compId, compName);
                    break;
                case "LED":
                    comp = new LED(compId, compName);
                    break;
                case "AND":
                    comp = new ANDGate(compId, compName, 2);
                    break;
                case "OR":
                    comp = new ORGate(compId, compName, 2);
                    break;
                case "NOT":
                    comp = new NOTGate(compId, compName);
                    break;
                case "XOR":
                    comp = new XORGate(compId, compName, 2);
                    break;
                case "NAND":
                    comp = new NANDGate(compId, compName, 2);
                    break;
                case "NOR":
                    comp = new NORGate(compId, compName, 2);
                    break;
                case "XNOR":
                    comp = new XNORGate(compId, compName, 2);
                    break;
                case "VCC":
                    comp = new VCC(compId, compName);
                    break;
                case "GND":
                    comp = new GND(compId, compName);
                    break;
            }

            if (comp != null) {
                // Get mouse position for component placement
                Point mousePos = circuitPanel.getMousePosition();
                if (mousePos == null) {
                    mousePos = new Point(100, 100);
                }
                comp.setPosition(mousePos.x, mousePos.y);

                circuit.addComponent(comp);
                circuitPanel.repaint();
                updateInfo();
                statusLabel.setText("Added: " + comp.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding component: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        com.circuitsimulator.components.Component selected = circuitPanel.getSelectedComponent();
        if (selected != null) {
            circuit.removeComponent(selected);
            circuitPanel.clearSelection();
            circuit.simulate();
            circuitPanel.repaint();
            updateInfo();
            statusLabel.setText("Deleted: " + selected.toString());
        } else {
            JOptionPane.showMessageDialog(this, "Please select a component to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void validateCircuit() {
        CircuitValidator validator = new CircuitValidator(circuit);
        if (validator.validate()) {
            JOptionPane.showMessageDialog(this, "Circuit is VALID!\n\n" + 
                    validator.getValidationReport(),
                    "Validation Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Validation FAILED!\n\n" + 
                    validator.getValidationReport(),
                    "Validation Result", JOptionPane.WARNING_MESSAGE);
        }
        statusLabel.setText("Validation completed");
    }

    private void simulateCircuit() {
        circuit.simulate();
        circuitPanel.repaint();
        updateInfo();
        statusLabel.setText("Simulation completed");
    }

    private void generateTruthTable() {
        TruthTableGenerator generator = new TruthTableGenerator(circuit);
        com.circuitsimulator.circuit.Node targetNode = circuitPanel.getSelectedTargetNode();
        String[][] tableData = (targetNode != null)
                ? generator.generateTruthTable(targetNode)
                : generator.generateTruthTable();

        // Find existing TruthTableComponent on canvas
        TruthTableComponent existing = null;
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof TruthTableComponent) {
                existing = (TruthTableComponent) comp;
                break;
            }
        }

        String title = (targetNode != null) ? "Truth Table (Selected Node)" : "Truth Table";

        if (existing == null) {
            String compId = "TT" + circuit.getTotalComponents();
            TruthTableComponent tt = new TruthTableComponent(compId, "truth_table");
            tt.setTitle(title);
            tt.setTable(tableData);

            // Place on grid near top-left of visible area (reasonable default)
            Point mousePos = circuitPanel.getMousePosition();
            if (mousePos == null) {
                mousePos = new Point(220, 160);
            }
            tt.setPosition(mousePos.x, mousePos.y);
            circuit.addComponent(tt);
            statusLabel.setText("Truth table added to canvas");
        } else {
            existing.setTitle(title);
            existing.setTable(tableData);
            statusLabel.setText("Truth table updated");
        }

        circuitPanel.repaint();
    }

    private void newCircuit() {
        String name = JOptionPane.showInputDialog(this, "Enter circuit name:", "New Circuit");
        if (name != null) {
            circuit.clear();
            circuit.setCircuitName(name);
            circuitPanel.repaint();
            updateInfo();
            statusLabel.setText("New circuit: " + name);
        }
    }

    private void installCanvasKeyBindings() {
        InputMap inputMap = circuitPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = circuitPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelection");
        actionMap.put("deleteSelection", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!circuitPanel.deleteSelectedConnection()) {
                    deleteSelected();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copyComponent");
        actionMap.put("copyComponent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedComponent();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelPlacement");
        actionMap.put("cancelPlacement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (readyToPlaceComponent) {
                    readyToPlaceComponent = false;
                    selectedComponentType = "-- Select Component --";
                    updateSelectedComponentLabel();
                    circuitPanel.setPlacementMode(false, null);
                    statusLabel.setText("Placement cancelled");
                }
            }
        });
    }

    private void saveCircuit() {
        JFileChooser chooser = new JFileChooser("circuits");
        chooser.setFileFilter(new FileNameExtensionFilter("Circuit files (*.cir)", "cir"));
        
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".cir")) {
                file = new File(file.getAbsolutePath() + ".cir");
            }
            
            if (CircuitFileManager.saveCircuit(circuit, file.getAbsolutePath())) {
                JOptionPane.showMessageDialog(this, "Circuit saved successfully",
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Saved: " + file.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save circuit",
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCircuit() {
        JFileChooser chooser = new JFileChooser("circuits");
        chooser.setFileFilter(new FileNameExtensionFilter("Circuit files (*.cir)", "cir"));
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Circuit loadedCircuit = CircuitFileManager.loadCircuit(file.getAbsolutePath());
            
            if (loadedCircuit != null) {
                circuit = loadedCircuit;
                circuitPanel.setCircuit(circuit);
                circuitPanel.repaint();
                updateInfo();
                statusLabel.setText("Loaded: " + file.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load circuit",
                        "Load Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAsText() {
        JFileChooser chooser = new JFileChooser("circuits");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            CircuitFileManager.exportAsText(circuit, file.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Circuit exported successfully",
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Exported: " + file.getName());
        }
    }

    private void clearCircuit() {
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to clear the entire circuit?",
                "Confirm Clear", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            circuit.clear();
            circuitPanel.repaint();
            updateInfo();
            statusLabel.setText("Circuit cleared");
        }
    }

    private void updateInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Circuit: ").append(circuit.getCircuitName()).append("\n\n");
        info.append("Components: ").append(circuit.getTotalComponents()).append("\n");
        info.append("Nodes: ").append(circuit.getTotalNodes()).append("\n");
        info.append("Connections: ").append(circuit.getConnections().size()).append("\n\n");

        int switches = 0, leds = 0, gates = 0;
        for (com.circuitsimulator.components.Component comp : circuit.getComponents()) {
            if (comp instanceof Switch) switches++;
            else if (comp instanceof LED) leds++;
            else if (comp instanceof LogicGate) gates++;
        }

        info.append("Switches: ").append(switches).append("\n");
        info.append("LEDs: ").append(leds).append("\n");
        info.append("Gates: ").append(gates).append("\n");

        infoPanel.setText(info.toString());
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Digital Circuit Simulator v1.0\n\n" +
                "Design and simulate digital logic circuits\n" +
                "with gates, switches, and LEDs.\n\n" +
                "Features:\n" +
                "- Add logic gates and components\n" +
                "- Connect components with numbered nodes\n" +
                "- Interactive switches and LED display\n" +
                "- Circuit validation\n" +
                "- Truth table generation\n" +
                "- Save and load circuits\n",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
