package com.nus.maze.client;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class MazeClient {
    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data = null;
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private LinkedList<Integer> serverPortPool = new LinkedList<Integer>();
    public MazeClient(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
        for(int i=9000;i<=9010;i++)
            serverPortPool.add(i);
        System.out.println(serverPortPool);
    }

    public static void main(String argv[]) throws Exception {
        String hostname = "localhost";
        //TODO: make the port dynamic for multiple clients
        int port = 9000;
        byte[] data = "Hello World".getBytes();

        MazeClient client = new MazeClient(hostname, port, data);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost",9000);
                    assert inetSocketAddress!=null && !inetSocketAddress.isUnresolved();
                    System.out.println("Connected to server at : " + 9000);
                }catch (Exception e){
                    System.out.println("Cannot connect to the server.... stopping the game now!");
                }
            }
        }, 1000, 2000);
        client.sendSomeMessages();
        //run the heartbeat monitor every 1 second and notify if couldn't connect.

    }

    public void sendSomeMessages() throws Exception {
        System.err.println("Opening connection to " + serverHostname + " port " + serverPort);
        boolean alreadyJoined = false;
        while (true) {
            String command = new Scanner(System.in).nextLine();
            byte[] buf = new byte[1024];
            if (command.equalsIgnoreCase("joinGame") && !alreadyJoined) {

                sock = new Socket(serverHostname, serverPort);
                sockInput = sock.getInputStream();
                sockOutput = sock.getOutputStream();
                alreadyJoined = true;
            }
            if(sockOutput!=null && !command.equalsIgnoreCase("joingame")){
                sockOutput.write(command.getBytes(), 0, command.getBytes().length);
            }
            if(sockInput!=null){
                sockInput.read(buf, 0, buf.length);
            }
            System.out.println(new String(buf));
        }
    }
}