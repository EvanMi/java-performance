package com.yumi;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadException {

    public static void main(String[] args) {
        //submitException();
        //executeExceptionWithTryCatch();
        /*
         * 线程池中的线程如果抛出了异常，那么线程池会把该线程移除销毁
         */
        //executeExceptionWithAfterExecute();
        executeExceptionWithUncaughtExceptionHandler();
    }

    private static void submitException() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<Integer> future = pool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1 / 0;
            }
        });
        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // 业务异常就在这里
            System.err.println(e.getCause().getMessage());
        }
        pool.shutdown();
    }














    private static void executeExceptionWithTryCatch() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(() -> {
            try {
                System.out.println(1 / 0);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
        pool.shutdown();
    }





















    private static void executeExceptionWithAfterExecute() {
        ThreadPoolExecutor localPool =localPool =
                new ThreadPoolExecutor(1, 1,
                0, TimeUnit.MICROSECONDS,
                new ArrayBlockingQueue<>(10)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (null != t) {
                    System.err.println(t.getMessage());
                }
                if (r instanceof FutureTask<?>) {
                    try {
                        ((Future<?>)r).get();
                    } catch (ExecutionException e) {
                        System.err.println(e.getMessage());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        localPool.execute(() -> System.out.println(1 / 0));
        localPool.execute(() -> System.out.println("afterException: " + Thread.currentThread().getName()));
        localPool.shutdown();
    }























    private static void executeExceptionWithUncaughtExceptionHandler() {
        ThreadFactory factory = (Runnable r) -> {
          Thread t = new Thread(r);
          t.setUncaughtExceptionHandler((Thread th, Throwable e) -> {
              if (null != e) {
                  System.err.println(e.getMessage());
              }
          });
          return t;
        };
        ThreadPoolExecutor localPool = new ThreadPoolExecutor(1,
                1,
                0, TimeUnit.MICROSECONDS,
                new ArrayBlockingQueue<>(10), factory);
        localPool.execute(() -> System.out.println(1 / 0));
        localPool.shutdown();
    }
}





