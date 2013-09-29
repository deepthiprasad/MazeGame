package com.nus.maze.server;

import com.nus.maze.datatypes.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Dell
 * Date: 9/24/13
 * Time: 12:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class MazeServer {

    /*Flag to indicate GameStatus instance*/
    private GameStatus gameStarted = new GameStatus(StatusEnum.INACTIVE);

    /*ServerSocket instance initialization*/
    private ServerSocket mazeServerSocket = null;

    /*Integer to track server socket port*/
    private AtomicInteger socketPortNumber = new AtomicInteger(9000);

    /*Build the game*/
    private Game game = new Game();

    /*ExecutorService for dispatching player threads individually*/
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /*The size of the Grid*/
    public static int gridSize;

    /*The treasure count*/
    public static int treasureCount;

    /*Build the grid*/
    private Grid grid = new Grid();

    MazeServer(){

        /*Add all the rows*/
        List<Row> rows = new ArrayList<Row>();
        int columnNumber = gridSize;
        for(int i=0;i<gridSize;i++){
            rows.add(new Row(0,--columnNumber, gridSize));
        }
        game.setGrid(grid);
        grid.setRows(rows);

        grid.fillRandomTreasures(treasureCount);
        System.out.println(grid);
        System.out.println("Starting cell : " + game.getGrid().getStartingCell());

    }

    void init(){
        try {
            mazeServerSocket = new ServerSocket(socketPortNumber.incrementAndGet());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Waiting for players to connect...");
    }

    private static final AtomicInteger PLAYER_COUNT = new AtomicInteger(0);

    private void acceptRequest() throws IOException {
        Socket clientSocket = null;
        mazeServerSocket = new ServerSocket(9000);
        while(gameStarted.status != StatusEnum.GAME_STARTED){

            /*Accept the client connection*/
            clientSocket =  mazeServerSocket.accept();
            if(gameStarted.status == StatusEnum.GAME_STARTED){
                //reject the connection from another player as the game has already begun.
                String alreadyBegun = "The game has already begun... plz wait for the next game";
                clientSocket.getOutputStream().write(alreadyBegun.getBytes(), 0, alreadyBegun.getBytes().length);
                clientSocket.getOutputStream().flush();
                break;
            }

            clientSocket.setTcpNoDelay(true);
            /*Write the welcome message to the player*/
            int playerId = writeWelcomeMessage(clientSocket);

            /*Build a player instance and instantiate the thread*/
            Player player = new Player();
            player.setId(playerId);
            player.setCurrentPosition(game.getGrid().getRandomUnOccupiedCell());
            //add a player to the game here
            game.getPlayerList().add(player);
            executorService.execute(new PlayerHandlerThread(clientSocket, player, gameStarted, game));

            if(gameStarted.status == StatusEnum.INACTIVE){
                Timer timer = new Timer();
                timer.schedule(new GameTimerTask(gameStarted, game),10000);
                gameStarted.status = StatusEnum.NEW_GAME_REQUESTED;
            }

            //init();
        }

    }

    private int writeWelcomeMessage(Socket clientSocket) throws IOException {
        int playerId = PLAYER_COUNT.incrementAndGet();
        String welcomeMesssage = "Welcome Player " + playerId;

        clientSocket.getOutputStream().write( welcomeMesssage.getBytes(), 0, welcomeMesssage.getBytes().length);
        clientSocket.getOutputStream().flush();
        return playerId;
    }

    public static void main(String[] args) throws IOException {


        /* If the game has not yet started, accept the requests */
        /* Start a timer for 20s to enable the joining window */
        /* If game has started, stop accepting requests */

        //starting the game with NxN grid with M treasures
        gridSize = Integer.parseInt(args[0]);
        treasureCount = Integer.parseInt(args[1]);


        new MazeServer().acceptRequest();

    }



}
