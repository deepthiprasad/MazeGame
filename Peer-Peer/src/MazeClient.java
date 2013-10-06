
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class MazeClient {
    private static Socket sock = null;
    private String serverHostname = null;
    private int serverPort = 0;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private final LinkedList<Integer> serverPortPool = new LinkedList<Integer>();
    private String backupPort;
    private String backupIP;
    private AtomicBoolean localClientExists;
    private Game game;
    private final PeerMazeServer server;

    public static void main(String argv[]) throws Exception {
        String hostname = "localhost";
        // TODO: make the port dynamic for multiple clients
        int port = 9000;

        //MazeClient client = new MazeClient(hostname, port);

        //client.sendSomeMessages();

        // run the heartbeat monitor every 1 second and notify if couldn't
        // connect.

    }

    /**
     * @param serverHostname
     * @param serverPort
     * @param server
     */
    public MazeClient(String serverHostname, int serverPort, PeerMazeServer server) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.server = server;
    }

    public void sendSomeMessages() throws Exception{
        System.err.println("Opening connection to " + serverHostname + " port " //$NON-NLS-1$
                + this.serverPort);
        boolean alreadyJoined = false;

        while (true) {
            byte[] buf = new byte[4096];

            String command = ""; //$NON-NLS-1$
            if (!alreadyJoined) {
                command = new Scanner(System.in).nextLine();
                if (command.equalsIgnoreCase("joingame")) {
                    alreadyJoined = setupConnectionWithPrimary(buf);
                }
            }
            try {
                command = ""; //$NON-NLS-1$
                while (command.trim().length() == 0)
                    command = new Scanner(System.in).nextLine().trim();

                if (this.sockOutput != null
                        && !command
                        .equalsIgnoreCase("joingame")) {
                    this.sockOutput.write(command.getBytes(), 0,
                            command.getBytes().length);
                    this.sockOutput.flush();
                }
                buf = new byte[4096];
                if (this.sockInput != null) {
                    this.sockInput.read(buf, 0, buf.length);
                }
            }  catch (Exception serverException){
                //Incase if the primary server is not responding, connect to Secondary.
                System.out.println("Primary down, connecting to backup... " + backupIP + ":" + backupPort);
                serverHostname = backupIP;
                serverPort = Integer.parseInt(backupPort.split("\n")[0]);
                alreadyJoined = setupConnectionWithPrimary(buf);
                System.out.println("Connected to the Backup Server successfully!!!... " + backupIP + ":" + backupPort);

            }


            System.out.println(new String(buf));
        }
    }

    private boolean setupConnectionWithPrimary(byte[] buf) throws Exception {
        boolean alreadyJoined;
        sock = new Socket(this.serverHostname, this.serverPort);
        sock.setTcpNoDelay(true);
        this.sockInput = new BufferedInputStream(sock
                .getInputStream());
        this.sockOutput = sock.getOutputStream();
        sock.setTcpNoDelay(true);
        sock.setKeepAlive(true);
        //sock.setSoTimeout(1000);
        // Execute this branch only once during client setup
        this.sockInput.read(buf, 0, buf.length);
        System.out.println(new String(buf));
        // Game begins after 20s
        String response = new String(buf).trim();
        alreadyJoined = true;
        if (response.contains("Backup=")) {
            String temp = response.substring(response.indexOf("Backup=") + 7);
            if (temp.length() > 0) {
                backupPort = temp.split(":")[1];
                backupIP = temp.split(":")[0];
                System.out.println("Backup server : " + temp);
            }
            if (secondPlayer(response)) {
                ServerSocket serverSocket = tryBackupServer();
                backupIP   = serverSocket.getInetAddress().getCanonicalHostName();
                backupPort = ""+serverSocket.getLocalPort();
                        // Send a grid request with the port
                String gridReq = "GridRequest:" + serverSocket.getInetAddress().getCanonicalHostName() + "=" + serverSocket.getLocalPort();
                this.sockOutput.write(gridReq.getBytes(), 0, gridReq.getBytes().length);
                sockOutput.flush();
                game = (Game)new ObjectInputStream(sockInput).readObject();
                System.out.println("The GRID response is : \n" + game.getGrid().toString());
                //now we should start a parallel server here.

                server.setBackupServerSocket(serverSocket);
            }
        }
        return alreadyJoined;
    }

    private ServerSocket tryBackupServer() {
        // Create the backup server instance
        ServerSocket backupServer = null;
        int startPort = 9000;
        boolean serverAvailable = false;
        while (!serverAvailable) {
            try {
                backupServer = new ServerSocket(startPort++);
                serverAvailable = true;
            } catch (Exception e) {
                //cannot start at this port on the same machine.
                //find the next vacant port plz.
                continue;
            }
        }
        return backupServer;
    }


    private boolean secondPlayer(String temp) {
        return temp.contains("Player 2");
    }

}