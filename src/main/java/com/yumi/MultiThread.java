package com.yumi;

import java.util.concurrent.TimeUnit;

public class MultiThread {

    static int cnt = 0;
    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    if (cnt % 2 == 0) {
                        System.out.println("t1:" + (++cnt));
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        lock.notifyAll();
                    } else {
                        try {
                            lock.wait();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    if (cnt % 2 != 0) {
                        System.out.println("t2:" + (++cnt));
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        lock.notifyAll();
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }).start();

        TimeUnit.SECONDS.sleep(10000);
    }


}
