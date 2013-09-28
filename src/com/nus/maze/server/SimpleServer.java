package com.nus.maze.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServer {
    private int serverPort = 0;
    private ServerSocket serverSock = null;
    private Socket sock = null;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public SimpleServer(int serverPort) throws IOException {
        this.serverPort = serverPort;

        serverSock = new ServerSocket(serverPort);
    }

    public void waitForConnections() {
        while (true) {
            try {
                sock = serverSock.accept();
                System.err.println("SimpleServer:Accepted new socket, creating new handler for it.");
                //executorService.execute(new PlayerHandlerThread(sock));
                System.err.println("SimpleServer:Finished with socket, waiting for next connection.");
            }
            catch (IOException e){
                e.printStackTrace(System.err);
            }
        }
    }

    public static void main(String argv[]) {
        int port = 54321;

        SimpleServer server = null;
        try {
            server = new SimpleServer(port);
        }
        catch (IOException e){
            e.printStackTrace(System.err);
        }
        server.waitForConnections();
    }
}