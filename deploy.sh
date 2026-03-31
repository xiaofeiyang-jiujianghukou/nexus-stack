#!/bin/bash

# 清屏
clear

echo "========================================="
echo "       Nexus Stack 管理脚本"
echo "========================================="
echo ""

# 显示服务状态
echo "当前服务状态:"
echo ""
docker-compose ps 2>/dev/null || echo "  未运行"
echo ""

echo "请选择操作:"
echo ""
echo "  1) 启动所有服务（自动初始化目录+权限）"
echo "  2) 停止所有服务"
echo "  3) 重启指定服务"
echo "  4) 查看日志"
echo "  5) 查看服务状态"
echo "  6) 停止并删除容器（保留数据）"
echo "  7) 完全清理（删除所有数据）⚠️"
echo "  0) 退出"
echo ""
echo -n "请输入选项 [0-7]: "
read choice

case $choice in
    1)
        echo ""
        echo "初始化目录和权限..."

        # 创建 data 目录（如果不存在）
        mkdir -p data
        chown $(id -u):$(id -g) data

        # 创建目录（如果已存在会跳过）
        mkdir -p \
            data/mysql/{data,logs} \
            data/clickhouse/{data,logs} \
            data/redis/{data,logs} \
            data/zookeeper/{data,logs} \
            data/kafka/{data,logs} \
            data/flink/{checkpoints,savepoints}

        # 修复权限（核心）
        sudo chown -R 101:101 data/clickhouse 2>/dev/null
        sudo chmod -R 755 data/clickhouse 2>/dev/null

        sudo chown -R 999:999 data/mysql 2>/dev/null
        sudo chmod -R 755 data/mysql 2>/dev/null

        sudo chown -R 999:999 data/redis 2>/dev/null
        sudo chmod -R 755 data/mysql 2>/dev/null

        sudo chown -R 9999:9999 data/flink 2>/dev/null
        sudo chmod -R 755 data/flink 2>/dev/null

        sudo chown -R $(id -u):$(id -g) data/zookeeper 2>/dev/null
        sudo chown -R $(id -u):$(id -g) data/kafka 2>/dev/null


        sudo chmod +x clear_nexus_stack.sh monitor-nexus.sh check.sh clear_checkpoints.sh


        echo "✅ 权限设置完成"
        echo ""

        echo "启动所有服务..."
        docker-compose up -d

        echo ""
        echo "✅ 服务已启动"
        echo ""
        echo "访问地址:"
        IP=$(curl -s ifconfig.me 2>/dev/null || echo "localhost")
        echo "  前端大屏: http://${IP}:33000"
        echo "  后端 API: http://${IP}:38080"
        echo "  Flink:    http://${IP}:38081"
        ;;
    2)
        echo ""
        echo "停止所有服务..."
        docker-compose stop
        echo "✅ 服务已停止"
        ;;
    3)
        echo ""
        echo "运行中的服务:"
        docker-compose ps --services 2>/dev/null
        echo ""
        echo -n "请输入服务名称: "
        read service
        if [ -n "$service" ]; then
            docker-compose restart "$service"
            echo "✅ 服务 $service 已重启"
        fi
        ;;
    4)
        echo ""
        echo "服务列表:"
        docker-compose ps --services 2>/dev/null
        echo ""
        echo -n "请输入服务名称 (回车查看所有): "
        read service
        if [ -n "$service" ]; then
            docker-compose logs -f "$service"
        else
            docker-compose logs -f --tail=50
        fi
        ;;
    5)
        echo ""
        docker-compose ps
        ;;
    6)
        echo ""
        echo "将停止并删除容器，数据将保留"
        read -p "确认继续? (y/N): " confirm
        if [[ $confirm == [yY] ]]; then
            docker-compose down
            echo "✅ 容器已删除，数据已保留"
        fi
        ;;
    7)
        echo ""
        echo "⚠️ 危险操作！这将删除所有数据"
        echo -n "请输入 DELETE 确认: "
        read confirm
        if [ "$confirm" = "DELETE" ]; then
            docker-compose down -v
            echo "✅ 已清理所有容器和数据"
        fi
        ;;
    0)
        echo "再见！"
        exit 0
        ;;
    *)
        echo "无效选项"
        ;;
esac

echo ""
echo "操作完成"