
import java.io.*;
import java.net.Socket;
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
    private PeerMazeServer mazeServer;

    public BackupServerThread(Socket clientSocket, GameStatus gameStarted, Game game, CountDownLatch gameStartNotification,
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

    // All this method does is wait for some bytes from the
    // connection, read them, then write them back again, until the
    // socket is closed from the other side.
    public synchronized void run() {

        int moveCounter=0;
        boolean socketClosed = false;
        while (true) {
            try {
                byte[] buf = new byte[4096];
                try {
                    System.out.println("Bckup server waiting for objects ...: ");

                    ObjectInputStream objectInputStream = new ObjectInputStream(sockInput);
                    mazeServer.setGame((Game) objectInputStream.readObject());
                    System.out.println("Players are : " + mazeServer.getGame().getPlayerList());
                    System.out.println("Grid received for move : " + moveCounter++ + " \nTreasures remaining : " +mazeServer.getGame().getTreasureInfo().getTreasureRemaining());

                } catch (Exception eofException) {
                    //Startup the backup ServerSocket as the Primary Server as the Primary has dropped off.
                    System.out.println("Primary went down... Starting the backup as the primary!");
                    socketClosed = true;
                    mazeServer.getGame().setBackupAvailable(false);
                    for(Player newPlayer : mazeServer.getGame().getPlayerList()){
                        if(newPlayer.getServerType().equalsIgnoreCase("Primary")){
                            newPlayer.setStatus(StatusEnum.INACTIVE);
                        }
                        if(newPlayer.getServerType().equalsIgnoreCase("Backup")){
                            newPlayer.setServerType("Primary");
                        }
                    }
                    sock.close();
                    //join the current player to this new primary server.
                    System.out.println("Connecting the client to my own backup server...");
                    CountDownLatch signalClientStart = new CountDownLatch(1);
                    new MazeClientThread(signalClientStart, mazeServer).start();
                    System.out.println("Started client connection to this backup....");
                    startPrimaryServer(signalClientStart);
                    System.out.println("Primary server started now....");
                    break;
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

    private void startPrimaryServer(CountDownLatch signalClientStart) throws Exception{
        mazeServer.setPrimaryServerSocket(signalClientStart);

    }

}