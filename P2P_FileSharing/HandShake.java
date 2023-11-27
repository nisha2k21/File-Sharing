package com.P2P_FileSharing;
import java.io.*;

public class HandShake implements Serializable{
    private String header = "P2PFILESHARINGPROJ";
    private byte[] zeroBits = new byte[10];
    private String peerID;
    private static final long serialVersionUID = 42L;

    public HandShake() {}

    public HandShake(String peerID) {
        this.peerID = peerID;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public byte[] getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits = zeroBits;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }
}
