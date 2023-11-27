package com.P2P_FileSharing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class ConnectionDetails extends Thread{

    private String peerId;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isHandShakeDone;
    protected Queue<Object> messageQueue;
    private Peer peerLocal;
    private MessageHandler messageHandler;

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public Queue<Object> getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(Queue<Object> messageQueue) {
        this.messageQueue = messageQueue;
    }
    
    public int getLength() {
    	return messageQueue.size();
    }

    public ConnectionDetails(String peerId, Socket socket, ObjectOutputStream out, ObjectInputStream in, Peer peerLocal, Queue<Object> messageQueue) {
        this.peerId = peerId;
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.peerLocal = peerLocal;
        this.messageQueue = messageQueue;
        
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    synchronized public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public boolean isHandShakeDone() {
        return isHandShakeDone;
    }

    public void setHandShakeDone(boolean handShakeDone) {
        this.isHandShakeDone = handShakeDone;
    }

    public void run() {
        try{
        	System.out.println("in Connection thread " + Thread.currentThread().getName()+""+this.hashCode()+" "+ messageQueue.hashCode());
        	this.messageHandler = new MessageHandler(this.peerLocal, this, this.messageQueue);
            messageHandler.start();
            while(true)
            {
                Object message = this.in.readObject();
                messageQueue.add(message);
            }
        }
        catch(ClassNotFoundException classNot){
            classNot.printStackTrace();
        }
        catch(IOException ioException){
            try {
                peerLocal.getBw().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ioException.printStackTrace();
        }
    }

    synchronized public void send(Object message) throws IOException {
        System.out.print("[Response] : ");
        if(message instanceof HandShake) {
            System.out.println("Sending Handshake message to " + peerId);
        }
        else if(message instanceof ActualMessage) {
            if(((ActualMessage) message).getMessageType()  == 7)
                System.out.println("Sending Actual message of type " + ((ActualMessage) message).getMessageType() +" with payload " + ((ActualMessage) message).getMessagePayload().split("-")[0]);
            else
                System.out.println("Sending Actual message of type " + ((ActualMessage) message).getMessageType() +" with payload " + ((ActualMessage) message).getMessagePayload());
        }
        out.writeObject(message);
        out.flush();
    }

    synchronized public void send(Object message, int piece) throws IOException {
        System.out.print("[Response] : ");
        if(message instanceof HandShake) {
            System.out.println("Sending Handshake message to " + peerId);
        }
        else if(message instanceof PieceMessage) {
            System.out.println("Sending Actual message of type " + ((PieceMessage) message).getMessageType() +" with payload " + piece);
        }
        else if(message instanceof ActualMessage) {
            if(((ActualMessage) message).getMessageType()  == 7)
                System.out.println("Sending Actual message of type " + ((ActualMessage) message).getMessageType() +" with payload " + piece);
            else
                System.out.println("Sending Actual message of type " + ((ActualMessage) message).getMessageType() +" with payload " + ((ActualMessage) message).getMessagePayload());
        }
        out.writeObject(message);
        out.flush();
    }
}
