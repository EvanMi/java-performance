package com.yumi;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewSocketsConcurrent {

    public static void main(String[] args) throws Exception{
        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int connections = Integer.parseInt(args[2]);

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        long before = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(8);
        for (int i = 0; i < 8; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < connections; j++) {
                    try {
                        Socket socket = new Socket(serverHost, serverPort);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        long after = System.currentTimeMillis();
        System.out.println((after - before));
    }
}
