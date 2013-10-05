
import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Grid implements Serializable {
    Map<String, Cell> cellOrientationMap = new HashMap<String, Cell>();
    private List<Row> rows;
    private int cellCount = 0;
    private int gridSize;
    private Cell[][] cellArray;

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
        cellCount = rows.size() * rows.size();
        gridSize = rows.size() * 2;
    }

    public Cell getCell(Cell currentCell, DirectionEnum directionEnum) {
        Cell cell = null;
        switch (directionEnum) {
            case N:
                cell = getAllCells().get(currentCell.getY() + rows.size());
                break;
            case S:
                cell = getAllCells().get(currentCell.getY() - rows.size());
                break;
            case E:
                cell = getAllCells().get(currentCell.getX() + rows.size());
                break;
            case W:
                cell = getAllCells().get(currentCell.getX() - rows.size());
                break;
        }
        return cell;
    }

    public void fillRandomTreasures(int treasureCount) {
        for (Cell cell : getAllCells()) {
            cellOrientationMap.put(cell.getX() + "," + cell.getY(), cell);
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
                if (currentCell.getData().contains("-")) {
                    //continue until u find a vacant space.
                    currentCell.setData("T");
                    treasureDeposited = true;
                } else if (currentCell.getData().contains("T")) {
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

    public Cell getStartingCell() {
        return getAllCells().get(0);
    }

    public Cell getRandomUnOccupiedCell() {
        //get the list of all the cells
        List<Cell> allCells = getAllCells();
        Random random = new Random();
        int cellNumber = random.nextInt(cellCount);
        Cell currentCell = allCells.get(cellNumber);
        if(noPlayerFound(currentCell)){
            return currentCell;
        }
        boolean vacancyFound = false;
        while(!vacancyFound){
           currentCell = allCells.get(random.nextInt(cellCount));
            if(noPlayerFound(currentCell)){
                vacancyFound = true;
            }
        }
        return currentCell;
    }

    private boolean noPlayerFound(Cell currentCell) {
        return currentCell.getData().contains("-") || currentCell.getData().contains("T");
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        //calculate the width of all the cells and increase the space accordingly.
        List<Cell> cellList = getAllCells();
        Collections.sort(cellList, new Comparator<Cell>() {
            @Override
            public int compare(Cell o1, Cell o2) {
                return o1.getData().length() < o2.getData().length() ? 1:-1;
            }
        });
        for(Cell cell : cellList){
            int biggetStringLength = cellList.get(0).getData().length();
            int currentCellLength  = cell.getData().length();
            int difference = biggetStringLength - currentCellLength;
            if(difference!=0 && difference % 2 == 0){
                //fill the spaces if the difference is even.
                String present = cell.getData();
                int index = 0;
                String leftSpaces = "";
                String rightSpaces = "";
                while(index < difference/2){
                    leftSpaces += " ";
                    index++;
                }
                leftSpaces += present;

                while(index <= difference){
                    rightSpaces += " ";
                    index++;
                }
                present += rightSpaces;
                cell.setData(present);
            } else if(difference % 2 != 0){
                String present = cell.getData();
                int index = 0;
                String leftSpaces = "";
                while(index < difference){
                    leftSpaces += " ";
                    index++;
                }
                leftSpaces += present;
                cell.setData(leftSpaces);
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            buffer.append("|" + rows.get(i) + "\n");
        }
        return buffer.toString();
    }

    public Cell getCellAtXY(int x, int y) {
        return cellOrientationMap.get(x + "," + y);
    }
}
