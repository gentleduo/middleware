package org.duo.choperator.spark

import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

import java.util.Properties

object sparkOperator {

  def main(args: Array[String]): Unit = {

    val session: SparkSession = SparkSession.builder().master("local").appName("test").getOrCreate()
    val jsonList: Seq[String] = List[String](
      "{\"id\":1,\"name\":\"张三\",\"age\":18}",
      "{\"id\":2,\"name\":\"李四\",\"age\":19}",
      "{\"id\":3,\"name\":\"王五\",\"age\":20}"
    )

    //将jsonList数据转换成DataSet
    import session.implicits._
    val ds: Dataset[String] = jsonList.toDS()

    val df: DataFrame = session.read.json(ds)
    df.show()

    //将结果写往ClickHouse
    val url = "jdbc:clickhouse://server01:8123/test"
    val table = "t_java"

    val properties = new Properties()
    properties.put("driver", "ru.yandex.clickhouse.ClickHouseDriver")
    properties.put("user", "default")
    properties.put("password", "")
    properties.put("socket_timeout", "300000")

    df.write.mode(SaveMode.Append).option(JDBCOptions.JDBC_BATCH_INSERT_SIZE, 100000).jdbc(url, table, properties)

  }

}
