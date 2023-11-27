package com.P2P_FileSharing;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server extends Thread{

    private static int sPort;
    Peer peer;

    public Server(int port, Peer peer) {
        sPort = port;
        this.peer = peer;
    }

    public void run() {
        System.out.println("The server is running.");
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(sPort);
            int clientNum = 1;
            System.out.println("in server " + Thread.currentThread().getName());
            while(Boolean.TRUE) {
                Socket socket = listener.accept();
                System.out.println("Client "  + clientNum + " is connected!");
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String peerIdConnected = Integer.toString(clientNum);
                ConnectionDetails connectionDetails = new ConnectionDetails(peerIdConnected, socket, out, in, peer, new ConcurrentLinkedQueue<Object>());
                peer.getConnections().add(connectionDetails);
                connectionDetails.start();
                clientNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
