# Dockerfile
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/flink:1.18-scala_2.12-java17

# 创建 lib 目录（如果不存在）
RUN mkdir -p /opt/flink/lib

# 复制所有需要的 JAR 到镜像内
COPY lib/*.jar /opt/flink/lib/

# 复制自定义 flink-conf.yaml
COPY flink-conf.yaml /opt/flink/conf/flink-conf.yaml

# 设置工作目录
WORKDIR /opt/flink

# 默认命令
CMD ["standalone-job"]