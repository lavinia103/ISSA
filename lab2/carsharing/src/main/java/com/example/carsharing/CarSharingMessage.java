package com.example.carsharing;
public class CarSharingMessage {
    private String clientId;
    private int clientType; // 0 for Owner, 1 for Renter
    private int messageId; // MessageId values: 0, 1, 2, 3, 4, 5
    private String payload; // Additional data

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }



}
