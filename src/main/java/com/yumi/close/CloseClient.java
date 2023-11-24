package com.yumi.close;

import java.net.Socket;

public class CloseClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 12345);
        //System.out.println("接收一个消息: " + socket.getInputStream().read());
        int i = 0;
        while (i < 100000) {
            socket.getOutputStream().write("1".getBytes());
            socket.getOutputStream().flush();
            i++;
        }
        socket.close();
    }
}
