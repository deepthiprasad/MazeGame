import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Venkatesh JK
 * Date: 5/10/13
 * Time: 7:44 PM
 */
class MulticastServerThread extends Thread {

    private CountDownLatch gameStatusNotification;

    private CountDownLatch gameEndNotification;

    MulticastServerThread(CountDownLatch gameStatusNotification, CountDownLatch gameEndNotification) {
        this.gameStatusNotification = gameStatusNotification;
        this.gameEndNotification = gameEndNotification;
    }

    public void run() {
        boolean notified = false;
        while (!notified)
            try {

                gameStatusNotification.await();

                notified = sendMessage("Game has started, please start collecting your coins as much as you can!");

                gameEndNotification.await();

                notified = sendMessage("Game has ended. Please see the score card to know the Player status");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
    }

    private boolean sendMessage(String dString) throws IOException {
        boolean notified;
        byte[] buf = new byte[1024];

        buf = dString.getBytes();

        InetAddress group = InetAddress.getByName("224.0.0.2");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
        MulticastSocket socket = new MulticastSocket(4446);
        socket.send(packet);
        notified = true;
        return notified;
    }
}