package com.circuitsimulator.components;


public class TruthTableComponent extends Component {
    private static final long serialVersionUID = 1L;

    private String title = "Truth Table";
    private String[][] table;

    private int visualWidth = 420;
    private int visualHeight = 240;

    public TruthTableComponent(String id, String name) {
        super(id, name);
        this.componentTypeId = 50;
    }

    @Override
    public void evaluate() {
        
    }

    @Override
    public String getComponentType() {
        return "TRUTH_TABLE";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) return;
        this.title = title;
    }

    public String[][] getTable() {
        return table;
    }

    public void setTable(String[][] table) {
        this.table = table;
    }

    public int getVisualWidth() {
        return visualWidth;
    }

    public int getVisualHeight() {
        return visualHeight;
    }

    public void setVisualSize(int width, int height) {
        if (width > 0) this.visualWidth = width;
        if (height > 0) this.visualHeight = height;
    }
}

