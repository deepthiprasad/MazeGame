import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Venkatesh JK
 * Date: 5/10/13
 * Time: 7:41 PM
 */
public class MulticastClientThread extends Thread {

    private final MultiSocketClient client;
    private final CountDownLatch notification;

    MulticastClientThread(MulticastSocket socket, CountDownLatch gameStartedNotification){
        client = new MultiSocketClient(socket);
        this.notification = gameStartedNotification;
    }

    public void run(){
        try {
            //notification.await();
            client.listenForMessages();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
