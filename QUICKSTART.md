# Digital Circuit Simulator - Quick Start Guide

## 5-Minute Setup

### Step 1: Open Terminal/Command Prompt
Navigate to the project directory:
```
cd oops_prj_trial
```

### Step 2: Compile and Run

**Windows:**
```
build_and_run.bat
```

**Linux/macOS:**
```
bash build_and_run.sh
```

### Step 3: The Application Starts!
You should see the Digital Circuit Simulator window with a blank canvas.

---

## Your First Circuit - 2-Input AND Gate

1. **Add First Switch:**
   - Select "Switch" from dropdown
   - Click "Add"
   - Name it "Switch_A"
   - It appears on canvas (green circle = closed)

2. **Add Second Switch:**
   - Select "Switch" from dropdown
   - Click "Add"
   - Name it "Switch_B"

3. **Add AND Gate:**
   - Select "AND" from dropdown
   - Click "Add"
   - Name it "AND1"

4. **Add LED:**
   - Select "LED" from dropdown
   - Click "Add"
   - Name it "LED1"

5. **Connect Components:**
   - Click "Connect" button
   - Enter first node number: 1 (for Switch_A output)
   - Enter second node number: 2 (this will be used by AND input)
   - Repeat:
     - Connect Switch_B (node 2 to 3)
     - Connect AND to LED (node 4 to 5)

6. **Test It:**
   - Double-click Switch_A to toggle
   - Double-click Switch_B to toggle
   - Watch LEDs change
   - Click "Truth Table" to see all combinations

---

## Common Tasks

### Validate Your Circuit
```
Circuit → Validate (or toolbar: "Validate" button)
```

### See Truth Table
```
Circuit → Generate Truth Table (or toolbar: "Truth Table" button)
```

### Save Your Work
```
File → Save (or toolbar: Ctrl+S)
```

### Load Saved Circuit
```
File → Open (or toolbar: Ctrl+O)
```

### Delete Component
- Select component on canvas
- Press Delete key (or toolbar: "Delete" button)

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Delete | Remove selected component |
| Drag | Move component around |
| Double-click Switch | Toggle state |
| Double-click anywhere | Activate that component |

---

## Understanding the Interface

**Left/Center Area**: Canvas where you draw your circuit
**Top Toolbar**: 
- Component selector and Add button
- Connect button for node connections
- Delete button
- Validate, Simulate, Truth Table buttons

**Right Panel**: 
- Circuit information
- Component count
- Node count
- Connection list

**Bottom Bar**: Status messages and info

---

## Tips & Tricks

1. **Grid-based positioning**: Components snap to grid for cleaner layouts
2. **Component selection**: Click on a component to select it (highlighted in yellow)
3. **Drag to move**: Selected components can be dragged to new positions
4. **Undo limitation**: Use Ctrl+Z is not available; plan carefully
5. **Truth table insight**: Run truth table generation after each major change
6. **Save frequently**: Save your work often (use File → Save)
7. **Validate before deploying**: Always validate before declaring circuit complete

---

## Understanding Nodes

Nodes are numbered connection points:
- Each node has a unique integer ID (1, 2, 3, ...)
- Multiple components can share the same node
- Signal propagates through connected nodes
- Red line = HIGH signal (1)
- Black line = LOW signal (0)

**Default Behaviors:**
- Open LED = automatically connected to GND (0/LOW)
- Open switch = automatically connected to VCC (1/HIGH)

---

## Common Errors & Solutions

**"Component has no output connection"**
- Connect the component's output to an LED or another gate's input

**"Gate needs at least 2 inputs"**
- Multi-input gates require minimum 2 connections

**"SHORT CIRCUIT detected"**
- You have a HIGH signal directly connected to LOW (ground)
- Check your connections

**"Input not connected"**
- Some components don't have inputs connected
- Every switch input and gate input must be driven by another component

---

## Next Steps

After mastering basics:

1. **Build XOR Gate**: Combine AND, OR, NOT gates
2. **Create Half Adder**: 2-bit sum and carry output
3. **Try MUX/DEMUX**: Complex logic combinations
4. **Export Circuit**: Save as text for documentation
5. **Load & Modify**: Open saved circuits and improve them

---

## Troubleshooting

**App crashes on start:**
```bash
# Windows
echo %JAVA_HOME%

# Linux/Mac
echo $JAVA_HOME
```

**Compilation fails:**
- Check Java version: `java -version` (must be 8+)
- Delete bin folder contents and recompile
- Check for typos in file names

**Can't save circuits:**
- Check write permissions in project directory
- Ensure 'circuits' folder exists
- Try exporting as text instead

---

## Getting Help

1. Check the full README.md
2. Run validation to get specific error messages
3. Try the example circuits section
4. Export your circuit as text to review structure

---

**Ready to build amazing circuits! Happy simulating! 🎉**
