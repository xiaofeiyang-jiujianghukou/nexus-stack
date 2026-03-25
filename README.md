# **_“实时数仓 + 数据服务 + 可视化大屏”一体化项目_**

---

# 一、项目目标（先把边界定义清楚）

核心目标：

> 将 **用户 / 订单 / 会员数据 → 实时汇聚 → 数仓建模 → 实时指标计算 → 大屏展示**

关键能力：

* 实时数据接入（订单、用户行为、会员变更）
* 数据分层（ODS → DWD → DWS → ADS）
* 实时指标计算（GMV、订单数、转化率、会员增长等）
* 秒级/亚秒级大屏刷新
* 支持后续 BI / 分析扩展

---

# 二、整体架构设计（核心）

## 1️⃣ 架构分层

```
业务系统（订单/用户/会员）
        ↓
数据采集层（CDC / MQ）
        ↓
实时计算层（Flink）
        ↓
实时数仓（ODS → DWD → DWS → ADS）
        ↓
数据服务层（SpringBoot）
        ↓
大屏展示（前端）
```

---

# 三、技术选型

## 1️⃣ 数据采集层

| 场景    | 技术                        |
| ----- | ------------------------- |
| 数据库变更 | **Debezium + Kafka**（CDC） |
| 业务埋点  | Kafka                     |
| 日志采集  | Filebeat（可选）              |

👉 推荐：

* MySQL → Debezium → Kafka
* 减少侵入业务系统（非常关键）

---

## 2️⃣ 消息队列

* **Apache Kafka**

作用：

* 解耦系统
* 承载实时数据流
* 支持高吞吐（订单场景必须）

---

## 3️⃣ 实时计算引擎（核心）

* **Apache Flink**

用途：

* 实时 ETL（ODS → DWD）
* 宽表构建（用户 + 订单 + 会员）
* 实时指标计算（GMV、UV、留存）

关键能力：

* 事件时间（Event Time）
* 状态管理（State）
* 精确一次（Exactly Once）

---

## 4️⃣ 实时数仓存储

### 明细层（ODS / DWD）

* **Apache Hudi** 或 Iceberg

👉 优点：

* 支持流批一体
* 支持更新（订单状态变化）

---

### 聚合层（DWS / ADS）

* **ClickHouse**

👉 用于：

* 大屏查询（毫秒级）
* 高并发聚合

---

## 5️⃣ 数据服务层（你要求的重点）

用：

* **Spring Boot 4.0.4**
* JDK 25

职责：

* 聚合查询接口
* 提供 REST API 给大屏
* 做缓存 + 限流

建议组件：

| 功能  | 技术                 |
| --- | ------------------ |
| ORM | MyBatis Plus       |
| 缓存  | Redis              |
| API | Spring WebFlux（推荐） |
| 限流  | Resilience4j       |

---

## 6️⃣ 大屏展示

* Vue3 + ECharts / AntV

---

# 四、数仓建模设计（重点）

## 1️⃣ 分层模型

### ODS（原始层）

* ods_order
* ods_user
* ods_member

👉 保持和业务库一致（Kafka原样入湖）

---

### DWD（明细层）

👉 做清洗 + 标准化

* dwd_order_detail
* dwd_user_info
* dwd_member_info

---

### DWS（汇总层）

👉 按主题建模：

* dws_trade_day
* dws_user_active
* dws_member_growth

---

### ADS（应用层）

👉 给大屏用

* ads_gmv_1m（每分钟GMV）
* ads_order_count
* ads_user_conversion

---

# 五、核心实时指标设计

## 1️⃣ GMV（实时）

Flink 计算：

```sql
SUM(order_amount)
GROUP BY TUMBLE(event_time, INTERVAL '1' MINUTE)
```

---

## 2️⃣ 转化率

```
支付用户数 / 访问用户数
```

---

## 3️⃣ 会员增长

```
新增会员数（按时间窗口）
```

---

# 六、数据链路（完整流转）

```
MySQL（订单库）
   ↓（CDC）
Debezium
   ↓
Kafka（topic: order）
   ↓
Flink（清洗 + 关联）
   ↓
Hudi（DWD）
   ↓
Flink（二次计算）
   ↓
ClickHouse（ADS）
   ↓
SpringBoot API
   ↓
大屏展示
```

---

# 七、SpringBoot 服务设计（核心）

## 1️⃣ 模块划分

```
data-service
 ├── controller
 ├── service
 ├── repository
 ├── dto
 ├── cache
```

---

## 2️⃣ 核心接口示例

```java
@GetMapping("/dashboard/gmv")
public GmvRes getGmvTrend() {
    return gmvService.queryLast1Hour();
}
```

---

## 3️⃣ 查询策略

👉 不要直接查明细表：

* 只查 ADS 表（ClickHouse）
* Redis 做热点缓存（1~5秒）

---

# 八、性能设计（必须考虑）

## 1️⃣ 数据层

* Kafka 分区 ≥ 订单峰值 TPS / 1000
* Flink 并行度 = Kafka 分区数

---

## 2️⃣ 查询层

* ClickHouse 使用：

    * 分区（按天）
    * 主键（时间 + 业务ID）

---

## 3️⃣ 缓存策略

* 热点接口：

    * 本地缓存（Caffeine） + Redis
    * TTL：3~5秒

---

# 九、扩展能力（加分项）

## 1️⃣ 离线数仓（补充）

* Hive / Spark（做T+1分析）

---

## 2️⃣ 数据治理

* 数据血缘
* 数据质量监控

---

## 3️⃣ 实时 OLAP

* 引入 **Apache Doris（可替代 ClickHouse）

---

# 十、项目亮点（你面试必须说的）

直接帮你总结一段“面试话术”：

> 本项目构建了一套基于 Flink + Kafka 的实时数仓体系，通过 CDC 实现业务数据无侵入采集，采用分层建模（ODS/DWD/DWS/ADS）保证数据可复用性。在实时计算方面，通过 Flink 进行多流关联和窗口聚合，实现 GMV、转化率等核心指标的秒级计算。数据服务层基于 SpringBoot 4 + JDK25 构建，通过 ClickHouse 提供高性能查询，并结合 Redis 缓存实现毫秒级响应，最终支撑大屏实时展示。

---

# 十一、给你的关键建议（非常重要）

你这个项目容易踩的坑：

### ❌ 错误方向

* 直接用 MySQL 做大屏查询（必死）
* 没有分层（后期不可维护）
* Flink 只做简单消费（浪费）

---

### ✅ 正确思路

* **Kafka + Flink 是核心**
* **ClickHouse 是查询核心**
* **SpringBoot 只是数据出口**

---

# 如果你要继续深入

我可以帮你往下拆：

1. Flink Job 代码级设计（含 join + state）
2. ClickHouse 表结构（可直接建表）
3. SpringBoot 项目骨架（DDD结构）
4. Docker Compose 一键部署整套架构
5. 面试问答（吊打大厂那种）

直接说你下一步要哪一块。
