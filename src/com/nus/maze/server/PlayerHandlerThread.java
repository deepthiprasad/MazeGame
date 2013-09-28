package com.nus.maze.server;

import com.nus.maze.datatypes.*;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.SocketException;

public class PlayerHandlerThread implements Runnable {
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private Player player;
    private final GameStatus gameStatus;
    private Grid grid;
    private Game game;

    public PlayerHandlerThread(Socket sock, Player player, GameStatus gameStatus, Game game) throws IOException {
        this.sock = sock;
        sockInput = sock.getInputStream();
        sockOutput = sock.getOutputStream();
        this.player = player;
        this.gameStatus = gameStatus;
        this.grid = game.getGrid();
        this.game = game;
    }

    // All this method does is wait for some bytes from the
    // connection, read them, then write them back again, until the
    // socket is closed from the other side.
    public void run() {
        System.out.println("PlayerHandlerThread: Handler run() starting.");
        while(true) {
            try {
                byte[] buf=new byte[1024];
                try {
                    sockInput.read(buf, 0, buf.length);
                    if(gameStatus.status != StatusEnum.GAME_STARTED){
                        //TODO: ignore the player inputs until the start of the game.
                        continue;
                    }
                    String command = new String(buf).trim();
                    if(!command.equalsIgnoreCase("N")
                            && !command.equalsIgnoreCase("S")
                            && !command.equalsIgnoreCase("E")
                            && !command.equalsIgnoreCase("W")
                            && !command.equalsIgnoreCase("NOMOVE")){
                        String error = "Invalid Command, Enter [N, S, E, W, NOMOVE]";
                        sockOutput.write(error.getBytes(), 0, error.getBytes().length);
                        continue;
                    }
                    if(command.equalsIgnoreCase("N")){
                        //here while constructing the grid, X is treated as Y and vice-versa.
                        if(player.getCurrentPosition().getY() <  grid.getRows().size()-1){
                            //ignore the rest of the requests as they are not valid moves.
                            //Since the command is NORTH, increment Y by 1 and get the corresponding cell.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX(), player.getCurrentPosition().getY() + 1);
                            moveAndCaptureTreasure(cell);
                        }
                    }
                    else if(command.equalsIgnoreCase("S")){
                        if(player.getCurrentPosition().getY() >  0){
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX(), player.getCurrentPosition().getY() - 1);
                            moveAndCaptureTreasure(cell);
                        }
                    }
                    else if(command.equalsIgnoreCase("E")){
                        if(player.getCurrentPosition().getX() <  grid.getRows().size()-1){
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX() + 1, player.getCurrentPosition().getY());
                            moveAndCaptureTreasure(cell);
                        }
                    }
                    else if(command.equalsIgnoreCase("W")){
                        if(player.getCurrentPosition().getX() > 0){
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX() - 1, player.getCurrentPosition().getY());
                            moveAndCaptureTreasure(cell);
                        }
                    }
                    else if(command.equalsIgnoreCase("NOMOVE")){
                        //just send the grid status.
                    }

                    String gridString = grid.toString();
                    sockOutput.write(gridString.getBytes(), 0, gridString.getBytes().length);
                } catch (SocketException socketException){
                    System.out.println("Player : " + player + " dropped off...");
                    //TODO : Set the status of the player to inactive
                    break;
                }
                finally {

                }
                sockOutput.flush();
            }
            catch (Exception e){
                e.printStackTrace(System.err);
                break;
            }
        }

        try {
            System.err.println("PlayerHandlerThread:Closing socket.");
            sock.close();
        }
        catch (Exception e){
            System.err.println("PlayerHandlerThread: Exception while closing socket, e="+e);
            e.printStackTrace(System.err);
        }

    }

    private void moveAndCaptureTreasure(Cell cell) {
        if(!cell.getData().contains("P")){
            if( (cell.getData().contains("T"))){
                player.setNumOfTreasuresFound(player.getNumOfTreasuresFound()+ ServerHelper.getTreasureValue(cell));
            }
            player.getCurrentPosition().setData("-");
            cell.setData("P" + player.getId());
            player.setCurrentPosition(cell);
        }
    }
}