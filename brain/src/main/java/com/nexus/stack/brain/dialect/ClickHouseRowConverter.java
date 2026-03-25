package com.nexus.stack.brain.dialect;

import org.apache.flink.connector.jdbc.converter.JdbcRowConverter;
import org.apache.flink.connector.jdbc.statement.FieldNamedPreparedStatement;
import org.apache.flink.table.data.*;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ClickHouseRowConverter implements JdbcRowConverter, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ClickHouseRowConverter.class);
    private static final long serialVersionUID = 1L;

    private final RowType rowType;
    private final JdbcDeserializationConverter[] deserializationConverters;
    private final JdbcSerializationConverter[] serializationConverters;

    public ClickHouseRowConverter(RowType rowType) {
        this.rowType = rowType;
        this.deserializationConverters = new JdbcDeserializationConverter[rowType.getFieldCount()];
        this.serializationConverters = new JdbcSerializationConverter[rowType.getFieldCount()];

        for (int i = 0; i < rowType.getFieldCount(); i++) {
            deserializationConverters[i] = createDeserializationConverter(rowType.getTypeAt(i));
            serializationConverters[i] = createSerializationConverter(rowType.getTypeAt(i));
        }
    }

    @Override
    public RowData toInternal(ResultSet resultSet) throws SQLException {
        GenericRowData genericRowData = new GenericRowData(rowType.getFieldCount());
        for (int i = 0; i < rowType.getFieldCount(); i++) {
            Object field = deserializationConverters[i].deserialize(resultSet, i + 1);
            genericRowData.setField(i, field);
        }
        return genericRowData;
    }

    @Override
    public FieldNamedPreparedStatement toExternal(RowData rowData, FieldNamedPreparedStatement statement) throws SQLException {
        for (int i = 0; i < rowData.getArity(); i++) {
            serializationConverters[i].serialize(rowData, i, statement);
        }
        return statement;
    }

    private JdbcDeserializationConverter createDeserializationConverter(LogicalType type) {
        // 使用 switch 返回具体的转换器实例，而不是 Lambda
        switch (type.getTypeRoot()) {
            case INTEGER:
                return new IntegerDeserializationConverter();
            case BIGINT:
                return new LongDeserializationConverter();
            case FLOAT:
                return new FloatDeserializationConverter();
            case DOUBLE:
                return new DoubleDeserializationConverter();
            case VARCHAR:
            case CHAR:
                return new StringDeserializationConverter();
            case BOOLEAN:
                return new BooleanDeserializationConverter();
            case DECIMAL:
                return new DecimalDeserializationConverter();
            case TIMESTAMP_WITHOUT_TIME_ZONE:
                return new TimestampDeserializationConverter();
            case DATE:
                return new DateDeserializationConverter();
            default:
                return new DefaultDeserializationConverter();
        }
    }

    private JdbcSerializationConverter createSerializationConverter(LogicalType type) {
        // 使用 switch 返回具体的转换器实例，而不是 Lambda
        switch (type.getTypeRoot()) {
            case INTEGER:
                return new IntegerSerializationConverter();
            case BIGINT:
                return new LongSerializationConverter();
            case FLOAT:
                return new FloatSerializationConverter();
            case DOUBLE:
                return new DoubleSerializationConverter();
            case VARCHAR:
            case CHAR:
                return new StringSerializationConverter();
            case BOOLEAN:
                return new BooleanSerializationConverter();
            case DECIMAL:
                return new DecimalSerializationConverter();
            case TIMESTAMP_WITHOUT_TIME_ZONE:
                return new TimestampSerializationConverter();
            case DATE:
                return new DateSerializationConverter();
            default:
                return new DefaultSerializationConverter();
        }
    }

    // ========== Deserialization Converters ==========

    private interface JdbcDeserializationConverter extends Serializable {
        Object deserialize(ResultSet resultSet, int index) throws SQLException;
    }

    private static class IntegerDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getInt(index);
        }
    }

    private static class LongDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getLong(index);
        }
    }

    private static class FloatDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getFloat(index);
        }
    }

    private static class DoubleDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getDouble(index);
        }
    }

    private static class StringDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            String value = resultSet.getString(index);
            return value == null ? null : StringData.fromString(value);
        }
    }

    private static class BooleanDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getBoolean(index);
        }
    }

    private static class DecimalDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            BigDecimal decimal = resultSet.getBigDecimal(index);
            return decimal == null ? null : DecimalData.fromBigDecimal(decimal, 38, 18);
        }
    }

    private static class TimestampDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            Timestamp timestamp = resultSet.getTimestamp(index);
            return timestamp == null ? null : TimestampData.fromTimestamp(timestamp);
        }
    }

    private static class DateDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            Date date = resultSet.getDate(index);
            return date == null ? null : (int) date.toLocalDate().toEpochDay();
        }
    }

    private static class DefaultDeserializationConverter implements JdbcDeserializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public Object deserialize(ResultSet resultSet, int index) throws SQLException {
            String value = resultSet.getString(index);
            return value == null ? null : StringData.fromString(value);
        }
    }

    // ========== Serialization Converters ==========

    private interface JdbcSerializationConverter extends Serializable {
        void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException;
    }

    private static class IntegerSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setInt(index, rowData.getInt(index));
            } else {
                statement.setNull(index, java.sql.Types.INTEGER);
            }
        }
    }

    private static class LongSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setLong(index, rowData.getLong(index));
            } else {
                statement.setNull(index, java.sql.Types.BIGINT);
            }
        }
    }

    private static class FloatSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setFloat(index, rowData.getFloat(index));
            } else {
                statement.setNull(index, java.sql.Types.FLOAT);
            }
        }
    }

    private static class DoubleSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setDouble(index, rowData.getDouble(index));
            } else {
                statement.setNull(index, java.sql.Types.DOUBLE);
            }
        }
    }

    private static class StringSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setString(index, rowData.getString(index).toString());
            } else {
                statement.setNull(index, java.sql.Types.VARCHAR);
            }
        }
    }

    private static class BooleanSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setBoolean(index, rowData.getBoolean(index));
            } else {
                statement.setNull(index, java.sql.Types.BOOLEAN);
            }
        }
    }

    private static class DecimalSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setBigDecimal(index, rowData.getDecimal(index, 38, 18).toBigDecimal());
            } else {
                statement.setNull(index, java.sql.Types.DECIMAL);
            }
        }
    }

    private static class TimestampSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setTimestamp(index, rowData.getTimestamp(index, 3).toTimestamp());
            } else {
                statement.setNull(index, java.sql.Types.TIMESTAMP);
            }
        }
    }

    private static class DateSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setDate(index, java.sql.Date.valueOf(
                        java.time.LocalDate.ofEpochDay(rowData.getInt(index))
                ));
            } else {
                statement.setNull(index, java.sql.Types.DATE);
            }
        }
    }

    private static class DefaultSerializationConverter implements JdbcSerializationConverter {
        private static final long serialVersionUID = 1L;
        @Override
        public void serialize(RowData rowData, int index, FieldNamedPreparedStatement statement) throws SQLException {
            if (!rowData.isNullAt(index)) {
                statement.setString(index, rowData.getString(index).toString());
            } else {
                statement.setNull(index, java.sql.Types.VARCHAR);
            }
        }
    }
}