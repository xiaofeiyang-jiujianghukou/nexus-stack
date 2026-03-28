//package com.nexus.stack.brain.dialect;
//
//import org.apache.flink.connector.jdbc.converter.JdbcRowConverter;
//import org.apache.flink.connector.jdbc.dialect.JdbcDialect;
//import org.apache.flink.table.api.ValidationException;
//import org.apache.flink.table.types.logical.RowType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.Serializable;
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class ClickHouseDialect implements JdbcDialect, Serializable {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ClickHouseDialect.class);
//
//    @Override
//    public String dialectName() {
//        return "ClickHouse";
//    }
//
//    @Override
//    public JdbcRowConverter getRowConverter(RowType rowType) {
//        return new ClickHouseRowConverter(rowType);
//    }
//
//    @Override
//    public String getLimitClause(long limit) {
//        return "LIMIT " + limit;
//    }
//
//    @Override
//    public void validate(RowType rowType) throws ValidationException {
//        // 可以添加自定义验证逻辑
//        LOG.debug("Validating RowType: {}", rowType);
//    }
//
//    @Override
//    public Optional<String> defaultDriverName() {
//        return Optional.of("com.clickhouse.jdbc.ClickHouseDriver");
//    }
//
//    @Override
//    public String quoteIdentifier(String identifier) {
//        return "`" + identifier + "`";
//    }
//
//    @Override
//    public String getInsertIntoStatement(String tableName, String[] fieldNames) {
//        if (fieldNames == null || fieldNames.length == 0) {
//            throw new ValidationException("Field names cannot be empty for INSERT statement");
//        }
//
//        String columns = Arrays.stream(fieldNames)
//                .map(this::quoteIdentifier)
//                .collect(Collectors.joining(", "));
//
//        String placeholders = Arrays.stream(fieldNames)
//                .map(f -> "?")
//                .collect(Collectors.joining(", "));
//
//        String sql = "INSERT INTO " + quoteIdentifier(tableName) + " (" + columns + ") VALUES (" + placeholders + ")";
//        LOG.debug("Generated INSERT statement: {}", sql);
//        return sql;
//    }
//
//    @Override
//    public String getUpdateStatement(String tableName, String[] fieldNames, String[] conditionFields) {
//        if (fieldNames == null || fieldNames.length == 0) {
//            return ""; // 没有需要更新的字段，返回空
//        }
//
//        if (conditionFields == null || conditionFields.length == 0) {
//            LOG.warn("No condition fields for UPDATE statement, returning empty");
//            return "";
//        }
//
//        String setClause = Arrays.stream(fieldNames)
//                .map(f -> quoteIdentifier(f) + " = ?")
//                .collect(Collectors.joining(", "));
//
//        String whereClause = Arrays.stream(conditionFields)
//                .map(f -> quoteIdentifier(f) + " = ?")
//                .collect(Collectors.joining(" AND "));
//
//        // ClickHouse 使用 ALTER TABLE ... UPDATE 语法
//        String sql = "ALTER TABLE " + quoteIdentifier(tableName) + " UPDATE " + setClause + " WHERE " + whereClause;
//        LOG.debug("Generated UPDATE statement: {}", sql);
//        return sql;
//    }
//
//    @Override
//    public String getDeleteStatement(String tableName, String[] conditionFields) {
//        if (conditionFields == null || conditionFields.length == 0) {
//            LOG.warn("No condition fields for DELETE statement, returning empty");
//            return "";
//        }
//
//        String whereClause = Arrays.stream(conditionFields)
//                .map(f -> quoteIdentifier(f) + " = ?")
//                .collect(Collectors.joining(" AND "));
//
//        // ClickHouse 使用 ALTER TABLE ... DELETE 语法
//        String sql = "ALTER TABLE " + quoteIdentifier(tableName) + " DELETE WHERE " + whereClause;
//        LOG.debug("Generated DELETE statement: {}", sql);
//        return sql;
//    }
//
//    @Override
//    public String getSelectFromStatement(String tableName, String[] selectFields, String[] conditionFields) {
//        if (selectFields == null || selectFields.length == 0) {
//            LOG.warn("No select fields, using *");
//            String sql = "SELECT * FROM " + quoteIdentifier(tableName);
//            if (conditionFields != null && conditionFields.length > 0) {
//                String whereClause = Arrays.stream(conditionFields)
//                        .map(f -> quoteIdentifier(f) + " = ?")
//                        .collect(Collectors.joining(" AND "));
//                sql += " WHERE " + whereClause;
//            }
//            LOG.debug("Generated SELECT statement: {}", sql);
//            return sql;
//        }
//
//        String select = Arrays.stream(selectFields)
//                .map(this::quoteIdentifier)
//                .collect(Collectors.joining(", "));
//
//        String sql = "SELECT " + select + " FROM " + quoteIdentifier(tableName);
//
//        if (conditionFields != null && conditionFields.length > 0) {
//            String whereClause = Arrays.stream(conditionFields)
//                    .map(f -> quoteIdentifier(f) + " = ?")
//                    .collect(Collectors.joining(" AND "));
//            sql += " WHERE " + whereClause;
//        }
//
//        LOG.debug("Generated SELECT statement: {}", sql);
//        return sql;
//    }
//
//    @Override
//    public Optional<String> getUpsertStatement(String tableName, String[] fieldNames, String[] uniqueKeyFields) {
//        if (fieldNames == null || fieldNames.length == 0) {
//            LOG.warn("No field names for UPSERT statement");
//            return Optional.empty();
//        }
//
//        // ClickHouse 没有原生的 UPSERT，使用 INSERT
//        // 如果使用 ReplacingMergeTree 引擎，可以简单使用 INSERT
//        String insertStatement = getInsertIntoStatement(tableName, fieldNames);
//        LOG.debug("Generated UPSERT (INSERT) statement: {}", insertStatement);
//        return Optional.of(insertStatement);
//    }
//
//    @Override
//    public String getRowExistsStatement(String tableName, String[] conditionFields) {
//        if (conditionFields == null || conditionFields.length == 0) {
//            LOG.warn("No condition fields for row exists check");
//            return "";
//        }
//
//        String whereClause = Arrays.stream(conditionFields)
//                .map(f -> quoteIdentifier(f) + " = ?")
//                .collect(Collectors.joining(" AND "));
//
//        String sql = "SELECT 1 FROM " + quoteIdentifier(tableName) + " WHERE " + whereClause + " LIMIT 1";
//        LOG.debug("Generated ROW EXISTS statement: {}", sql);
//        return sql;
//    }
//}