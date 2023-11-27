package com.P2P_FileSharing;

public class MessageQueueElement {
    ConnectionDetails connection_details;
    Object msg;

    public MessageQueueElement(ConnectionDetails connection_details, Object msg) {
        this.connection_details = connection_details;
        this.msg = msg;
    }

    public ConnectionDetails getConnection_details() {
        return connection_details;
    }

    public void setConnection_details(ConnectionDetails connection_details) {
        this.connection_details = connection_details;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }
}