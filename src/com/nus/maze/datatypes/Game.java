package com.nus.maze.datatypes;


import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/23/13
 * Time: 11:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game {
    private Grid grid;
    private TreasureInfo treasureInfo;
    private List<Player> playerList;

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

    @Override
    public String toString() {
        return "Game{" +
                "grid=" + grid +
                ", treasureInfo=" + treasureInfo +
                ", playerList=" + playerList +
                '}';
    }
}
