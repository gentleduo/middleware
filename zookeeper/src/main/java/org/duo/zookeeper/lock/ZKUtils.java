package org.duo.zookeeper.lock;

import org.apache.zookeeper.ZooKeeper;
import org.duo.zookeeper.watch.DefaultWatch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {

    private String address = "192.168.56.101:2181,192.168.56.102:2181,192.168.56.103:2181/lock";

    // ZooKeeper的连接状态是通过watch进行异步通知的，所以在new完之后要通过CountDownLatch阻塞主线程
    // 直到watch收到SyncConnected信号后再唤醒阻塞的主线程，返回ZooKeeper连接对象
    CountDownLatch intCountDownLatch = new CountDownLatch(1);

    public ZooKeeper getZk() throws IOException, InterruptedException {

        DefaultWatch defaultWatch = new DefaultWatch();
        defaultWatch.setIntCountDownLatch(intCountDownLatch);
        ZooKeeper zk = new ZooKeeper(address, 1000, defaultWatch);
        intCountDownLatch.await();
        return zk;
    }
}
