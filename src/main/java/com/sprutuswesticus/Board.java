package com.sprutuswesticus;

import java.util.Arrays;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Board {
    static final int DEFAULT_HEIGHT = 10;
    static final int DEFAULT_WIDTH = 10;
    private int height, width;
    int[][] lines;
    int[][] clues;

    public Board() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // empty intialization
    public Board(int width, int height) {
        this.height = height;
        this.width = width;
        this.lines = new int[height][width];
        this.clues = new int[height + 1][width + 1];
    }

    public Board(String spec) {
        this.specific(spec);
    }

    public boolean specific(String spec) {
        spec = spec.trim();
        int colon = spec.indexOf(':');
        int x = spec.indexOf('x');
        this.width = Integer.parseInt(spec.substring(0, x));
        this.height = Integer.parseInt(spec.substring(x + 1, colon));
        this.lines = new int[this.height][this.width];
        this.clues = new int[this.height + 1][this.width + 1];
        for (int[] row : clues) {
            Arrays.fill(row, -1);
        }

        int row = 0;
        int col = 0;
        for (int cur = colon + 1; cur < spec.length(); cur++) {
            char c = spec.charAt(cur);
            if (c >= '0' && c <= '4') {
                this.clues[row][col] = c - '0';
                col++;
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

    public boolean alter(Update up) {
        if (up == null) {
            return true;
        }
        if (up.x >= this.height || up.y >= this.width) {
            return false;
        }
        this.lines[up.x][up.y] = up.orientation;
        // TODO: run issue checker
        // issuecheck()

        return true;
    }

    // todo make it return an update of wrong objects
    private boolean issuecheck(Update up) {
        loopcheck(up);
        return false;
    }

    // perform bfs for the loop
    private Node loopcheck(Update up) {
        return null;
    }

    private class Node {

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
        double MARGIN = 30.0;
        double CLUE_RADIUS = 12.0;

        double w_canvas = g.getCanvas().getWidth();
        double h_canvas = g.getCanvas().getHeight();

        double w_cell = (w_canvas - 2*MARGIN) / width;
        double h_cell = (h_canvas - 2*MARGIN) / height;

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
                if (lines[r][c] == -1) {
                    g.strokeLine(x, y, x + w_cell, y + h_cell);
                } else if (lines[r][c] == 1) {
                    g.strokeLine(x, y + h_cell, x + w_cell, y);
                }
            }
        }

        // Draw clues
        g.setFill(Color.WHITE);
        g.setStroke(Color.BLACK);
        for (int r = 0; r < height + 1; r++) {
            for (int c = 0; c < width + 1; c++) {
                if (clues[r][c] == -1) {
                    continue;
                }
                double c_x = MARGIN + w_cell * c;
                double c_y = MARGIN + h_cell * r;
                g.fillOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2*CLUE_RADIUS, 2*CLUE_RADIUS);
                g.strokeOval(c_x - CLUE_RADIUS, c_y - CLUE_RADIUS, 2*CLUE_RADIUS, 2*CLUE_RADIUS);
                g.strokeText(Integer.toString(clues[r][c]), c_x - CLUE_RADIUS + 8, c_y - CLUE_RADIUS + 16);
            }
        }
    }
}
