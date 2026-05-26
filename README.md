# Digital Circuit Simulator

A powerful Java Swing-based GUI application for designing, simulating, and analyzing digital logic circuits.

## Features

- **Visual Circuit Design**: Drag-and-drop interface to add components and connections
- **Logic Gates**: Support for AND, OR, NOT, XOR, NAND, NOR, XNOR gates
- **Interactive Components**: 
  - Switches (toggle between open/closed states)
  - LEDs (change color to indicate on/off status)
  - Logic gates with multiple inputs
- **Circuit Validation**: Automatic detection of design errors and short circuits
- **Truth Table Generation**: Generate complete truth tables for all input combinations
- **Signal Simulation**: Real-time signal propagation through the circuit
- **Save & Load**: Persist circuits to files for later use
- **Import/Export**: Export circuit details as text files

## System Requirements

- **Java Development Kit (JDK)** 8 or higher
- **Operating System**: Windows, Linux, or macOS
- **Screen Resolution**: Minimum 1400x900 pixels recommended

## Project Structure

```
oops_prj_trial/
├── src/
│   └── com/circuitsimulator/
│       ├── gui/
│       │   ├── MainWindow.java        # Main application window
│       │   └── CircuitPanel.java      # Drawing canvas for circuits
│       ├── components/
│       │   ├── Component.java          # Abstract base class
│       │   ├── Switch.java             # Interactive switch
│       │   ├── LED.java                # Visual LED display
│       │   ├── LogicGate.java          # Abstract logic gate
│       │   ├── ANDGate.java
│       │   ├── ORGate.java
│       │   ├── NOTGate.java
│       │   ├── XORGate.java
│       │   ├── NANDGate.java
│       │   ├── NORGate.java
│       │   └── XNORGate.java
│       ├── circuit/
│       │   ├── Circuit.java            # Main circuit manager
│       │   ├── Node.java               # Circuit node (pin)
│       │   └── Connection.java         # Connection between nodes
│       ├── validator/
│       │   ├── CircuitValidator.java   # Circuit validation
│       │   └── TruthTableGenerator.java # Truth table generation
│       └── file/
│           └── CircuitFileManager.java # Save/Load functionality
├── bin/                                 # Compiled .class files
├── circuits/                            # Saved circuit files directory
├── build_and_run.bat                   # Windows build and run script
├── build_and_run.sh                    # Linux/Mac build and run script
├── compile.bat                         # Windows compile only script
├── compile.sh                          # Linux/Mac compile only script
└── README.md                           # This file
```

## Compilation & Execution

### Windows

**Option 1: Build and Run (One Step)**
```bash
build_and_run.bat
```

**Option 2: Compile Only**
```bash
compile.bat
```

Then run manually:
```bash
java -cp bin com.circuitsimulator.gui.MainWindow
```

### Linux / macOS

**Option 1: Build and Run (One Step)**
```bash
bash build_and_run.sh
```

**Option 2: Compile Only**
```bash
bash compile.sh
```

Then run manually:
```bash
java -cp bin com.circuitsimulator.gui.MainWindow
```

## User Guide

### Getting Started

1. **Launch the Application**
   - Run the build script or execute the Java main class

2. **Creating a New Circuit**
   - Go to `File → New` and enter circuit name
   - Or use the default "New Circuit"

3. **Adding Components**

   **Using Toolbar:**
   - Select component type from dropdown (Switch, LED, AND, OR, NOT, XOR, NAND, NOR, XNOR)
   - Click "Add" button
   - Enter component name
   - Component will appear at mouse position

   **Using Menu:**
   - Add components through right-click context menu or toolbar

4. **Connecting Components**

   - Click "Connect" button
   - Enter first node number (source)
   - Enter second node number (destination)
   - Connection is created and displayed

   **Node Numbering Scheme:**
   - Use numeric IDs for all nodes (1, 2, 3, etc.)
   - Open LED connections default to GND (LOW)
   - Open switch connections default to VCC (HIGH)

5. **Interactive Simulation**

   - **Double-click on switches** to toggle between open/closed states
   - **LEDs automatically update** to show current state
   - **Red color** = HIGH (1)
   - **Gray color** = LOW (0)

6. **Component Interaction**

   - **Switches:**
     - Green circle = CLOSED (conducting)
     - Red circle = OPEN (non-conducting)
     - Double-click to toggle

   - **LEDs:**
     - Bright red = ON (receiving HIGH signal)
     - Dark gray = OFF (receiving LOW signal)

   - **Logic Gates:**
     - Blue rectangles with gate type label
     - Can be dragged to new positions
     - Multiple input support

### Circuit Operations

**Validation**
- Click "Validate" button or `Circuit → Validate`
- Checks for:
  - Incomplete connections
  - Short circuits (0 resistance from HIGH to LOW)
  - Undriven inputs
  - Missing outputs

**Simulation**
- Click "Simulate" button or `Circuit → Simulate`
- Propagates signals through all components
- Updates all outputs based on current inputs
- All LEDs will display the result

**Truth Table Generation**
- Click "Truth Table" button or `Circuit → Generate Truth Table`
- Automatically tests all possible input combinations
- Shows output states for each combination
- Useful for verification and testing

### File Operations

**Save Circuit**
- `File → Save`
- Saves circuit in binary format (.cir)
- Preserves all components, connections, and settings
- Default location: `circuits/` directory

**Load Circuit**
- `File → Open`
- Loads previously saved circuit
- All components and connections restored

**Export as Text**
- `File → Export as Text`
- Exports circuit information as readable text
- Lists all components, nodes, and connections
- Useful for documentation

**New Circuit**
- `File → New`
- Clears all components and connections
- Starts fresh with given name

### Keyboard Shortcuts

| Key | Action |
|-----|--------|
| Delete | Remove selected component |
| Double-click | Toggle switch state |
| Drag | Move component |

### Menu Reference

**File Menu**
- New - Create new circuit
- Open - Load saved circuit
- Save - Save current circuit
- Export as Text - Export to text file
- Exit - Close application

**Edit Menu**
- Clear All - Remove all components
- Delete Selected - Remove selected component

**Circuit Menu**
- Validate - Check circuit for errors
- Simulate - Run simulation
- Generate Truth Table - Create truth table

**Help Menu**
- About - Application information

## Node Numbering System

The circuit uses a number-based connection scheme where each connection point (node) has a unique integer ID.

### Rules:
1. Each node represents a connection point in the circuit
2. Nodes are identified by unique integer numbers
3. Multiple components can share the same node
4. Nodes can carry either HIGH (1) or LOW (0) signals

### Defaults:
- **Unconnected LED inputs** → Automatically set to GND (LOW / 0)
- **Unconnected switch outputs** → Automatically set to VCC (HIGH / 1)

## Component Details

### Switches (SPST)
- **Type**: Input source
- **Function**: Provides variable input signal
- **States**: OPEN (HIGH/1) or CLOSED (LOW/0)
- **Interaction**: Double-click to toggle

### LEDs
- **Type**: Output indicator
- **Function**: Displays output signal status
- **Visual**: Color change indicates state
- **Colors**: Red (ON/1), Gray (OFF/0)

### AND Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: Output HIGH only when ALL inputs are HIGH
- **Truth Table**: See truth table generation feature

### OR Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: Output HIGH if ANY input is HIGH

### NOT Gate (Inverter)
- **Input**: 1
- **Output**: 1
- **Function**: Output is inverse of input

### XOR Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: Output HIGH when ODD number of inputs are HIGH

### NAND Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: NOT(AND) - Output is LOW when ALL inputs are HIGH

### NOR Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: NOT(OR) - Output is LOW when ANY input is HIGH

### XNOR Gate
- **Input**: 2+ inputs
- **Output**: 1
- **Function**: NOT(XOR) - Output is HIGH when EVEN number of inputs are HIGH

## Features in Detail

### Circuit Validation

The validator checks for:

1. **Structural Errors**
   - Missing input/output connections
   - Unconnected components

2. **Short Circuit Detection**
   - Detects 0-resistance connections from HIGH to LOW
   - Prevents invalid electrical states

3. **Data Flow Issues**
   - Identifies undriven inputs
   - Finds potential floating nodes
   - Checks for circular dependencies

### Truth Table Generation

- Generates all 2^n combinations (n = number of switches)
- Tests each combination automatically
- Shows outputs for all combinations
- Formatted table display with borders
- Useful for verification and debugging

### Save/Load System

**Binary Format (.cir)**
- Preserves all circuit information
- Efficient storage
- Fast load/save operations
- Version compatible

**Text Export (.txt)**
- Human-readable format
- Component details
- Connection information
- Node states

## Example Circuits

### Example 1: Simple AND Gate Circuit
1. Add 2 Switches (S1, S2)
2. Add AND Gate (G1)
3. Add LED (L1)
4. Connect:
   - S1 output → AND input 1
   - S2 output → AND input 2
   - AND output → LED input
5. Double-click switches to test
6. Generate truth table to verify

### Example 2: Half Adder Circuit
1. Add 2 Switches (A, B)
2. Add XOR Gate (Sum)
3. Add AND Gate (Carry)
4. Add 2 LEDs (SUM, CARRY)
5. Connect inputs and outputs
6. Test all 4 combinations

### Example 3: Logic Expression Implementation
1. Implement circuit for: (A AND B) OR (NOT C)
2. Add 3 Switches, AND Gate, NOT Gate, OR Gate, 1 LED
3. Build the logic structure
4. Verify with truth table

## Troubleshooting

**Application won't start**
- Check Java installation: `java -version`
- Ensure JDK is installed (not just JRE)
- Check PATH environment variable

**Compilation errors**
- Ensure all source files are present
- Use compile scripts in project root
- Check Java version compatibility

**Circuit won't simulate**
- Click "Validate" to check for errors
- Ensure all inputs are connected
- Verify node numbers are unique where needed

**Short circuit warning**
- Check for conflicting connections
- Verify node states are appropriate
- Review circuit logic

## Technical Details

### Signal Representation
- **HIGH (1)**: Logic state representing voltage
- **LOW (0)**: Logic state representing ground

### Component Architecture
- All components inherit from base `Component` class
- Each has input and output nodes
- `evaluate()` method calculates output from inputs

### File Format
- Circuits saved as serialized Java objects
- Supports future extensions
- Backward compatible

## Limitations

- Combinational logic only (no flip-flops, latches)
- No analog components or voltage simulation
- Ideal gates (no propagation delays)
- No bus or multi-bit signals
- Maximum practical components: ~50 (GUI performance dependent)

## Future Enhancements

- Sequential logic components (Flip-flops, Latches)
- Timing analysis and delay simulation
- More detailed gate properties (propagation delay, power)
- Custom component definitions
- Circuit library and templates
- Integrated circuit (IC) support
- Oscilloscope/waveform display
- Schematic view (in addition to block diagram)

## License & Credits

This is an educational project demonstrating:
- Object-oriented design principles
- Swing GUI programming
- Digital logic simulation
- Data persistence

## Support

For issues, questions, or suggestions:
1. Check the Troubleshooting section
2. Review the User Guide
3. Validate your circuit design
4. Check error messages in console

---

**Version**: 1.0  
**Last Updated**: April 2026  
**Platform**: Java (Cross-platform compatible)
