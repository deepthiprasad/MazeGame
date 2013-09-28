package com.nus.maze.datatypes;

import com.nus.maze.server.ServerHelper;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Grid {
    private List<Row> rows;
    private int cellCount = 0;
    private int gridSize;
    private Cell[][] cellArray;
    Map<String, Cell> cellOrientationMap = new HashMap<String, Cell>();
    public List<Row> getRows() {
        return rows;
    }

    public Cell getCell(Cell currentCell, DirectionEnum directionEnum){
        Cell cell = null;
        switch (directionEnum){
            case N: cell = getAllCells().get(currentCell.getY()+rows.size());
                break;
            case S: cell = getAllCells().get(currentCell.getY()-rows.size());
                break;
            case E: cell = getAllCells().get(currentCell.getX()+rows.size());
                break;
            case W: cell = getAllCells().get(currentCell.getX()-rows.size());
                break;
        }
        return cell;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
        cellCount = rows.size() * rows.size();
        gridSize = rows.size() * 2;
    }

    public void fillRandomTreasures(int treasureCount) {
        for(Cell cell : getAllCells()){
            cellOrientationMap.put(cell.getX()+","+cell.getY(), cell);
        }
        //get the list of all the cells
        List<Cell> allCells = getAllCells();
        for (int i = 0; i < treasureCount; i++) {
            boolean treasureDeposited = false;
            Random random = new Random();

            //until the treasure gets deposited in a vacant position, keep finding.
            while (!treasureDeposited) {
            /*Get a random column now and put the treasure there*/
                int cellNumber = random.nextInt(cellCount);
                Cell currentCell = allCells.get(cellNumber);
                if (currentCell.getData().equals("-")) {
                    //continue until u find a vacant space.
                    currentCell.setData("T");
                    treasureDeposited = true;
                }
                else if (currentCell.getData().contains("T")) {
                    //continue until u find a vacant space.
                    ServerHelper.applyTreasureValue(currentCell);

                    treasureDeposited = true;
                }
            }
        }
    }

    private List<Cell> getAllCells() {
        List<Cell> cells = new ArrayList<Cell>();
        for (Row row : rows) {
            cells.addAll(row.getCells());
        }
        return cells;
    }

    public Cell getStartingCell(){
        return getAllCells().get(0);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < rows.size(); i++) {
            buffer.append("| " + rows.get(i) + "\n");
        }
        return buffer.toString();
    }

    public Cell getCellAtXY(int x, int y) {
        return cellOrientationMap.get(x+","+y);
    }
}
