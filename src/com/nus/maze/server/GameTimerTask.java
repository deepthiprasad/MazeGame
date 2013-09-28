package com.nus.maze.server;

import com.nus.maze.datatypes.Game;
import com.nus.maze.datatypes.Player;
import com.nus.maze.datatypes.StatusEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/24/13
 * Time: 12:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameTimerTask extends TimerTask {
    private GameStatus gameStarted;

    private Game game;

    public GameTimerTask(GameStatus gameStarted, Game game) {
        this.gameStarted = gameStarted;
        this.game = game;
    }

    @Override
    public void run() {
        gameStarted.status = StatusEnum.GAME_STARTED;
        System.out.println("Starting the game now...");
    }
}
