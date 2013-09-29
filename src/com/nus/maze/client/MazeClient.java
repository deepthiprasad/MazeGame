package com.nus.maze.client;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class MazeClient {
    private static Socket sock = null;
    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private LinkedList<Integer> serverPortPool = new LinkedList<Integer>();

    public MazeClient(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
        for (int i = 9000; i <= 9010; i++)
            serverPortPool.add(i);
        System.out.println(serverPortPool);
    }

    public static void main(String argv[]) throws Exception {
        String hostname = "localhost";
        //TODO: make the port dynamic for multiple clients
        int port = 9000;
        byte[] data = "Hello World".getBytes();

        MazeClient client = new MazeClient(hostname, port, data);

        client.sendSomeMessages();

        //run the heartbeat monitor every 1 second and notify if couldn't connect.

    }

    public void sendSomeMessages() throws Exception {
        System.err.println("Opening connection to " + serverHostname + " port " + serverPort);
        boolean alreadyJoined = false;

        while (true) {
            byte[] buf = new byte[2048];

            String command = "";
            if (!alreadyJoined) {
                command = new Scanner(System.in).nextLine();
                if (command.equalsIgnoreCase("joinGame")) {

                    sock = new Socket(serverHostname, serverPort);
                    sock.setTcpNoDelay(true);
                    sockInput = new BufferedInputStream(sock.getInputStream());
                    sockOutput = sock.getOutputStream();
                    alreadyJoined = true;
                    while (alreadyJoined) {
                        sockInput.read(buf, 0, buf.length);
                        System.out.println(new String(buf));
                        if (new String(buf).contains("Game")) {
                            break;
                        }
                    }
                }
            }
            command = "";
            while(command.trim().length()==0)
                command = new Scanner(System.in).nextLine();

            if (sockOutput != null && !command.equalsIgnoreCase("joingame")) {
                sockOutput.write(command.getBytes(), 0, command.getBytes().length);
                sockOutput.flush();
            }
            buf = new byte[2048];
            if (sockInput != null) {
                sockInput.read(buf, 0, buf.length);
            }

            System.out.println(new String(buf));
        }
    }
}