# Nexus Stack 实时数仓 + 数据服务 + 可视化大屏一体化平台

## 📊 项目概述

Nexus Stack 是一个完整的实时数据仓库解决方案，集成了**实时数据采集、流式计算、数据服务、可视化大屏**四大模块。通过 Flink SQL + CDC 技术，实现 MySQL 业务数据到 ClickHouse 数仓的毫秒级同步，并对外提供实时数据服务接口，最终以可视化大屏的形式呈现业务指标。

### 核心能力

- **实时同步**：MySQL 数据变更通过 Flink CDC 实时同步到 ClickHouse
- **流式计算**：支持复杂事件处理、窗口聚合、维表关联
- **数据服务**：定时查询 ClickHouse 并缓存到 Redis，提供高性能 API
- **可视化大屏**：实时展示 GMV、用户数、会员数等核心指标

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           数据采集层                                    │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                         │
│  │  MySQL   │───▶│ Flink CDC│───▶│  Kafka   │                         │
│  │ 业务数据 │    │  采集    │    │  消息队列 │                         │
│  └──────────┘    └──────────┘    └────┬─────┘                         │
│                                        │                               │
├────────────────────────────────────────┼───────────────────────────────┤
│                           计算存储层    │                               │
│                                        ▼                               │
│  ┌──────────────────────────────────────────────────────────────┐     │
│  │                    Flink Stream Processing                    │     │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐             │     │
│  │  │ 订单宽表   │  │   GMV聚合  │  │  用户画像  │             │     │
│  │  │ (DWD层)   │  │  (DWS层)  │  │  (DWS层)  │             │     │
│  │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘             │     │
│  └────────┼────────────────┼────────────────┼────────────────────┘     │
│           ▼                ▼                ▼                          │
│  ┌──────────────────────────────────────────────────────────────┐     │
│  │                      ClickHouse 数仓                          │     │
│  │  ┌────────────────┐  ┌────────────────┐                     │     │
│  │  │ dwd_order_wide │  │  ads_gmv_1m    │                     │     │
│  │  │   订单宽表     │  │   GMV聚合表    │                     │     │
│  │  └────────────────┘  └────────────────┘                     │     │
│  └──────────────────────────────────────────────────────────────┘     │
│                                                                       │
├───────────────────────────────────────────────────────────────────────┤
│                           数据服务层                                  │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐     │
│  │                    Spring Boot 数据服务                       │     │
│  │                                                              │     │
│  │  ┌────────────┐    ┌────────────┐    ┌────────────┐        │     │
│  │  │ 定时任务   │───▶│  ClickHouse│───▶│   Redis    │        │     │
│  │  │ (每5秒)   │    │   查询     │    │   缓存     │        │     │
│  │  └────────────┘    └────────────┘    └─────┬──────┘        │     │
│  │                                              │               │     │
│  │  ┌──────────────────────────────────────────┴──────────────┐│     │
│  │  │                    RESTful API                          ││     │
│  │  │  GET /api/dashboard/gmv    GET /api/dashboard/trend     ││     │
│  │  │  GET /api/dashboard/users  GET /api/dashboard/members   ││     │
│  │  └─────────────────────────────────────────────────────────┘│     │
│  └──────────────────────────────────────────────────────────────┘     │
│                                                                       │
├───────────────────────────────────────────────────────────────────────┤
│                           可视化层                                    │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐     │
│  │              React + Ant Design 可视化大屏                    │     │
│  │                                                              │     │
│  │  ┌────────────────────────────────────────────────────────┐ │     │
│  │  │                    GMV 实时大屏                        │ │     │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │ │     │
│  │  │  │当前 GMV  │ │累计 GMV  │ │ 用户数   │ │ 会员数   │ │ │     │
│  │  │  │1,562,670│ │1,562,670│ │  151人   │ │  67人    │ │ │     │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ │ │     │
│  │  │                                                      │ │     │
│  │  │  ┌─────────────────────┐ ┌─────────────────────┐    │ │     │
│  │  │  │     GMV 趋势图      │ │     GMV 分布图      │    │ │     │
│  │  │  │     (折线图)        │ │     (饼图/柱状图)   │    │ │     │
│  │  │  │                    │ │                    │    │ │     │
│  │  │  │  时间: 02:00       │ │  占比: 45%         │    │ │     │
│  │  │  │  GMV: ¥101,029.59 │ │                    │    │ │     │
│  │  │  └─────────────────────┘ └─────────────────────┘    │ │     │
│  │  └────────────────────────────────────────────────────────┘ │     │
│  └──────────────────────────────────────────────────────────────┘     │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 📁 项目结构

```
nexus-stack/
├── docker-compose.yml          # Docker 编排文件
├── deploy.sh                   # 一键部署脚本
├── shutdown.sh                 # 服务管理脚本
│
├── brain/                      # Spring Boot 后端服务
│   ├── src/
│   │   ├── main/java/          # Java 源码
│   │   └── resources/          # 配置文件
│   └── Dockerfile              # 后端镜像构建文件
│
├── brain-view/                 # React 前端大屏
│   ├── src/
│   │   ├── components/         # React 组件
│   │   ├── pages/              # 页面
│   │   └── services/           # API 服务
│   └── Dockerfile              # 前端镜像构建文件
│
├── flink/                      # Flink 作业配置
│   └── sql/                    # Flink SQL 脚本
│       ├── cdc/                # CDC 源表定义
│       ├── sink/               # 结果表定义
│       └── join/               # JOIN 逻辑
│
├── mysql/                      # MySQL 数据目录
│   ├── data/                   # 数据库文件
│   ├── init/                   # 初始化 SQL 脚本
│   └── logs/                   # 日志文件
│
├── clickhouse/                 # ClickHouse 数据目录
│   ├── data/                   # 数据库文件
│   ├── init/                   # 初始化 SQL 脚本
│   └── logs/                   # 日志文件
│
├── redis/                      # Redis 数据目录
│   ├── data/                   # 持久化文件
│   └── logs/                   # 日志文件
│
├── kafka/                      # Kafka 数据目录
│   └── data/                   # 消息数据
│
└── zookeeper/                  # Zookeeper 数据目录
    ├── data/                   # 数据文件
    └── logs/                   # 日志文件
```

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 容器编排 |
| Git | 2.0+ | 代码管理 |
| 内存 | 8GB+ | 推荐 16GB |

### 一键部署

```bash
# 1. 克隆项目
git clone https://github.com/your-username/nexus-stack.git
cd nexus-stack

# 2. 赋予脚本执行权限
chmod +x deploy.sh shutdown.sh

# 3. 一键启动所有服务
./deploy.sh
```

启动过程会自动完成以下工作：

- ✅ 创建所有数据目录
- ✅ 初始化数据库表结构
- ✅ 启动 MySQL、ClickHouse、Redis、Kafka、Zookeeper
- ✅ 启动 Flink 集群和 SQL Gateway
- ✅ 提交 Flink CDC 作业
- ✅ 启动后端数据服务
- ✅ 启动前端可视化大屏

### 服务管理

```bash
# 查看所有服务状态
./shutdown.sh status

# 停止所有服务（保留数据）
./shutdown.sh stop

# 重启某个服务
./shutdown.sh restart mysql

# 完全清理（删除所有数据）
./shutdown.sh clean
```

---

## 🖥️ 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| **可视化大屏** | http://localhost:33000 | React 前端页面 |
| **后端 API** | http://localhost:38080 | Spring Boot 数据接口 |
| **MySQL** | localhost:33306 | 业务数据库（root/123456） |
| **ClickHouse** | localhost:38123 | 数据仓库（default/123456） |
| **Redis** | localhost:36379 | 缓存服务（密码:123456） |
| **Kafka** | localhost:9092 | 消息队列 |
| **Flink Web UI** | http://localhost:38081 | Flink 作业监控 |
| **Flink SQL Gateway** | http://localhost:38083 | SQL 提交入口 |

---

## 📊 实时大屏展示

![GMV 数据大屏](./image.png)

### 核心指标

| 指标 | 说明 | 数据来源 |
|------|------|----------|
| **当前 GMV** | 最近10秒内订单总额 | Flink 滚动窗口聚合 |
| **累计 GMV** | 历史订单总额 | ClickHouse 聚合查询 |
| **用户数** | 活跃用户数量 | MySQL 用户表 |
| **会员数** | 会员用户数量 | MySQL 会员表 |
| **GMV 趋势图** | 按时间维度的 GMV 变化 | ClickHouse 时序数据 |
| **GMV 分布图** | 按用户等级/地区的分布 | ClickHouse 分组聚合 |

### 数据刷新机制

1. **Flink 实时计算**：每10秒计算一次 GMV，写入 ClickHouse
2. **后端定时任务**：每5秒查询 ClickHouse，写入 Redis 缓存
3. **前端轮询**：每3秒请求后端 API，实时更新大屏数据

---

## 🔧 核心数据流

### 1. MySQL CDC 实时同步

```sql
-- Flink CDC 源表定义
CREATE TABLE mysql_orders (
    order_id BIGINT,
    user_id BIGINT,
    amount DECIMAL(10,2),
    ts BIGINT,
    row_time AS TO_TIMESTAMP_LTZ(ts, 3),
    WATERMARK FOR row_time AS row_time - INTERVAL '5' SECOND,
    PRIMARY KEY (order_id) NOT ENFORCED
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql',
    'port' = '3306',
    'username' = 'root',
    'password' = '123456',
    'database-name' = 'brain_db',
    'table-name' = 'orders'
);
```

### 2. 订单宽表 JOIN

```sql
INSERT INTO ck_wide_orders
SELECT
    o.order_id,
    o.user_id,
    o.amount,
    COALESCE(u.level, 'NORMAL') AS level,
    COALESCE(m.is_member, 0) AS is_member,
    o.ts
FROM mysql_orders o
LEFT JOIN mysql_users u ON o.user_id = u.user_id
LEFT JOIN mysql_members m ON o.user_id = m.user_id;
```

### 3. GMV 滚动窗口聚合

```sql
INSERT INTO ads_gmv_1m
SELECT
    TUMBLE_START(row_time, INTERVAL '10' SECOND) AS stat_time,
    CAST(ROUND(SUM(amount), 2) AS DECIMAL(10, 2)) AS gmv
FROM mysql_orders
GROUP BY TUMBLE(row_time, INTERVAL '10' SECOND);
```

### 4. 后端数据服务接口

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/gmv")
    public DashboardData getGmv() {
        // 优先从 Redis 获取缓存
        DashboardData cached = redisTemplate.opsForValue().get("dashboard:gmv");
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询 ClickHouse
        DashboardData data = clickHouseService.queryGmv();
        redisTemplate.opsForValue().set("dashboard:gmv", data, 3, TimeUnit.SECONDS);
        return data;
    }
}
```

---

## 📈 性能指标

| 指标 | 数值 | 说明 |
|------|------|------|
| **CDC 延迟** | < 1 秒 | MySQL → Flink 延迟 |
| **窗口聚合延迟** | 10 秒 | 滚动窗口大小 |
| **端到端延迟** | < 15 秒 | 数据产生 → 大屏展示 |
| **吞吐量** | 10,000+ 条/秒 | Flink 处理能力 |
| **API 响应时间** | < 50ms | 带 Redis 缓存 |
| **大屏刷新频率** | 3 秒 | 前端轮询间隔 |

---

## 🐳 Docker 部署架构

### 容器列表

| 容器 | 镜像 | 端口映射 | 资源限制 |
|------|------|----------|----------|
| nexus-stack-mysql | mysql:8.0 | 33306:3306 | 1核/2GB |
| nexus-stack-clickhouse | clickhouse-server:23 | 38123:8123 | 2核/4GB |
| nexus-stack-redis | redis:7 | 36379:6379 | 512MB |
| nexus-stack-zookeeper | cp-zookeeper:7.5.0 | - | 512MB |
| nexus-stack-kafka | cp-kafka:7.5.0 | 9092:9092 | 1核/2GB |
| nexus-stack-jobmanager | flink:1.18 | 38081:8081 | 1核/2GB |
| nexus-stack-taskmanager | flink:1.18 | - | 2核/4GB |
| nexus-stack-sql-gateway | flink:1.18 | 38083:8083 | 1核/1GB |
| nexus-stack-brain | brain:latest | 38080:8080 | 1核/1GB |
| nexus-stack-brain-view | brain-view:latest | 33000:80 | 256MB |

### 网络架构

```
┌─────────────────────────────────────────────────────────────┐
│                     nexus-stack-net                         │
│  (bridge network, 172.17.0.0/16)                           │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  mysql   │  │clickhouse│  │  redis   │  │  kafka   │   │
│  │  :3306   │  │  :8123   │  │  :6379   │  │  :9092   │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │             │             │           │
│       └─────────────┼─────────────┼─────────────┘           │
│                     ▼             ▼                         │
│              ┌────────────┐ ┌────────────┐                  │
│              │  jobmanager│ │taskmanager │                  │
│              │  :8081    │ │            │                  │
│              └────────────┘ └────────────┘                  │
│                     │             │                         │
│                     ▼             ▼                         │
│              ┌────────────┐ ┌────────────┐                  │
│              │   brain    │ │ brain-view │                  │
│              │  :8080    │ │   :80      │                  │
│              └────────────┘ └────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 故障排查

### 常见问题

#### 1. Flink 作业无法启动

```bash
# 查看 Flink JobManager 日志
docker logs nexus-stack-jobmanager

# 查看 Flink TaskManager 日志
docker logs nexus-stack-taskmanager

# 检查 MySQL CDC 连接
docker exec nexus-stack-mysql mysql -uroot -p123456 -e "SHOW PROCESSLIST"
```

#### 2. ClickHouse 连接失败

```bash
# 测试 ClickHouse 连接
curl -u default:123456 "http://localhost:38123/?query=SELECT 1"

# 查看 ClickHouse 日志
docker logs nexus-stack-clickhouse
```

#### 3. 大屏无数据

```bash
# 查看后端 API 响应
curl http://localhost:38080/api/dashboard/gmv

# 查看 Redis 缓存
docker exec nexus-stack-redis redis-cli -a 123456 keys "*"

# 查看 ClickHouse 数据
docker exec nexus-stack-clickhouse clickhouse-client --query "SELECT * FROM dwd_order_wide LIMIT 10"
```

---

## 📝 配置说明

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | 123456 | MySQL 密码 |
| `CLICKHOUSE_PASSWORD` | 123456 | ClickHouse 密码 |
| `REDIS_PASSWORD` | 123456 | Redis 密码 |
| `FLINK_REST_PORT` | 38081 | Flink Web UI 端口 |
| `SQL_GATEWAY_PORT` | 38083 | SQL Gateway 端口 |

### 自定义配置

修改 `docker-compose.yml` 中的环境变量即可自定义配置。

---

## 👥 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

## 📞 联系方式

- 项目主页: https://github.com/your-username/nexus-stack
- 问题反馈: https://github.com/your-username/nexus-stack/issues
- 邮箱: your-email@example.com

---

**感谢使用 Nexus Stack！** 🎉

如果您觉得这个项目有帮助，欢迎 Star ⭐️ 支持！