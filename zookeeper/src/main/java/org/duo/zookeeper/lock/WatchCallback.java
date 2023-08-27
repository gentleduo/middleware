package org.duo.zookeeper.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallback extends Stat implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {


    ZooKeeper zk;
    String threadName;
    String pathName;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void tryLock() throws InterruptedException, KeeperException {

        zk.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "ctx");
        countDownLatch.await();
    }

    public void unLock() throws InterruptedException, KeeperException {

        zk.delete(pathName, -1);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        // 如果持有锁的节点释放了锁，其实只有监控它的节点收到回调事件
        // 如果有节点掉了，也会造成它后面监控它的节点收到通知，从而让它后面的节点去watch宕机的节点的前面的节点
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "ctx");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }

    /**
     * 每个线程创建节点成功后，执行下面的的回调函数
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {

        if (name != null) {
            System.out.println(threadName + " create node : " + name);
            pathName = name;
            // 节点创建成功后，获取根节点下所有已经创建的节点信息
            zk.getChildren("/", false, this, "ctx");
        }
    }

    /**
     * 该线程以及所有在该线程之前创建的节点
     */
    @Override
    public void processResult(int i, String s, Object o, List<String> list, Stat stat) {

        Collections.sort(list);
        int i1 = list.indexOf(pathName.substring(1));

        // 假设自己排在第一个，也就是第一个创建节点成功的线程，那表示自己第一个抢到锁
        if (i1 == 0) {
            System.out.println(threadName + " i am first ......");
//            try {
//                zk.setData("/", threadName.getBytes(), -1);
//            } catch (KeeperException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            countDownLatch.countDown();
            // 当自己不是第一个时，监控自己的前一个节点
        } else {
            zk.exists("/" + list.get(i1 - 1), this, this, "ctx");
        }
    }

    @Override
    public void processResult(int i, String s, Object o, Stat stat) {

    }
}
