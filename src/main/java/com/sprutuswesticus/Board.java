package com.sprutuswesticus;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.Arrays;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Board implements Serializable {
    private static final long serialVersionUID = 6090317075470884294L;

    static final double MARGIN = 30.0;
    static final String DEFAULT_SPECIFIC = "12x10:d1b1b1c11a113c14c1c11f321c21a2c1a2a3a2a211b12b11a3a2a1c3a132a123b1a3e33b1221a2a22a21a1222a1a1a2b0a11a1c1a1b";
    static final int DEFAULT_HEIGHT = 10;
    static final int DEFAULT_WIDTH = 10;

    private String specific;
    private int height, width;
    int[][] lines, solu, clues;
    private int issues;

    // search/result variables
    private boolean[][] adj,isloop, satisfiable;
    private boolean[] visited;
    private int[] tin, low;
    private int timer;
    private int flatlen;

    public Board() {
        this.specific(DEFAULT_SPECIFIC);
    }

    // empty intialization
    public Board(int width, int height) {
        initboard(width, height);
    }

    public Board(String spec) {
        this.specific(spec);
    }

    public String getspec() {
        return specific;
    }

    public int getIssue() {
        return issues;
    }

    public boolean specific(String spec) {
        spec = spec.trim();
        this.specific = spec;
        int colon = spec.indexOf(':');
        int x = spec.indexOf('x');
        initboard(Integer.parseInt(spec.substring(0, x)), Integer.parseInt(spec.substring(x+1,colon)));
        int row = 0;
        int col = 0;
        for (int cur = colon + 1; cur < spec.length(); cur++) {
            char c = spec.charAt(cur);
            if (c >= '0' && c <= '4') {
                this.clues[row][col++] = c - '0';
            } else if (c >= 'a' && c <= 'z') {
                col += c - 96;
            } else {
                return false;
            }
            if (col >= width + 1) {
                row++;
                col = col % (width + 1);
            }
        }
        if (!(row == height && col == width)) {
            return false;
        }
        return true;
    }

    // Initialize empty values of board with width and height
    private boolean initboard(int width, int height) {
        if (width > 0 && height > 0) {
            this.width = width;
            this.height = height;
            this.lines = new int[this.height][this.width];
            this.solu = new int[this.height][this.width];
            this.clues = new int[this.height + 1][this.width + 1];
            for (int[] row : this.clues) {
                Arrays.fill(row, -1);
            }
            this.satisfiable = new boolean[this.height + 1][this.width + 1];
            for (boolean[] row : this.satisfiable) {
                Arrays.fill(row, true);
            }
            this.isloop = new boolean[this.height][this.width];

            this.flatlen = (width + 1) * (height + 1);
            this.adj = new boolean[this.flatlen][4];
            this.visited = new boolean[this.flatlen];
            this.tin = new int[this.flatlen];
            this.low = new int[this.flatlen];
            this.issues = width * height;
            return true;
        }
        return false;
    }

    public boolean alter(Update up) {
        if (up == null) {
            return true;
        }
        if (up.r >= this.height || up.c >= this.width) {
            return false;
        }
        if(this.lines[up.r][up.c] == up.orientation) return true;
        if(up.orientation == 0) {
            this.issues += this.isloop[up.r][up.c] ? 0: 1;
            this.isloop[up.r][up.c] = false;
        }

        this.issues += this.lines[up.r][up.c] == 0 ? -1: 0;

        this.lines[up.r][up.c] = up.orientation;
        // update adjacency array
        int flat = (up.r * (width + 1)) + up.c;
        
        adj[flat][3] = up.orientation == -1;
        adj[flat + 1][2] = up.orientation == 1;
        adj[flat + width + 1][1] = up.orientation == 1;
        adj[flat + width + 2][0] = up.orientation == -1;
        // TODO: run issue checker
        issuecheck(up);
        
        System.out.println(this.issues);
        return true;
    }

    // todo make it return an update of wrong objects
    private boolean issuecheck(Update up) {
        clueIsSatisfiable(up.r, up.c);
        clueIsSatisfiable(up.r, up.c + 1);
        clueIsSatisfiable(up.r + 1, up.c);
        clueIsSatisfiable(up.r + 1, up.c + 1);
        loopcheck(up);
        return false;
    }


    // perform dfs bridge find algo
    //TODO: Call bridge_dfs only on 4 corners of the updated line
    private void loopcheck(Update up) {
        Arrays.fill(this.visited, false);
        Arrays.fill(this.tin, -1);
        Arrays.fill(this.low, -1);
        this.timer = 0;
        for (int i = 0; i < flatlen; i++) {
            if (!visited[i]) {
                bridge_dfs(i, -1);
            }
        }
    }

    private void bridge_dfs(int v, int p) {
        visited[v] = true;
        tin[v] = low[v] = timer++;
        int to;
        for (int i = 0; i < 4; i++) {
            if (adj[v][i]) {
                to = v + (i % 2) * 2 + (i < 2 ? -(this.width + 2) : this.width);
                if (to == p){
                    continue;
                }
                int r = Math.min(v/(width+1), to/(width+1));
                int c = Math.min(v%(width+1), to%(width+1));
                
                this.issues += this.isloop[r][c] ? 0 : 1;
                if (visited[to]) {
                    low[v] = Math.min(low[v], tin[to]);
                    isloop[r][c] = true;
                }else{
                    bridge_dfs(to, v);
                    low[v] = Math.min(low[v], low[to]);
                    isloop[r][c] = (low[to] <= tin[v]);
                    this.issues += this.isloop[r][c] ? 0 : -1;
                }
            }
        }

    }

    public String stringifylines() {
        String out = "";
        for (int[] row : this.lines) {
            out += Arrays.toString(row);
            out += "\n";
        }
        return out;
    }
    
    public String stringifygrid() {
        String out = "";
        for (int[] row : this.clues) {
            out += Arrays.toString(row);
            out += "\n";
        }
        return out;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void draw(GraphicsContext g) {
        double CLUE_RADIUS = 10.0;

        double w_canvas = g.getCanvas().getWidth();
        double h_canvas = g.getCanvas().getHeight();

        double w_cell = (w_canvas - 2 * MARGIN) / width;
        double h_cell = (h_canvas - 2 * MARGIN) / height;

        // Clear bg
        g.setFill(Color.rgb(43, 45, 66));
        g.fillRect(0, 0, w_canvas, h_canvas);

        // Draw bg grid
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1.0);
        for (int c = 0; c < width + 1; c++) {
            double x = MARGIN + w_cell * c;
            g.strokeLine(x, MARGIN, x, h_canvas - MARGIN);
        }
        for (int r = 0; r < height + 1; r++) {
            double y = MARGIN + h_cell * r;
            g.strokeLine(MARGIN, y, w_canvas - MARGIN, y);
        }

        // Draw lines
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                double x = MARGIN + w_cell * c;
                double y = MARGIN + h_cell * r;
                g.setStroke(Color.WHITE);
                g.setLineWidth(2.0);
                if (lines[r][c] == -1) {
                    g.strokeLine(x, y, x + w_cell, y + h_cell);
                } else if (lines[r][c] == 1) {
                    g.strokeLine(x, y + h_cell, x + w_cell, y);
                }
                if (this.isloop[r][c]) {
                    g.setStroke(Color.RED);
                    g.setLineWidth(1.5);
                    if (lines[r][c] == -1) {
                        g.strokeLine(x, y, x + w_cell, y + h_cell);
                    } else if (lines[r][c] == 1) {
                        g.strokeLine(x, y + h_cell, x + w_cell, y);
                    }
                }
            }
        }

        // Draw clues
        g.setFill(Color.LIGHTSLATEGRAY);
        g.setLineWidth(1.0);
        g.setFont(Font.font("Comic Sans MS", 12.0));
        for (int r = 0; r < height + 1; r++) {
            for (int c = 0; c < width + 1; c++) {
                if (clues[r][c] == -1) {
                    continue;
                }
                double c_x = MARGIN + w_cell * c;
                double c_y = MARGIN + h_cell * r;
                g.setStroke(satisfiable[r][c] ? Color.BLACK : Color.RED);
                g.fillOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2 * CLUE_RADIUS, 2 * CLUE_RADIUS);
                g.strokeOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2 * CLUE_RADIUS, 2 * CLUE_RADIUS);
                g.strokeText(Integer.toString(clues[r][c]), c_x - CLUE_RADIUS + 6, c_y - CLUE_RADIUS + 14);
            }
        }
    }

    private boolean clueIsSatisfiable(int r, int c) { 
        if(this.clues[r][c] == -1) return true;
        int count = 0;
        int maxcount = 0;
        if (r > 0 && c > 0) {
            if(lines[r - 1][c - 1] == -1){
                count++;
                maxcount++;
            }else if(lines[r-1][c-1] == 0){
                maxcount++;
            }
        }
        if (r < height && c > 0) {
            if(lines[r][c - 1] == 1){
                count++;
                maxcount++;
            }else if(lines[r][c-1] == 0){
                maxcount++;
            }
        }
        if (r > 0 && c < width) {
            if(lines[r - 1][c] == 1){
                count++;
                maxcount++;
            }else if(lines[r-1][c] == 0){
                maxcount++;
            }
        }
        if (r < height && c < width) {
            if(lines[r][c] == -1){
                count++;
                maxcount++;
            }else if(lines[r][c] == 0){
                maxcount++;
            }
        }
        this.issues += this.satisfiable[r][c] ? 0: -1;
        boolean ret = count <= clues[r][c] && maxcount >= clues[r][c];
        this.satisfiable[r][c] = ret;
        this.issues += ret ? 0 : 1;
        return ret;
    }

    
    private void generate_solu(int width, int height, int difficulty){
        int[][] sets = new int[height + 1][width + 1];
        initboard(width, height);
        boolean us, ds;
        for(int r =0; r < height + 1; r++) {
            for(int c = 0; c < width + 1; c++) {
                sets[r][c] = r*(width + 1) + c;
            }
        }
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                ds = sets[r][c] == sets[r+1][c+1];
                us = sets[r+1][c] == sets[r][c+1];
                assert !(ds && us);
            }
        }
        
    }
}
