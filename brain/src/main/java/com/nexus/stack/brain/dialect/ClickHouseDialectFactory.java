//package com.nexus.stack.brain.dialect;
//
//import org.apache.flink.connector.jdbc.dialect.JdbcDialect;
//import org.apache.flink.connector.jdbc.dialect.JdbcDialectFactory;
//
//import java.io.Serializable;
//
//public class ClickHouseDialectFactory implements JdbcDialectFactory, Serializable {
//
//    @Override
//    public boolean acceptsURL(String url) {
//        return url.startsWith("jdbc:clickhouse:") || url.startsWith("jdbc:ch:");
//    }
//
//    @Override
//    public JdbcDialect create() {
//        return new ClickHouseDialect();
//    }
//}