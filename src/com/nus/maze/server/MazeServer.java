package com.nus.maze.server;

import com.nus.maze.datatypes.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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

    /*The size of the Grid*/
    private static int gridSize;

    /*The treasure count*/
    private static int treasureCount;
    
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

    /*Build the grid*/
    private Grid grid = new Grid();

    private static final AtomicInteger PLAYER_COUNT = new AtomicInteger(0);
    
    public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));

		// introduction display
		System.out
				.println("=======================================Maze Server======================================="); //$NON-NLS-1$
		System.out
				.println("The maze will have a size of N-by-N, with M number of treasures."); //$NON-NLS-1$

		// getting initial input for M (i.e. number of treasures) and N (i.e.
		// grid size)
		System.out.print("Enter the desired grid size (N): "); //$NON-NLS-1$
		gridSize = Integer.parseInt(reader.readLine());
		System.out.print("Enter the desired number of treasures (M): "); //$NON-NLS-1$
		treasureCount = Integer.parseInt(reader.readLine());

		// initialise the maze
		MazeServer mazeServer = new MazeServer();
		System.out.println("The game is initialized with a maze size of " //$NON-NLS-1$
				+ gridSize + "-by-" + gridSize + " with " + treasureCount //$NON-NLS-1$ //$NON-NLS-2$
				+ " treasures"); //$NON-NLS-1$

		System.out.println("\nWaiting for players to connect..."); //$NON-NLS-1$
		/*
		 * Start the server and start accept requests /* If the game has not yet
		 * started, accept the requests
		 */
		/* Start a timer for 20s to enable the joining window */
		/* If game has started, stop accepting requests */

		// start the server
		mazeServer.acceptRequest();
    }
    
    MazeServer(){

        /*Add all the rows*/
        List<Row> rows = new ArrayList<Row>();
        int columnNumber = gridSize;
        for(int i=0;i<gridSize;i++){
            rows.add(new Row(0,--columnNumber, gridSize));
        }
        game.setGrid(grid);
        grid.setRows(rows);
        game.setTreasureInfo(new TreasureInfo(treasureCount, treasureCount, new HashMap<Cell, Treasure>()));
        grid.fillRandomTreasures(treasureCount);
//        System.out.println(grid);
//        System.out.println("Starting cell : " + game.getGrid().getStartingCell());

    }

    void init(){
        try {
            mazeServerSocket = new ServerSocket(socketPortNumber.incrementAndGet());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Waiting for players to connect...");
    }

    private void acceptRequest() throws IOException {
        Socket clientSocket = null;
        mazeServerSocket = new ServerSocket(9000);
        while(gameStarted.status != StatusEnum.GAME_STARTED){

            /*Accept the client connection*/
            clientSocket =  mazeServerSocket.accept();
            if(gameStarted.status == StatusEnum.GAME_STARTED){
            	
                //reject the connection from another player as the game has already begun.
                String alreadyBegun = "The game has already begun... please wait for the next game";
                clientSocket.getOutputStream().write(alreadyBegun.getBytes(), 0, alreadyBegun.getBytes().length);
                clientSocket.getOutputStream().flush();
                break;
            }

            clientSocket.setTcpNoDelay(true);
            /*Write the welcome message to the player*/
            int playerId = PLAYER_COUNT.incrementAndGet();

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
                System.out.println("\nGame starting in 20s..."); //$NON-NLS-1$
            }
            //init();
        }

    }
}
