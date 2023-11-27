package com.P2P_FileSharing;

import java.io.Serializable;

public class ActualMessage implements Serializable {
    private int messageLength;
    private int messageType;
    private String messagePayload;
    private static final long serialVersionUID = 8983558202217591746L;

    public ActualMessage() {}

    public ActualMessage(int messageType, String messagePayload) {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        if(messagePayload != null)
            this.messageLength = messagePayload.length() + 1;
        else
            this.messageLength = 1;
    }

    public ActualMessage(int messageType) {
        this.messageType = messageType;
        this.messageLength = 1;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }
}

class PieceMessage extends ActualMessage {
    int messageType;
    byte[] piece;

    public PieceMessage(int messageType, byte[] piece) {
        this.messageType = messageType;
        this.piece = piece;
    }

    public byte[] getPiece() {
        return piece;
    }

    public void setPiece(byte[] piece) {
        this.piece = piece;
    }
}
