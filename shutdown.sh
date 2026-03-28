#!/bin/bash

# 检测终端是否支持颜色
if [ -t 1 ] && [ -n "$TERM" ] && [ "$TERM" != "dumb" ]; then
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    RED='\033[0;31m'
    BLUE='\033[0;34m'
    CYAN='\033[0;36m'
    NC='\033[0m' # No Color
else
    GREEN=''
    YELLOW=''
    RED=''
    BLUE=''
    CYAN=''
    NC=''
fi

# 清屏函数（兼容不同系统）
clear_screen() {
    printf "\033[2J\033[1;1H" 2>/dev/null || clear
}

# 显示标题
show_header() {
    echo "${CYAN}=========================================${NC}"
    echo "${CYAN}       Nexus Stack 管理脚本${NC}"
    echo "${CYAN}=========================================${NC}"
    echo ""
}

# 显示服务状态
show_status() {
    echo "${YELLOW}📊 当前服务状态:${NC}"
    echo ""

    if command -v docker-compose &> /dev/null && docker-compose ps &> /dev/null; then
        docker-compose ps 2>/dev/null | grep -v "CONTAINER" | while read line; do
            # 移除颜色代码，只显示纯文本
            echo "$line" | sed 's/\x1b\[[0-9;]*m//g'
        done
    else
        echo "  未运行"
    fi
    echo ""
}

# 显示菜单（无颜色代码）
show_menu() {
    echo "请选择操作:"
    echo ""
    echo "  1) 停止所有服务（保留数据）"
    echo "  2) 停止并删除容器（保留数据）"
    echo "  3) 重启指定服务"
    echo "  4) 查看服务日志"
    echo "  5) 查看服务状态"
    echo "  6) 启动所有服务"
    echo "  7) 完全清理（删除所有数据）⚠️"
    echo "  0) 退出"
    echo ""
    echo -n "请输入选项 [0-7]: "
}

# 重启服务
restart_service() {
    echo ""
    echo "可重启的服务:"
    docker-compose ps --services 2>/dev/null | grep -v "^$" | nl -w2 -s') '
    echo ""
    echo -n "请输入服务名称或编号: "
    read input

    # 如果是数字，查找对应服务
    if [[ "$input" =~ ^[0-9]+$ ]]; then
        service=$(docker-compose ps --services 2>/dev/null | sed -n "${input}p")
    else
        service="$input"
    fi

    if [ -n "$service" ]; then
        echo ""
        echo "🔄 重启服务: $service"
        docker-compose restart "$service"
        echo "✅ 服务 $service 已重启"
    else
        echo "❌ 未找到服务"
    fi
}

# 查看日志
view_logs() {
    echo ""
    echo "可查看日志的服务:"
    docker-compose ps --services 2>/dev/null | grep -v "^$" | nl -w2 -s') '
    echo ""
    echo -n "请输入服务名称或编号: "
    read input

    if [[ "$input" =~ ^[0-9]+$ ]]; then
        service=$(docker-compose ps --services 2>/dev/null | sed -n "${input}p")
    else
        service="$input"
    fi

    if [ -n "$service" ]; then
        echo ""
        echo "查看 $service 日志 (按 Ctrl+C 退出)"
        sleep 2
        docker-compose logs -f "$service"
    else
        echo "❌ 未找到服务"
    fi
}

# 停止所有服务
stop_all() {
    echo ""
    echo "🛑 停止所有服务..."
    docker-compose stop
    echo "✅ 所有服务已停止"
    echo ""
    echo "提示: 使用 ./deploy.sh 重新启动"
}

# 停止并删除容器
down_containers() {
    echo ""
    echo "⚠️  将停止并删除所有容器，但数据卷会保留"
    echo "注意: 容器删除后，需要重新运行 ./deploy.sh 才能启动"
    echo ""
    read -p "确认继续? (y/N): " confirm
    if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
        echo ""
        echo "🗑️  停止并删除容器..."
        docker-compose down
        echo "✅ 容器已删除，数据已保留"
        echo ""
        echo "查看数据卷: docker volume ls | grep nexus-stack"
        echo "重新启动: ./deploy.sh"
    else
        echo "操作已取消"
    fi
}

# 完全清理
clean_all() {
    echo ""
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║  ⚠️⚠️⚠️ 危险操作！这将删除所有数据！ ⚠️⚠️⚠️               ║"
    echo "║                                                          ║"
    echo "║  以下数据将被永久删除:                                    ║"
    echo "║    • MySQL 数据库                                        ║"
    echo "║    • ClickHouse 数据库                                   ║"
    echo "║    • Redis 缓存                                          ║"
    echo "║    • Kafka 消息                                          ║"
    echo "║    • Zookeeper 数据                                      ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo ""
    echo -n "请输入 DELETE 确认删除所有数据: "
    read confirm
    if [ "$confirm" = "DELETE" ]; then
        echo ""
        echo "🗑️  正在删除所有容器和数据..."
        docker-compose down -v
        echo "✅ 已清理所有容器和数据"
        echo ""
        echo "提示: 运行 ./deploy.sh 重新初始化"
    else
        echo "操作已取消"
    fi
}

# 启动所有服务
start_all() {
    echo ""
    echo "🚀 启动所有服务..."
    if [ -f "./deploy.sh" ]; then
        ./deploy.sh
    else
        docker-compose up -d
        echo "✅ 服务已启动"
    fi
}

# 主循环
main() {
    while true; do
        clear_screen
        show_header
        show_status
        show_menu
        read choice

        case $choice in
            1)
                stop_all
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            2)
                down_containers
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            3)
                restart_service
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            4)
                view_logs
                ;;
            5)
                clear_screen
                show_header
                show_status
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            6)
                start_all
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            7)
                clean_all
                echo ""
                read -p "按 Enter 键继续..."
                ;;
            0)
                echo ""
                echo "再见！"
                exit 0
                ;;
            *)
                echo "无效选项，请重新选择"
                sleep 1
                ;;
        esac
    done
}

# 检查 docker-compose 文件是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 未找到 docker-compose.yml 文件"
    exit 1
fi

# 检查 docker-compose 命令是否存在
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose 命令未找到"
    exit 1
fi

# 运行主程序
main