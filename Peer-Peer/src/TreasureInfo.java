import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreasureInfo implements Serializable {
    private int treasureCount;
    private int treasureRemaining;
    private Map<Cell,Treasure> mapOfTreasures = new HashMap<Cell, Treasure>();

    public TreasureInfo(int treasureCount, int treasureRemaining, Map<Cell, Treasure> mapOfTreasures) {
        this.treasureCount = treasureCount;
        this.treasureRemaining = treasureRemaining;
        this.mapOfTreasures = mapOfTreasures;
    }

    public int getTreasureCount() {
        return treasureCount;
    }

    public void setTreasureCount(int treasureCount) {
        this.treasureCount = treasureCount;
    }

    public int getTreasureRemaining() {
        return treasureRemaining;
    }

    public void setTreasureRemaining(int treasureRemaining) {
        this.treasureRemaining = treasureRemaining;
    }

    public Map<Cell, Treasure> getMapOfTreasures() {
        return mapOfTreasures;
    }

    public void setMapOfTreasures(Map<Cell, Treasure> mapOfTreasures) {
        this.mapOfTreasures = mapOfTreasures;
    }

    @Override
    public String toString() {
        return "TreasureInfo{" +
                "treasureCount=" + treasureCount +
                ", treasureRemaining=" + treasureRemaining +
                ", mapOfTreasures=" + mapOfTreasures +
                '}';
    }
}
