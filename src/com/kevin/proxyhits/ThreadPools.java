package com.kevin.proxyhits;

import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPools {
    // 池中所保存的线程数，包括空闲线程。
    private static int corePoolSize = 500;

    // 池中允许的最大线程数。
    private static int maximumPoolSize = 1000;

    // 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
    private static long keepAliveTime = 60;

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new RejectedExecutionHandler() {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            new Thread(r, "exception by pool").start();
        }

    });

    private final static ThreadPools instance = new ThreadPools();

    private ThreadPools() {

    }

    public static ThreadPools getPool() {
        return instance;
    }

    public void shutdown() {
        pool.shutdown();
    }

    public boolean isTerminated() {
        return pool.isTerminated();
    }

    public synchronized void addTask(Runnable run) {
        pool.execute(run);
    }

    public int getActiveCount() {
        return pool.getActiveCount();
    }

    public synchronized void addTask(List<Runnable> runs) {
        if (runs != null) {
            for (Runnable r : runs) {
                this.addTask(r);
            }
        }
    }

    public void closePool() {
        pool.shutdown();
    }
}
