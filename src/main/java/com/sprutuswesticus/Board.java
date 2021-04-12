package com.sprutuswesticus;

import java.util.Arrays;
import java.util.Scanner;

import javafx.scene.layout.HBox;

public class Board {
    static final int DEFAULT_HEIGHT = 10;
    static final int DEFAULT_WIDTH = 10;
    private int height, width;
    int[][] lines;
    int[][] clues;

    public Board() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    //empty intialization
    public Board(int width, int height) {
        this.height = height;
        this.width = width;
        this.lines =  new int[height+1][width+1];
        this.clues = new int[height][width];
    }
    public Board(String spec){
        this.specific(spec);
    }

    public boolean specific(String spec) {
        spec = spec.trim();
        int colon = spec.indexOf(':');
        int x = spec.indexOf('x');
        this.width = Integer.parseInt(spec.substring(0, x));
        this.height = Integer.parseInt(spec.substring(x+1, colon));
        this.lines =  new int[this.height][this.width];
        this.clues = new int[this.height+1][this.width+1];
        for (int[] row: clues){
            Arrays.fill(row, -1);
        }

        int row = 0;
        int col = 0;
        for(int cur = colon + 1; cur < spec.length(); cur++){
            char c = spec.charAt(cur);
            if(c >= '0' && c <= '4'){
                this.clues[row][col] = c-'0';
                System.out.println(c-'0');
                col++;
            }else if(c >= 'a' && c <= 'z'){
                col += c - 96;
            }else{
                return false;
            }
            if(col > width){
                row++;
                col = col%(width+1);
            }
        }
        if(!(row == height && col == width)) {
            return false;
        }
        return true;
    }

    public boolean alter(Update up){
        if(up.x >= this.height || up.y >= this.width){
            return false;
        }
        this.lines[up.x][up.y] = up.orientation;
        //TODO: run issue checker
        //issuecheck()

        return true;
    }

    //todo make it return an update of wrong objects
    private boolean issuecheck(Update up) {
        loopcheck(up);
        return false;
    }
    //perform bfs for the loop
    private Node loopcheck(Update up) {
        
    }

    private class Node{
        
    }

    public String stringifylines() {
        String out = "";
        for(int[] row: this.lines) {
            out += Arrays.toString(row);
            out += "\n";
        }
        return out;
    }
    public String stringifygrid() {
        String out = "";
        for(int[] row: this.clues) {
            out += Arrays.toString(row);
            out += "\n";
        }
        return out;
    }
}
