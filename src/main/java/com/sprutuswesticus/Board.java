package com.sprutuswesticus;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.Arrays;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Board implements Serializable {
    private static final long serialVersionUID = 6090317075470884294L;

    static final double MARGIN = 30.0;
    static final int DEFAULT_HEIGHT = 10;
    static final int DEFAULT_WIDTH = 10;

    private int height, width;
    int[][] lines, clues;

    // search variables
    private boolean[][] adj,isloop;
    private boolean[] visited;
    private int[] tin, low;
    private int timer;
    private int flatlen;

    public Board() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // empty intialization
    public Board(int width, int height) {
        initboard(width, height);
    }

    public Board(String spec) {
        this.specific(spec);
    }

    public boolean specific(String spec) {
        spec = spec.trim();
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
            this.clues = new int[this.height + 1][this.width + 1];
            for (int[] row : this.clues) {
                Arrays.fill(row, -1);
            }

            this.isloop = new boolean[this.height][this.width];

            this.flatlen = (width + 1) * (height + 1);
            this.adj = new boolean[this.flatlen][4];
            this.visited = new boolean[this.flatlen];
            this.tin = new int[this.flatlen];
            this.low = new int[this.flatlen];
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
        this.lines[up.r][up.c] = up.orientation;
        // update adjacency array
        int flat = (up.r * (width + 1)) + up.c;
        if (up.orientation == -1) {
            adj[flat][3] = true;
            adj[flat + 1][2] = false;
            adj[flat + width + 1][1] = false;
            adj[flat + width + 2][0] = true;
        } else if (up.orientation == 1) {
            adj[flat][3] = false;
            adj[flat + 1][2] = true;
            adj[flat + width + 1][1] = true;
            adj[flat + width + 2][0] = false;
        } else {
            adj[flat][3] = false;
            adj[flat + 1][2] = false;
            adj[flat + width + 1][1] = false;
            adj[flat + width + 2][0] = false;
        }
        // TODO: run issue checker
        issuecheck(up);

        return true;
    }

    // todo make it return an update of wrong objects
    private boolean issuecheck(Update up) {
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
                if (visited[to]) {
                    low[v] = Math.min(low[v], tin[to]);
                    isloop[Math.min(v/(width+1), to/(width+1))][Math.min(v%(width+1), to%(width+1))] 
                    = true;
                }else{
                    bridge_dfs(to, v);
                    low[v] = Math.min(low[v], low[to]);
                    isloop[Math.min(v/(width+1), to/(width+1))][Math.min(v%(width+1), to%(width+1))] 
                    = (low[to] <= tin[v]);
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
        double CLUE_RADIUS = 12.0;

        double w_canvas = g.getCanvas().getWidth();
        double h_canvas = g.getCanvas().getHeight();

        double w_cell = (w_canvas - 2 * MARGIN) / width;
        double h_cell = (h_canvas - 2 * MARGIN) / height;

        // Clear bg
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w_canvas, h_canvas);

        // Draw bg grid
        g.setStroke(Color.LIGHTGRAY);
        for (int c = 0; c < width + 1; c++) {
            double x = MARGIN + w_cell * c;
            g.strokeLine(x, MARGIN, x, h_canvas - MARGIN);
        }
        for (int r = 0; r < height + 1; r++) {
            double y = MARGIN + h_cell * r;
            g.strokeLine(MARGIN, y, w_canvas - MARGIN, y);
        }

        // Draw lines
        g.setStroke(Color.BLACK);
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                double x = MARGIN + w_cell * c;
                double y = MARGIN + h_cell * r;
                g.setStroke(this.isloop[r][c] ? Color.RED: Color.BLACK);
                if (lines[r][c] == -1) {
                    g.strokeLine(x, y, x + w_cell, y + h_cell);
                } else if (lines[r][c] == 1) {
                    g.strokeLine(x, y + h_cell, x + w_cell, y);
                }
            }
        }

        // Draw clues
        g.setFill(Color.WHITE);
        for (int r = 0; r < height + 1; r++) {
            for (int c = 0; c < width + 1; c++) {
                if (clues[r][c] == -1) {
                    continue;
                }
                double c_x = MARGIN + w_cell * c;
                double c_y = MARGIN + h_cell * r;
                g.setStroke(clueIsSatisfied(r, c) ? Color.BLACK : Color.RED);
                g.fillOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2 * CLUE_RADIUS, 2 * CLUE_RADIUS);
                g.strokeOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2 * CLUE_RADIUS, 2 * CLUE_RADIUS);
                g.strokeText(Integer.toString(clues[r][c]), c_x - CLUE_RADIUS + 8, c_y - CLUE_RADIUS + 16);
            }
        }
    }

    private boolean clueIsSatisfied(int r, int c) {
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
        return count <= clues[r][c] && maxcount >= clues[r][c];
    }
}
