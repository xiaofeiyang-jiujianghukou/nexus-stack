//package com.nexus.stack.brain.job.joins.function;
//
//import com.nexus.stack.brain.pojo.Member;
//import com.nexus.stack.brain.pojo.WideOrder;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.api.common.state.MapStateDescriptor;
//import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
//import org.apache.flink.util.Collector;
//
//@Slf4j
//public class OrderMemberJoinFunction
//        extends KeyedBroadcastProcessFunction<Long, WideOrder, Member, WideOrder> {
//
//    private final MapStateDescriptor<Long, Member> stateDesc =
//            new MapStateDescriptor<>("member-state", Long.class, Member.class);
//
//    @Override
//    public void processElement(
//            WideOrder order,
//            ReadOnlyContext ctx,
//            Collector<WideOrder> out) throws Exception {
//
//        Member member = ctx.getBroadcastState(stateDesc)
//                .get(order.getUserId());
//
//        if (member == null) {
//            // 🚩 只有没找到的时候才打印警告
//            log.warn("⚠️ [维度缺失] 会员 ID: {} 在内存中不存在", order.getUserId());
//            order.setMember(false); // 默认值
//        } else {
//            // ✅ 找到了，正常赋值
//            log.info("🎯 [关联成功] 用户: {}, 状态: {}", order.getUserId(), member.isMember());
//            order.setMember(member.isMember());
//        }
//
//        out.collect(order);
//    }
//
//    @Override
//    public void processBroadcastElement(
//            Member member,
//            Context ctx,
//            Collector<WideOrder> out) throws Exception {
//        // 🚩 确保这里有打印，看 Kafka 数据到底进没进内存
//        log.info("📥 [写入内存] 收到维度更新 -> ID: {}, Status: {}", member.getUserId(), member.isMember());
//        ctx.getBroadcastState(stateDesc)
//                .put(member.getUserId(), member);
//    }
//}
