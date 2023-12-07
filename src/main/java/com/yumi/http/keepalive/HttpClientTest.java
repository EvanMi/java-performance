package com.yumi.http.keepalive;

import java.util.concurrent.TimeUnit;

public class HttpClientTest {
    public static void main(String[] args) throws Exception{
        try {
            HttpClient httpClient = new HttpClient();
            for (int i = 0 ; i < 10; i++) {
                String s = httpClient.get("http://127.0.0.1:8080/", true);
                System.out.println(s);
                TimeUnit.MILLISECONDS.sleep(20000);
            }
        } finally {
            NettyClientFactory.shutdown();
        }
    }
}
