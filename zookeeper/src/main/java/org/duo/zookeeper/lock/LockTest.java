package org.duo.zookeeper.lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class LockTest {

    ZooKeeper zk;

    @Before
    public void conn() throws IOException, InterruptedException {
        ZKUtils zkUtils = new ZKUtils();
        zk = zkUtils.getZk();
    }

    @After
    public void close() throws InterruptedException {
        zk.close();
    }

    @Test
    public void lock() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            new Thread() {

                public void run() {

                    String name = Thread.currentThread().getName();
                    WatchCallback watchCallback = new WatchCallback();
                    watchCallback.setZk(zk);
                    watchCallback.setThreadName(name);
                    // 抢锁
                    try {
                        watchCallback.tryLock();
                        // 执行业务逻辑
                        System.out.println(name + " running......");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    }
                    // 释放锁
                    try {
                        watchCallback.unLock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        while (true) {
            Thread.sleep(1000);
        }
    }
}
