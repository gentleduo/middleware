package org.duo.zookeeper.watch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class WatchTest {

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
    public void testWatch() throws InterruptedException {

        WatchCallback watchCallback = new WatchCallback();
        DataBean dataBean = new DataBean();
        watchCallback.setZk(zk);
        watchCallback.setDataBean(dataBean);

        watchCallback.await();
        while (true) {

            if ("".equals(dataBean.getData())) {
                System.out.println("wait.......");
                watchCallback.await();
            } else {
                System.out.println(dataBean.getData());
            }
            Thread.sleep(1000);
        }
    }
}
