package com.yumi;

import java.net.Socket;

public class NewSockets2000 {
    public static void main(String[] args) throws Exception{
        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int connections = Integer.parseInt(args[2]);
        long before = System.currentTimeMillis();
        for (int i = 0; i < connections; i++) {
            Socket socket = new Socket(serverHost, serverPort);
        }
        long after = System.currentTimeMillis();
        System.out.println((after - before));
    }
}
