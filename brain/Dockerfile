# 构建阶段使用官方 Maven + JDK 镜像
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# COPY 源码和 settings.xml
COPY pom.xml .
COPY src/ src/
COPY settings.xml /tmp/settings.xml

# 使用 Maven 缓存加速依赖
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /tmp/settings.xml clean package -DskipTests -Dmaven.compiler.release=25

FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
            "--add-opens", "java.base/java.util=ALL-UNNAMED", \
            "-XX:+UseZGC", "-XX:+ZGenerational", \
            "-jar", "app.jar"]