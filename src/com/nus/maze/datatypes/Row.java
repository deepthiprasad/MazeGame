package com.nus.maze.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class Row {

    private List<Cell> cells;

    private Cell[][] cellArray;
    public Row(int rowNumber, int columnNumber, int numberOfColumns){
        cells = new ArrayList<Cell>();
        numberOfColumns --;
        while(rowNumber <=numberOfColumns ){
            cells.add(new Cell(rowNumber++, columnNumber, "-"));
        }
    }

    public Row(int rowNumber, int columnNumber, int numberOfColumns, boolean array){
        if(cellArray == null)
            cellArray = new Cell[numberOfColumns][numberOfColumns];
        numberOfColumns --;
        while(rowNumber <=numberOfColumns ){
            cellArray[rowNumber][columnNumber] = new Cell(rowNumber++, columnNumber, "-");
        }
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }


    public Cell[][] getCellArray() {
        return cellArray;
    }

    public void setCellArray(Cell[][] cellArray) {
        this.cellArray = cellArray;
    }


    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for(int i=0;i<cells.size();i++){
            buffer.append(cells.get(i));
            if(cells.get(i).getData().length() <10){
                buffer.append(" |  ");
            }else{
                buffer.append("  |  ");
            }
        }
        return buffer.toString();
    }
}
