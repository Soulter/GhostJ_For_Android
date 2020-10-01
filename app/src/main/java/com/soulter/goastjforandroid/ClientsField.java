package com.soulter.goastjforandroid;

public class ClientsField {
    private String clientName;
    private String clientNum;

    public ClientsField(String clientName, String clientNum){
        this.clientName = clientName;
        this.clientNum = clientNum;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientNum() {
        return clientNum;
    }
}
