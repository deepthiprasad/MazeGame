import java.util.concurrent.CountDownLatch;


/**
 * Created with IntelliJ IDEA.
 * User: Deepthi Prasad
 * Date: 30/9/13
 * Time: 10:58 AM
 */
public class MazeClientThread extends Thread {


    private final CountDownLatch startSignal;

    private PeerMazeServer server;

    MazeClientThread(CountDownLatch startSignal, PeerMazeServer peerMazeServer) {
        this.startSignal = startSignal;
        this.server = peerMazeServer;
    }

    public void run() {
        try {
            /*Wait till the server is done with initial setup*/
            System.out.println("Waiting for the server to come up.....");

            startSignal.await();

            System.out.println("Server ready to accept requests.....");

            System.out.println("Server port is : " + server.getMazeSocket());

            int portNumber = 9000;
            if(server.getMazeSocket() != null){
               portNumber = server.getMazeSocket().getLocalPort();
            }

            new MazeClient("localhost", portNumber, server).sendSomeMessages();

        } catch (Exception e) {
            System.out.println("Could not send the command to the server....");

        }
    }

}
