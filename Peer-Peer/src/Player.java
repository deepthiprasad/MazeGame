import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class Player implements Serializable {
      private int id;
      private Cell currentPosition = new Cell(0,0,"");
      private int numOfTreasuresFound;
      private StatusEnum status;

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    private String serverType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cell getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Cell currentPosition) {
        this.currentPosition = currentPosition;
        this.currentPosition.setData("(P" + id +")");
    }

    public int getNumOfTreasuresFound() {
        return numOfTreasuresFound;
    }

    public void setNumOfTreasuresFound(int numOfTreasuresFound) {
        this.numOfTreasuresFound = numOfTreasuresFound;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", currentPosition=" + currentPosition +
                ", numOfTreasuresFound=" + numOfTreasuresFound +
                ", status=" + status +
                ", serverType='" + serverType + '\'' +
                '}';
    }
}
