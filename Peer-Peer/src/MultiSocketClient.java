import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created with IntelliJ IDEA.
 * User: Venkatesh JK
 * Date: 5/10/13
 * Time: 7:21 PM
 */
public class MultiSocketClient {

    private MulticastSocket socket;

    MultiSocketClient(MulticastSocket socket) {
        this.socket = socket;
    }

    public void listenForMessages() {
        boolean alreadyReceieved = false;
        while (true)
            try {
                receiveMessage();
                alreadyReceieved = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private void receiveMessage() throws IOException {
        DatagramPacket packet;
        byte[] buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData());
        System.out.println(received);
    }
}
