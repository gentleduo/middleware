package org.duo.choperator.flink

import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcExecutionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import java.sql.PreparedStatement


//import org.apache.flink.api.common.typeinfo.Types
//import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
//import org.apache.flink.table.api.{EnvironmentSettings, Table}
//import org.apache.flink.table.api.scala.StreamTableEnvironment


/**
 * 通过 flink-jdbc API 将 Flink 数据结果写入到ClickHouse中，只支持Table API
 *
 * 注意：
 * 1.由于 ClickHouse 单次插入的延迟比较高，我们需要设置 BatchSize 来批量插入数据，提高性能。
 * 2.在 JDBCAppendTableSink 的实现中，若最后一批数据的数目不足 BatchSize，则不会插入剩余数据。
 */

case class PersonInfo(id: Int, name: String, age: Int)

object flinkOperator {

  def main(args: Array[String]): Unit = {

    // flink 1.10.x 及之前版本
    /*    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        //设置并行度为1，后期每个并行度满批次需要的条数时，会插入click中
        env.setParallelism(1)
        val settings: EnvironmentSettings = EnvironmentSettings.newInstance().inStreamingMode().useBlinkPlanner().build()
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env, settings)

        //导入隐式转换
        import org.apache.flink.streaming.api.scala._

        //读取Socket中的数据
        val sourceDS: DataStream[String] = env.socketTextStream("server01", 9999)
        val ds: DataStream[PersonInfo] = sourceDS.map(line => {
          val arr: Array[String] = line.split(",")
          PersonInfo(arr(0).toInt, arr(1), arr(2).toInt)
        })

        //将 ds 转换成 table 对象
        import org.apache.flink.table.api.scala._
        val table: Table = tableEnv.fromDataStream(ds, 'id, 'name, 'age)

        //将table 对象写入ClickHouse中
        //需要在ClickHouse中创建表:create table flink_result(id Int,name String,age Int) engine = MergeTree() order by id;
        val insertIntoCkSql = "insert into t_java (id,name,age) values (?,?,?)"

        //准备ClickHouse table sink
        val sink: JDBCAppendTableSink = JDBCAppendTableSink.builder()
          .setDrivername("ru.yandex.clickhouse.ClickHouseDriver")
          .setDBUrl("jdbc:clickhouse://server01:8123/test")
          .setUsername("default")
          .setPassword("")
          .setQuery(insertIntoCkSql)
          .setBatchSize(2) //设置批次量,默认5000条
          .setParameterTypes(Types.INT, Types.STRING, Types.INT)
          .build()

        //注册ClickHouse table Sink，设置sink 数据的字段及Schema信息
        tableEnv.registerTableSink("ck-sink",
          sink.configure(Array("id", "name", "age"), Array(Types.INT, Types.STRING, Types.INT)))

        //将数据插入到 ClickHouse Sink 中
        tableEnv.insertInto(table, "ck-sink")

        //触发以上执行
        env.execute("Flink Table API to ClickHouse Example")*/

    //flink 1.11.x 及之后版本
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //设置并行度为1
    env.setParallelism(1)
    import org.apache.flink.streaming.api.scala._

    val ds: DataStream[String] = env.socketTextStream("server01", 9999)

    val result: DataStream[(Int, String, Int)] = ds.map(line => {
      val arr: Array[String] = line.split(",")
      (arr(0).toInt, arr(1), arr(2).toInt)
    })

    //准备向ClickHouse中插入数据的sql
    val insetIntoCkSql = "insert into t_java (id,name,age) values (?,?,?)"

    //设置ClickHouse Sink
    val ckSink: SinkFunction[(Int, String, Int)] = JdbcSink.sink(
      //插入数据SQL
      insetIntoCkSql,

      //设置插入ClickHouse数据的参数
      new JdbcStatementBuilder[(Int, String, Int)] {
        override def accept(ps: PreparedStatement, tp: (Int, String, Int)): Unit = {
          ps.setInt(1, tp._1)
          ps.setString(2, tp._2)
          ps.setInt(3, tp._3)
        }
      },
      //设置批次插入数据
      new JdbcExecutionOptions.Builder().withBatchSize(2).build(),

      //设置连接ClickHouse的配置
      new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withDriverName("ru.yandex.clickhouse.ClickHouseDriver")
        .withUrl("jdbc:clickhouse://server01:8123/test")
        .withUsername("default")
        .withUsername("")
        .build()
    )

    //针对数据加入sink
    result.addSink(ckSink)

    env.execute("Flink DataStream to ClickHouse Example")
  }
}
