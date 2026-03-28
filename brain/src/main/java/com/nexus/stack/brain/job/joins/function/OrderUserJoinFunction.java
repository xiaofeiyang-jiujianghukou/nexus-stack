//package com.nexus.stack.brain.job.joins.function;
//
//import com.nexus.stack.brain.pojo.Order;
//import com.nexus.stack.brain.pojo.User;
//import com.nexus.stack.brain.pojo.WideOrder;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.api.common.state.MapStateDescriptor;
//import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
//import org.apache.flink.util.Collector;
//
//@Slf4j
//public class OrderUserJoinFunction
//        extends KeyedBroadcastProcessFunction<Long,Order, User, WideOrder> {
//
//    private final MapStateDescriptor<Long, User> stateDesc =
//            new MapStateDescriptor<>("user-state", Long.class, User.class);
//
//    @Override
//    public void processElement(
//            Order order,
//            ReadOnlyContext ctx,
//            Collector<WideOrder> out) throws Exception {
//
//        User user = ctx.getBroadcastState(stateDesc).get(order.getUserId());
//
//        WideOrder wide = new WideOrder();
//        wide.setOrderId(order.getOrderId());
//        wide.setUserId(order.getUserId());
//        wide.setAmount(order.getAmount());
//
//        if (user == null) {
//            // 🚩 只有没找到的时候才打印警告
//            log.warn("⚠️ [维度缺失] 用户 ID: {} 在内存中不存在", order.getUserId());
//            wide.setLevel("NORMAL"); // 默认值
//        } else {
//            // ✅ 找到了，正常赋值
//            log.info("🎯 [关联成功] 用户: {}, 等级: {}", user.getUserId(), user.getLevel());
//            wide.setLevel(user.getLevel());
//        }
//
//        out.collect(wide);
//    }
//
//    @Override
//    public void processBroadcastElement(
//            User user,
//            Context ctx,
//            Collector<WideOrder> out) throws Exception {
//        log.info("📥 [写入内存] 收到维度更新 -> ID: {}, Status: {}", user.getUserId(), user.getLevel());
//        ctx.getBroadcastState(stateDesc)
//                .put(user.getUserId(), user);
//    }
//}