package com.nus.maze.datatypes;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cell {
    int x,y;
    String data;

    public Cell(int x, int y, String data) {
        this.x = x;
        this.y = y;
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(data);
        return buffer.toString();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
