package com.nus.maze.server;

import com.nus.maze.datatypes.Cell;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/26/13
 * Time: 1:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServerHelper {
    public static void applyTreasureValue(Cell currentCell){
        int tIndex = currentCell.getData().indexOf("T");
        if(tIndex==0){
            currentCell.setData("2T");
        }else{
            int currentTreasure = Integer.parseInt(currentCell.getData().substring(0,tIndex)) + 1;
            currentCell.setData( currentTreasure +"T");
        }
    }

    public static int getTreasureValue(Cell currentCell){
        int treasureValue = 00;
        int tIndex = currentCell.getData().trim().indexOf("T");
        if(tIndex==0){
            treasureValue = 1;
        }else{
            treasureValue = Integer.parseInt(currentCell.getData().trim().substring(0,tIndex)) ;
        }
        return treasureValue;
    }
}
