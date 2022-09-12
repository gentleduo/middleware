package org.duo.redis;

import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;

public class JedisOperate {

    /**
     * redis单机模式
     *
     * @return
     */
    public Jedis connectJedis() {

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(20); // 设置连接redis的最大空闲数
        jedisPoolConfig.setMaxWaitMillis(5000); // 设置连接redis的超时时间
        jedisPoolConfig.setMaxTotal(50); // 设置redis连接最大客户端数
        jedisPoolConfig.setMinIdle(5);
        // 获取redis连接池
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "server01", 6379);
        // 获取redis客户端
        Jedis resource = jedisPool.getResource();
        return resource;
    }

    /**
     * Redis哨兵模式
     *
     * @return
     */
    public Jedis getSentinelConn() {

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMinIdle(5);
        // 哨兵信息
        Set<String> sentinels = new HashSet<>(Arrays.asList("server01:26379",
                "server02:26379", "server03:26379"));
        // 创建连接池
        JedisSentinelPool pool = new JedisSentinelPool("mymaster", sentinels, jedisPoolConfig);
        // 获取客户端
        Jedis jedis = pool.getResource();

        return jedis;
    }


    /**
     * Redis集群模式
     *
     * @return
     */
    public JedisCluster getClusterConn() {

        Set<HostAndPort> portSet = new HashSet<HostAndPort>();
        portSet.add(new HostAndPort("server01", 7001));
        portSet.add(new HostAndPort("server01", 7002));
        portSet.add(new HostAndPort("server01", 7003));
        portSet.add(new HostAndPort("server01", 7004));
        portSet.add(new HostAndPort("server01", 7005));
        portSet.add(new HostAndPort("server01", 7006));
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPoolConfig.setMaxTotal(30);
        jedisPoolConfig.setMinIdle(5);

        JedisCluster jedisCluster = new JedisCluster(portSet, jedisPoolConfig);

        return jedisCluster;
    }

    public static void main(String[] args) throws IOException {

        JedisOperate jedisOperate = new JedisOperate();

//        Jedis resource = jedisOperate.connectJedis();
//        //***********************
//        //操作string类型数据
//        //***********************
//        //添加
//        resource.set("jediskey", "jedisvalue");
//        //查询
//        String jediskey = resource.get("jediskey");
//        System.out.println(jediskey);
//        //修改
//        resource.set("jediskey", "jedisvalueUpdate");
//        //删除
//        resource.del("jediskey");
//        //实现整型数据的增长操作
//        resource.incr("jincr");
//        resource.incrBy("jincr", 3);
//        String jincr = resource.get("jincr");
//        System.out.println(jincr);
//
//        //***********************
//        //操作hash列表类型数据
//        //***********************
//        //添加数据
//        resource.hset("jhsetkey", "jmapkey", "jmapvalue");
//        resource.hset("jhsetkey", "jmapkey2", "jmapvalue2");
//        //获取所有数据
//        Map<String, String> jhsetkey = resource.hgetAll("jhsetkey");
//        for (String s : jhsetkey.keySet()) {
//            System.out.println(s);
//        }
//        //修改数据
//        resource.hset("jhsetkey", "jmapkey2", "jmapvalueupdate2");
//        Map<String, String> jhsetkey2 = resource.hgetAll("jhsetkey");
//        for (String s : jhsetkey2.keySet()) {
//            System.out.println("修改数据打印" + s);
//        }
//        //删除数据
//        resource.del("jhsetkey");
//
//        Set<String> jhsetkey1 = resource.keys("jhsetkey");
//        for (String result : jhsetkey1) {
//            System.out.println(result);
//        }
//
//        //***********************
//        //操作list类型数据
//        //***********************
//        //从左边插入元素
//        resource.lpush("listkey", "listvalue1", "listvalue1", "listvalue2");
//        //从右边移除元素
//        resource.rpop("listkey");
//        //获取所有值
//        List<String> listkey = resource.lrange("listkey", 0, -1);
//        for (String s : listkey) {
//            System.out.println(s);
//        }
//
//        //***********************
//        //操作set类型的数据
//        //***********************
//        //添加数据
//        resource.sadd("setkey", "setvalue1", "setvalue1", "setvalue2", "setvalue3");
//        //查询数据
//        Set<String> setkey = resource.smembers("setkey");
//        for (String s : setkey) {
//            System.out.println(s);
//        }
//        //移除掉一个数据
//        resource.srem("setkey", "setvalue3");
//        resource.close();

//        //***********************
//        //测试Redis哨兵模式
//        //***********************
//        Jedis sentinelConn = jedisOperate.getSentinelConn();
//        // 执行两个命令
//        sentinelConn.set("mykey", "myvalue");
//        String value = sentinelConn.get("mykey");
//        System.out.println(value);
//
//        sentinelConn.close();


        //***********************
        //测试Redis集群模式
        //***********************
        JedisCluster jedisCluster = jedisOperate.getClusterConn();

        jedisCluster.set("cluster", "clustervalue");
        System.out.println(jedisCluster.get("cluster"));

        jedisCluster.close();
    }
}
