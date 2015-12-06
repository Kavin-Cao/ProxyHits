package com.kevin.proxyhits;

public class ProxyServer {
    private String ip;

    private int port;

    public ProxyServer(String ip, int port) {
        super();
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
