package com.nexus.stack.brain.dialect;

import org.apache.flink.connector.jdbc.dialect.JdbcDialectTypeMapper;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.DecimalType;
import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.flink.table.types.logical.TimestampType;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ClickHouseTypeMapper implements JdbcDialectTypeMapper, Serializable {

    @Override
    public DataType mapping(ObjectPath objectPath, ResultSetMetaData resultSetMetaData, int i) throws SQLException {
        String columnType = resultSetMetaData.getColumnTypeName(i);
        int precision = resultSetMetaData.getPrecision(i);
        int scale = resultSetMetaData.getScale(i);

        // ClickHouse 类型映射
        switch (columnType.toUpperCase()) {
            case "INT8":
            case "INT16":
            case "INT32":
                return DataTypes.INT();
            case "INT64":
                return DataTypes.BIGINT();
            case "FLOAT32":
                return DataTypes.FLOAT();
            case "FLOAT64":
                return DataTypes.DOUBLE();
            case "STRING":
            case "FIXEDSTRING":
                return DataTypes.STRING();
            case "DATE":
                return DataTypes.DATE();
            case "DATETIME":
                return DataTypes.TIMESTAMP(3);
            case "DECIMAL":
                return DataTypes.DECIMAL(precision, scale);
            case "BOOL":
            case "BOOLEAN":
                return DataTypes.BOOLEAN();
            default:
                return DataTypes.STRING();
        }
    }
}
