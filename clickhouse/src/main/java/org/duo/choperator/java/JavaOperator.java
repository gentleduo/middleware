package org.duo.choperator.java;

import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseStatement;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.ResultSet;

public class JavaOperator {

    public static void main(String[] args) throws Exception {
        ClickHouseProperties props = new ClickHouseProperties();
        props.setUser("");
        props.setPassword("");
        BalancedClickhouseDataSource dataSource = new BalancedClickhouseDataSource("jdbc:clickhouse://server01:8123,server02:8123,server03:8123/test", props);
        ClickHouseConnection conn = dataSource.getConnection();
        ClickHouseStatement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select id, name from t_java");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id = " + id + ",name = " + name);
        }
        rs.close();
        statement.close();
        conn.close();
    }
}
