package com.sprutuswesticus.network;

public class NetworkConfig {

    public final boolean valid;

    private String ip;
    private int port;
    private String user;

    public NetworkConfig() {
        valid = false;
    }

    public NetworkConfig(String ip, int port, String user) {
        this.ip = ip;
        this.port = port;
        this.user = user;

        valid = true;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

}
