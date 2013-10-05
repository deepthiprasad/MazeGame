
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BackupServerThread extends Thread {
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
    private Socket backupSocket;

    public BackupServerThread(Socket clientSocket, GameStatus gameStarted, Game game, CountDownLatch gameStartNotification,
                              CountDownLatch gameEndNotification) throws IOException {
        sock = clientSocket;
        sockInput = clientSocket.getInputStream();
        sockOutput = new BufferedOutputStream(clientSocket.getOutputStream());
        this.player = new Player();
        this.grid = game.getGrid();
        this.gameStatus = gameStarted;
        this.game = game;
        this.gameStartNotification = gameStartNotification;
        this.gameEndNotification = gameEndNotification;

    }

    // All this method does is wait for some bytes from the
    // connection, read them, then write them back again, until the
    // socket is closed from the other side.
    public synchronized void run() {

        while (true) {
            try {
                byte[] buf = new byte[4096];
                try {

                      ObjectInputStream objectInputStream = new ObjectInputStream(sockInput);
                      Game game = (Game) objectInputStream.readObject();
                    System.out.println("Grid received : " + game.getGrid());

                } catch (SocketException socketException) {
                    System.out.println("Player : " + player + " dropped off...");
                    //TODO : Set the status of the player to inactive
                    break;
                } finally {

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
            backupSocket = new Socket(game.getBackupAddress(), Integer.parseInt(game.getBackupPort()));
        }
    }

    private void sendACopyOfGame() throws Exception {
        // Send the Grid object to the client
        ObjectOutputStream gridTransferStream = new ObjectOutputStream(backupSocket.getOutputStream());
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