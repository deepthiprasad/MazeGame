import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game implements Serializable {
    private Grid grid;
    private TreasureInfo treasureInfo;
    private List<Player> playerList;
    private String backupAddress;
    private String backupPort;
    private AtomicBoolean isBackupAvailable= new AtomicBoolean(false);

    public TreasureInfo getTreasureInfo() {
        return treasureInfo;
    }

    public void setTreasureInfo(TreasureInfo treasureInfo) {
        this.treasureInfo = treasureInfo;
    }

    public List<Player> getPlayerList() {
        if(playerList == null){
            playerList = new ArrayList<Player>();
        }
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public void setBackupAddress(String backupAddress) {
        this.backupAddress = backupAddress;
    }

    public String getBackupAddress() {
        return backupAddress;
    }

    public void setBackupPort(String backupPort) {
        this.backupPort = backupPort;
    }

    public String getBackupPort() {
        return backupPort;
    }


    public boolean isBackupAvailable() {
        return isBackupAvailable.get();
    }

    public void setBackupAvailable(boolean backupAvailable) {
        isBackupAvailable.set(backupAvailable);
    }

    @Override
    public String toString() {
        return "Game{" +
                "grid=" + grid +
                ", treasureInfo=" + treasureInfo +
                ", playerList=" + playerList +
                ", backupAddress='" + backupAddress + '\'' +
                ", backupPort='" + backupPort + '\'' +
                ", isBackupAvailable=" + isBackupAvailable +
                '}';
    }
}
