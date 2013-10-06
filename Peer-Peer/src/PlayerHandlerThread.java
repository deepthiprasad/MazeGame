
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerHandlerThread extends Thread {
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private Player player;
    private GameStatus gameStatus;
    private Grid grid;
    private Game game;
    String backupAddress;
    String backupPort;
    private static final AtomicInteger PLAYER_COUNT = new AtomicInteger(0);
    private CountDownLatch gameStartNotification;
    private CountDownLatch gameEndNotification;
    private PeerMazeServer mazeServer;

    public PlayerHandlerThread(Socket clientSocket, GameStatus gameStarted, Game game, CountDownLatch gameStartNotification,
                               CountDownLatch gameEndNotification, PeerMazeServer mazeServer) throws IOException {
        sock = clientSocket;
        sockInput = clientSocket.getInputStream();
        sockOutput = new BufferedOutputStream(clientSocket.getOutputStream());
        this.player = new Player();
        this.grid = game.getGrid();
        this.gameStatus = gameStarted;
        this.game = game;
        this.gameStartNotification = gameStartNotification;
        this.gameEndNotification = gameEndNotification;
        this.mazeServer = mazeServer;

    }

    private void writeWelcomeMessage(int id) throws IOException {
        String welcomeMesssage = "Welcome Player " + id;
        welcomeMesssage += "\n Backup=" + game.getBackupAddress() + ":" + game.getBackupPort();
        sock.getOutputStream().write(welcomeMesssage.getBytes(), 0, welcomeMesssage.getBytes().length);
        sock.getOutputStream().flush();
    }

    // All this method does is wait for some bytes from the
    // connection, read them, then write them back again, until the
    // socket is closed from the other side.
    public synchronized void run() {
        boolean alreadyNotifiedWaitStatus = false;
        boolean alreadyNotifiedStart = false;
        boolean alreadySentBackupInfo = false;
        while (true) {
            try {
                byte[] buf = new byte[4096];
                try {
                    if(getName().contains("Player 1")){
                        player.setServerType("Primary");
                    }
                    if (gameStatus.status == StatusEnum.GAME_STARTED && !alreadyNotifiedStart) {
                        //reject the connection from another player as the game has already begun.
                        String alreadyBegun = "The game has already begun... plz wait for the next game";
                        sockOutput.write(alreadyBegun.getBytes(), 0, alreadyBegun.getBytes().length);
                        sockOutput.flush();
                        break;
                    }

                    if (gameStatus.status != StatusEnum.GAME_STARTED) {
                        player.setId(PLAYER_COUNT.incrementAndGet());
                        player.setCurrentPosition(game.getGrid().getRandomUnOccupiedCell());
                        player.setNumOfTreasuresFound(ServerHelper.getTreasureValue(player.getCurrentPosition()));
                        player.setStatus(StatusEnum.ACTIVE);
                        if(game.getPlayerList().size() ==1){

                        }
                        //add a player to the game here
                        game.getPlayerList().add(player);
                        System.out.println("Player : P" + player.getId() + " joined...");

                        this.setName("Player " + player.getId());

                        if(getName().contains("Player 1")){
                            player.setServerType("Primary");
                        }

                        /*Write welcome message for this player who just joined*/
                        writeWelcomeMessage(player.getId());

                        /*Check whether the player is already notified and send a welcome message*/
                        checkAndNotifyAboutGame(alreadyNotifiedWaitStatus);

                        /*Build a player instance and instantiate the thread*/
                        if (!game.isBackupAvailable() && game.getPlayerList().size() > 1) {
                            // Read the Grid request from the client
                            sock.getInputStream().read(buf, 0, buf.length);
                            String temp = new String(buf).trim();
                            if (temp.contains("GridRequest")) {
                                String backupServer = temp.split(":")[1];
                                game.setBackupAddress(backupServer.split("=")[0]);
                                game.setBackupPort(backupServer.split("=")[1]);

                                //The Player who connects first to the primary shall be declared as Backup.
                                player.setServerType("Backup");
                                //if valid port is found, try connecting to it
                                checkForValidBackup();
                            }
                            game.setBackupAvailable(true);

                            // Player 2 server thread shall Respond with grid object
                           if (getName().contains("Player 2")) {
                               player.setServerType("Backup");
                               System.out.println("Grid Request sent is..." + temp);
                                // Send the Grid object to the client
                                ObjectOutputStream gridTransferStream = new ObjectOutputStream(sock.getOutputStream());
                                gridTransferStream.writeObject(game);
                                gridTransferStream.flush();
                                //game.setBackupAvailable(true);
                            }
                        }
                        alreadyNotifiedWaitStatus = true;
                        alreadyNotifiedStart = true;
                    }


                    /*Wait till you receive a signal from client thread that the game has started*/
                    gameStartNotification.await();
                    buf = new byte[4096];
                    sockInput.read(buf, 0, buf.length);
                    String command = new String(buf).trim();
                    if (checkForValidMoves(command))  {
                        continue;
                    }

                    if (command.equalsIgnoreCase("N")) {
                        //here while constructing the grid, X is treated as Y and vice-versa.
                        if (player.getCurrentPosition().getY() < grid.getRows().size() - 1) {
                            //ignore the rest of the requests as they are not valid moves.
                            //Since the command is NORTH, increment Y by 1 and get the corresponding cell.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX(), player.getCurrentPosition().getY() + 1);
                            moveAndCaptureTreasure(cell);
                        }
                    } else if (command.equalsIgnoreCase("S")) {
                        if (player.getCurrentPosition().getY() > 0) {
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX(), player.getCurrentPosition().getY() - 1);
                            moveAndCaptureTreasure(cell);
                        }
                    } else if (command.equalsIgnoreCase("E")) {
                        if (player.getCurrentPosition().getX() < grid.getRows().size() - 1) {
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX() + 1, player.getCurrentPosition().getY());
                            moveAndCaptureTreasure(cell);
                        }
                    } else if (command.equalsIgnoreCase("W")) {
                        if (player.getCurrentPosition().getX() > 0) {
                            //ignore the rest of the requests as they are not valid moves.
                            Cell cell = grid.getCellAtXY(player.getCurrentPosition().getX() - 1, player.getCurrentPosition().getY());
                            moveAndCaptureTreasure(cell);
                        }
                    } else if (command.equalsIgnoreCase("NOMOVE")) {
                        //just send the grid status.
                    }
                    System.out.println("Current Primary/Backup : " + player);



                    StringBuffer buffer = new StringBuffer();
                    buffer.append("--------------------------\n");
                    buffer.append("| Player | Treasure Count|\n");
                    buffer.append("--------------------------\n");
                    for (Player player1 : game.getPlayerList()) {
                        buffer.append("| P" + player1.getId() + "     |      " + player1.getNumOfTreasuresFound() + "     |" + "\n");
                    }
                    buffer.append("--------------------------\n");
                    String gridString = buffer.append("\n").toString() + grid.toString();
                    sockOutput.write(gridString.getBytes(), 0, gridString.getBytes().length);
                } catch (SocketException socketException) {
                    System.out.println("Player : " + player + " dropped off...");
                    player.setStatus(StatusEnum.INACTIVE);
                    //TODO : Set the status of the player to inactive
                    break;
                } finally {
                    //send the current grid standings to the backup server for a new copy.
                    if(game.isBackupAvailable())
                        sendACopyOfGame();
                }
                sockOutput.flush();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                break;
            }
        }

        try {
            System.err.println("PlayerHandlerThread:Closing socket.");
            sock.close();
        } catch (Exception e) {
            System.err.println("PlayerHandlerThread: Exception while closing socket, e=" + e);
            e.printStackTrace(System.err);
        }

    }

    private void checkForValidBackup() throws IOException {
        if(game.getBackupPort()!=null && game.getBackupAddress()!=null){
            game.setBackupAvailable(true);
            mazeServer.setBackupSocket(new Socket(game.getBackupAddress(), Integer.parseInt(game.getBackupPort())));
        }
    }

    private void sendACopyOfGame() throws Exception {
        // Send the Grid object to the client
        ObjectOutputStream gridTransferStream = new ObjectOutputStream(mazeServer.getBackupSocket().getOutputStream());
        gridTransferStream.writeObject(game);
        gridTransferStream.flush();
    }

    private boolean checkForValidMoves(String command) throws IOException {
        if (!command.equalsIgnoreCase("N")
                && !command.equalsIgnoreCase("S")
                && !command.equalsIgnoreCase("E")
                && !command.equalsIgnoreCase("W")
                && !command.equalsIgnoreCase("NOMOVE")) {
            String error = "Invalid Command, Enter [N, S, E, W, NOMOVE]";
            sockOutput.write(error.getBytes(), 0, error.getBytes().length);
            sockOutput.flush();
            return true;
        }
        return false;
    }

    private void checkAndNotifyAboutGame(boolean alreadyNotifiedWaitStatus) throws IOException {
        if (gameStatus.status != StatusEnum.GAME_STARTED) {
            if (!alreadyNotifiedWaitStatus) {
                //TODO: ignore the player inputs until the start of the game.
                String gridString = "\nWaiting for the other players to join...";
                sockOutput.write(gridString.getBytes(), 0, gridString.getBytes().length);
                sockOutput.flush();
            }
        }
    }

    private void broadcastStartMessage(boolean alreadyNotifiedStart) throws IOException {
        if (gameStatus.status == StatusEnum.GAME_STARTED && !alreadyNotifiedStart) {

            String gridString = "Game has begun, start collecting your treasures as much as you can!...\n";
            gridString += game.getGrid().toString();
            sockOutput.write(gridString.getBytes(), 0, gridString.getBytes().length);
            sockOutput.flush();
            alreadyNotifiedStart = true;
        }
    }

    private void moveAndCaptureTreasure(Cell cell) {
        if (!cell.getData().contains("P")) {
            if (cell.getData().contains("T")) {
                int treasureRemaining = game.getTreasureInfo().getTreasureRemaining();
                int currentCellTreasureValue = ServerHelper.getTreasureValue(cell);
                player.setNumOfTreasuresFound(player.getNumOfTreasuresFound() + ServerHelper.getTreasureValue(cell));
                game.getTreasureInfo().setTreasureRemaining(treasureRemaining - currentCellTreasureValue);
                if (game.getTreasureInfo().getTreasureRemaining() == 0) {
                    gameEndNotification.countDown();
                }
            }
            player.getCurrentPosition().setData(" - ");
            cell.setData("P" + player.getId());
            player.setCurrentPosition(cell);
        }

    }
}