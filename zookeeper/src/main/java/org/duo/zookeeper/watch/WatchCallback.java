package org.duo.zookeeper.watch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {


    ZooKeeper zk;
    DataBean dataBean;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public DataBean getDataBean() {
        return dataBean;
    }

    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
    }

    public void await() throws InterruptedException {
        // 方法里面的第二个参数表示：在"/dataWatch"节点上注册了一个watch，
        // 方法里面的第三个参数表示：zk中存在"/dataWatch"节点的时候将执行的一个异步回调方法：AsyncCallback.StatCallback
        // 在自定义的WatchCallback类中，既实现了Watcher接口，又实现了AsyncCallback.StatCallback, AsyncCallback.DataCallback接口
        zk.exists("/dataWatch", this, this, "ctx");
        countDownLatch.await();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                // 方法里面的第二个参数表示：在收到NodeCreated事件后又在"/dataWatch"节点上注册了一个watch，
                // 方法里面的第三个参数表示：获取zk中"/dataWatch"节点的值的时候将执行的一个异步回调方法：AsyncCallback.DataCallback
                zk.getData("/dataWatch", this, this, "ctx");
                break;
            case NodeDeleted:
                // 当节点别删除后，将数据清空，并重新设置CountDownLatch，这样当有线程再次调用await方法来获取数据时，将重新进入阻塞状态
                dataBean.setData("");
                countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                // 方法里面的第二个参数表示：在收到NodeDataChanged事件后又在"/dataWatch"节点上注册了一个watch，
                // 方法里面的第三个参数表示：获取zk中"/dataWatch"节点的值的时候将执行的一个异步回调方法：AsyncCallback.DataCallback
                zk.getData("/dataWatch", this, this, "ctx");
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

    @Override
    public void processResult(int i, String s, Object o, byte[] bytes, Stat stat) {


        if (bytes != null) {
            String data = new String(bytes);
            dataBean.setData(data);
            countDownLatch.countDown();
        }
    }

    @Override
    public void processResult(int i, String s, Object o, Stat stat) {

        if (stat != null) {

            zk.getData("/dataWatch", this, this, "ctx");
        }
    }
}
