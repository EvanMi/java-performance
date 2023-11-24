package com.yumi.close;

import java.net.ServerSocket;
import java.net.Socket;

public class CloseServer {
    //sudo tcpdump -S -i lo0 host 127.0.0.1 and tcp port 12345
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(12345);
        Socket aClient = serverSocket.accept();
        aClient.close();
        System.out.println(aClient.isClosed());
        System.out.println(aClient.getInputStream().read());
        serverSocket.close();
    }
}
