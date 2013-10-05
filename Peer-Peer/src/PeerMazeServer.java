import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Venkatesh JK
 * Date: 5/10/13
 * Time: 4:03 PM
 */
public class PeerMazeServer {


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

    /*Try getting a signaling mechanism for server/client interaction*/
    CountDownLatch startClientThreadSignal = new CountDownLatch(1);

    CountDownLatch gameStartedNotification = new CountDownLatch(1);

    CountDownLatch gameEndNotification = new CountDownLatch(1);

    PeerMazeServer() throws Exception {

        boolean primaryServerUp = false;

        boolean currentServerUp = false;

        try {
            mazeServerSocket = new ServerSocket(9000);
            currentServerUp = true;
        } catch (Exception e) {
            primaryServerUp = true;
            System.out.println("Primary server is already up at 9000");
        }

        /*Starting the client with signaling mechanism*/
        new MazeClientThread(startClientThreadSignal, this).start();

        /*Start a broadcasting socket to update game status for all the players*/
        new MulticastServerThread(gameStartedNotification, gameEndNotification).start();

        MulticastSocket socket = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("224.0.0.2");
        socket.joinGroup(group);
        /*Start a broadcasting socket to update game status for all the players*/
        new MulticastClientThread(socket, gameStartedNotification).start();

        if (!primaryServerUp && currentServerUp) {
           /*Accept the input parameters*/
            acceptInputParams();

           /*Build the Game Grid*/
            buildGameGrid();

        }

        /*Irrespective of the server status, the client player must atleast work, hence signaling to run now*/
        startClientThreadSignal.countDown();

        if (!primaryServerUp && currentServerUp) {
            /*Start listening to client requests*/
            acceptRequest();
        }


    }

    private void buildGameGrid() {
        List<Row> rows = new ArrayList<Row>();
        int columnNumber = gridSize;
        for (int i = 0; i < gridSize; i++) {
            rows.add(new Row(0, --columnNumber, gridSize));
        }
        game.setGrid(grid);
        grid.setRows(rows);
        game.setTreasureInfo(new TreasureInfo(treasureCount, treasureCount, new HashMap<Cell, Treasure>()));
        grid.fillRandomTreasures(treasureCount);
        System.out.println(grid);
        System.out.println("Starting cell : " + game.getGrid().getStartingCell());
    }


    private synchronized void acceptRequest() throws IOException {
        Socket clientSocket = null;
        while (gameStarted.status != StatusEnum.GAME_STARTED) {

            /*Accept the client connection*/
            clientSocket = mazeServerSocket.accept();

            clientSocket.setTcpNoDelay(true);
            clientSocket.setKeepAlive(true);

            executorService.execute(new PlayerHandlerThread(clientSocket, gameStarted, game, gameStartedNotification, gameEndNotification));

            if (gameStarted.status == StatusEnum.INACTIVE) {
                Timer timer = new Timer();
                timer.schedule(new GameTimerTask(gameStarted, game, gameStartedNotification), 20000);
                gameStarted.status = StatusEnum.NEW_GAME_REQUESTED;
            }

        }

    }


    public static void main(String[] args) throws Exception {

        new PeerMazeServer();

    }

    private void acceptInputParams() throws IOException {
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
        gridSize = Integer.parseInt(new Scanner(System.in).nextLine());
        System.out.print("Enter the desired number of treasures (M): "); //$NON-NLS-1$
        treasureCount = Integer.parseInt(new Scanner(System.in).nextLine());

        // initialise the maze
        System.out.println("The game is initialized with a maze size of " //$NON-NLS-1$
                + gridSize + "-by-" + gridSize + " with " + treasureCount //$NON-NLS-1$ //$NON-NLS-2$
                + " treasures"); //$NON-NLS-1$

    }

    public void setMazeServerSocket(ServerSocket mazeServerSocket) throws Exception{
        this.mazeServerSocket = mazeServerSocket;
        System.out.println("Starting the server at :" + mazeServerSocket.getLocalPort());
        executorService.execute(new BackupServerThread(mazeServerSocket.accept(), gameStarted, game, gameStartedNotification, gameEndNotification));
    }

}
