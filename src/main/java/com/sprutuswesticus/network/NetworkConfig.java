package com.sprutuswesticus.network;

public class NetworkConfig {

    private int port;
    private String user;

    public NetworkConfig(int port, String user) {
        this.port = port;
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

}
