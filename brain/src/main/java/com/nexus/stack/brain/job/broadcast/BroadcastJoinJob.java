//package com.nexus.stack.brain.job.broadcast;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nexus.stack.brain.pojo.Order;
//import com.nexus.stack.brain.pojo.User;
//import com.nexus.stack.brain.pojo.WideOrder;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.api.common.functions.FilterFunction;
//import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.common.serialization.SimpleStringSchema;
//import org.apache.flink.api.common.state.MapStateDescriptor;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.connector.kafka.source.KafkaSource;
//import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
//import org.apache.flink.streaming.api.datastream.BroadcastStream;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
//import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
//import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
//import org.apache.flink.util.Collector;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.Serializable;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.Timestamp;
//import java.time.Duration;
//import java.util.Objects;
//import java.util.Properties;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component // 👈 变成 Spring Bean
//public class BroadcastJoinJob implements Serializable {
//
//    @Value("${kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Value("${app.datasource.clickhouse.jdbc-url}")
//    private String ckUrl;
//
//    @Value("${app.datasource.clickhouse.username}")
//    private String ckUser;
//
//    @Value("${app.datasource.clickhouse.password}")
//    private String ckPass;
//
//
//
//    public void run() throws Exception {
//
//        // 在 run 方法第一行打印出来看看！
//        log.info("DEBUG: bootstrapServers = " + bootstrapServers);
//
//        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
//        env.setParallelism(1);
//
//        env.disableOperatorChaining(); // 禁用算子链
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        // 订单流 Source
//        KafkaSource<String> orderSource = KafkaSource.<String>builder()
//                .setBootstrapServers(bootstrapServers)
//                .setTopics("order-topic")
//                .setGroupId("order-process-group")
//                .setStartingOffsets(OffsetsInitializer.latest())
//                .setValueOnlyDeserializer(new SimpleStringSchema())
//                .build();
//
//        // 用户流 Source
//        KafkaSource<String> userSource = KafkaSource.<String>builder()
//                .setBootstrapServers(bootstrapServers)
//                .setTopics("user-topic")
//                .setGroupId("user-sync-group-" + System.currentTimeMillis())
//                .setStartingOffsets(OffsetsInitializer.earliest())
//                .setValueOnlyDeserializer(new SimpleStringSchema())
//                .build();
//
//        MapStateDescriptor<Long, User> userStateDesc = new MapStateDescriptor<>(
//                "user-state", Long.class, User.class);
//
//        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(3));
//
//        // 订单流处理
//        DataStream<Order> orderStream = env
//                .fromSource(orderSource, watermarkStrategy, "order-source")
//                .map(new MapFunction<String, Order>() {
//                    @Override
//                    public Order map(String json) throws Exception {
//                        // 🚩 1. 过滤掉空字符串和只有空格的脏数据
//                        if (json == null || json.trim().isEmpty()) {
//                            log.error("⚠️ [跳过空数据] 发现一条空消息，已拦截");
//                            return null;
//                        }
//                        // 1. 第一时间打印所有进入订单流的数据
//                        log.info("🔍 [OrderStream 流量监控] 原始数据内容 -> " + json);
//
//                        try {
//                            JsonNode node = mapper.readTree(json);
//
//                            // 2. 这里的逻辑极其关键：手动检查字段
//                            if (node.has("level") && !node.has("amount")) {
//                                log.error("❌ [发现投毒数据] 这是 User 数据，却跑进了 Order 流！内容: " + json);
//                                return null; // 返回 null，由下方的 filter 过滤，不触发强转
//                            }
//
//                            // 3. 正常解析
//                            Order order = new Order();
//                            order.setUserId(node.path("userId").asLong(0L));
//                            order.setOrderId(node.path("orderId").asLong(0L));
//                            order.setAmount(node.path("amount").asDouble(0.0));
//                            order.setTs(node.path("ts").asLong(System.currentTimeMillis()));
//
//                            return order;
//
//                        } catch (Exception e) {
//                            log.error("💥 [解析异常] 数据格式无法处理: " + json + " | 错误: " + e.getMessage());
//                            return null;
//                        }
//                    }
//                })
//                .returns(Order.class) // 👈 关键点：告诉 Flink 这一路吐出来的是 Order
//                .filter(new FilterFunction<Order>() { // 🚩 这里的 Filter 也要写成匿名类，别用 Lambda
//                    @Override
//                    public boolean filter(Order value) throws Exception {
//                        return value != null;
//                    }
//                });
//
//        // 用户流处理
//        DataStream<User> userStream = env
//                .fromSource(userSource, watermarkStrategy, "user-source")
//                .map(new MapFunction<String, User>() {
//                    @Override
//                    public User map(String json) throws Exception {
//                        // 过滤掉空字符串和只有空格的脏数据
//                        if (json == null || json.trim().isEmpty()) {
//                            log.error("⚠️ [跳过空数据] 发现一条空消息，已拦截");
//                            return null;
//                        }
//                        try {
//                            JsonNode node = mapper.readTree(json);
//                            return User.builder()
//                                    .userId(node.path("userId").asLong())
//                                    .level(node.path("level").asText("NORMAL"))
//                                    .registerTime(node.path("registerTime").asLong(System.currentTimeMillis()))
//                                    .build();
//                        } catch (Exception e) {
//                            log.error("💥 [解析异常] 数据格式无法处理: " + json + " | 错误: " + e.getMessage());
//                            return null;
//                        }
//                    }
//                })
//                .returns(User.class) // 👈 关键点：告诉 Flink 这一路吐出来的是 User
//                .filter(new FilterFunction<User>() { // 🚩 这里的 Filter 也要写成匿名类，别用 Lambda
//                    @Override
//                    public boolean filter(User value) throws Exception {
//                        return value != null;
//                    }
//                });
//
//        BroadcastStream<User> userBroadcast = userStream.broadcast(userStateDesc);
//
//        SingleOutputStreamOperator<WideOrder> wideStream = orderStream
//                .keyBy(Order::getUserId)
//                .connect(userBroadcast)
//                .process(new KeyedBroadcastProcessFunction<Long, Order, User, WideOrder>() {
//                    @Override
//                    public void processElement(Order order, ReadOnlyContext ctx, Collector<WideOrder> out) throws Exception {
//                        User user = ctx.getBroadcastState(userStateDesc).get(order.getUserId());
//                        String level = (user != null) ? user.getLevel() : "NORMAL";
//                        WideOrder wide = new WideOrder();
//                        wide.setOrderId(order.getOrderId());
//                        wide.setUserId(order.getUserId());
//                        wide.setAmount(order.getAmount());
//                        wide.setLevel(level);
//                        out.collect(wide);
//                    }
//
//                    @Override
//                    public void processBroadcastElement(User user, Context ctx, Collector<WideOrder> out) throws Exception {
//                        ctx.getBroadcastState(userStateDesc).put(user.getUserId(), user);
//                    }
//                });
//
//        wideStream.addSink(new RichSinkFunction<WideOrder>() {
//            private Connection conn;
//            private PreparedStatement ps; // 👈 1. 提出来作为成员变量
//            private int count = 0; // 计数器
//            private long lastFlushTime = 0L; // 上次刷新时间
//            private final long INTERVAL_MS = 5000L; // 5秒门槛
//
//            private ScheduledExecutorService scheduler;
//
//            @Override
//            public void open(Configuration parameters) throws Exception {
//                Properties props = new Properties();
//                props.setProperty("user", ckUser);
//                props.setProperty("password", ckPass);
//                conn = DriverManager.getConnection(ckUrl, props);
//                conn.setAutoCommit(true); // 建议开启
//                // 👈 2. 在 open 中只初始化一次
//                ps = conn.prepareStatement("INSERT INTO dwd_order_wide VALUES (?, ?, ?, ?)");
//                lastFlushTime = System.currentTimeMillis(); // 初始化时间
//
//                // 哪怕没有新订单进来，5 秒一到也必须写进去，需要启用一个定时器
//                scheduler = Executors.newSingleThreadScheduledExecutor();
//                scheduler.scheduleAtFixedRate(() -> {
//                    synchronized (this) { // 注意多线程同步
//                        try {
//                            if (count > 0) {
//                                flush();
//                            }
//                        } catch (Exception e) {
//                            log.error("定时刷写失败", e);
//                        }
//                    }
//                }, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS);
//            }
//
//            @Override
//            public void invoke(WideOrder value, Context context) throws Exception {
//                // 👈 3. 这里只管赋值和执行，速度极快
//                ps.setLong(1, value.getOrderId());
//                ps.setLong(2, value.getUserId());
//                ps.setDouble(3, value.getAmount());
//                ps.setString(4, value.getLevel());
//
//                ps.addBatch(); // 👈 添加到批处理
//                count++;
//
//                // 对于 ClickHouse 这种极其讨厌单条插入（单条插入会产生大量小文件）的数据库，强烈建议配合 Batch 使用
//                // 判断是否满足【数量】或【时间】阈值
//                long currentTime = System.currentTimeMillis();
//                if (count >= 500 || (currentTime - lastFlushTime >= INTERVAL_MS && count > 0)) { // 👈 每 500 条刷写一次
//                    flush(); // 执行写入
//                }
//            }
//
//            // 抽离出来的刷新方法
//            private void flush() throws Exception {
//                ps.executeBatch();
//                log.info("🚀 [ClickHouse] 已写入批次，数据条数: " + count);
//                count = 0;
//                lastFlushTime = System.currentTimeMillis();
//            }
//
//            @Override
//            public void close() throws Exception {
//                // 👈 4. 必须在这里显式关闭，防止内存和连接泄露
//                if (ps != null) ps.close();
//                if (conn != null && !conn.isClosed()) conn.close();
//            }
//        });
//
//        wideStream.print();
//        env.execute("Wide Order Join Job");
//    }
//}
