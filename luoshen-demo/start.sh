#!/bin/bash

# 洛神系统快速启动脚本
# 用于一键启动所有智能体服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目目录
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 检查 API Key
check_api_key() {
    if [ -z "$DASHSCOPE_API_KEY" ] && [ -z "$OPENAI_API_KEY" ]; then
        echo -e "${RED}错误: 未设置 API Key${NC}"
        echo "请设置环境变量:"
        echo "  export DASHSCOPE_API_KEY=your-api-key"
        echo "或"
        echo "  export OPENAI_API_KEY=your-api-key"
        exit 1
    fi
}

# 编译项目
build_project() {
    echo -e "${YELLOW}正在编译项目...${NC}"
    cd "$PROJECT_DIR"
    mvn clean install -DskipTests
    echo -e "${GREEN}编译完成${NC}"
}

# 启动服务
start_service() {
    local module=$1
    local port=$2
    local name=$3
    
    echo -e "${YELLOW}启动 $name (端口 $port)...${NC}"
    
    java -jar "$PROJECT_DIR/$module/target/$module-1.0.0-SNAPSHOT.jar" \
        --server.port=$port > "$PROJECT_DIR/logs/$name.log" 2>&1 &
    
    echo $! > "$PROJECT_DIR/logs/$name.pid"
    echo -e "${GREEN}$name 已启动 (PID: $(cat $PROJECT_DIR/logs/$name.pid))${NC}"
}

# 停止服务
stop_service() {
    local name=$1
    
    if [ -f "$PROJECT_DIR/logs/$name.pid" ]; then
        local pid=$(cat "$PROJECT_DIR/logs/$name.pid")
        if [ -n "$pid" ]; then
            echo -e "${YELLOW}停止 $name (PID: $pid)...${NC}"
            kill $pid 2>/dev/null || true
            rm "$PROJECT_DIR/logs/$name.pid"
            echo -e "${GREEN}$name 已停止${NC}"
        fi
    fi
}

# 创建日志目录
mkdir -p "$PROJECT_DIR/logs"

# 主命令
case "$1" in
    build)
        build_project
        ;;
    
    start)
        check_api_key
        build_project
        
        echo -e "${GREEN}=== 启动洛神系统 ===${NC}"
        
        start_service "luoshen-leader-agent" 8080 "Leader Agent"
        sleep 2
        start_service "luoshen-device-agent" 8081 "Device Agent"
        sleep 2
        start_service "luoshen-quality-agent" 8082 "Quality Agent"
        sleep 2
        start_service "luoshen-material-agent" 8083 "Material Agent"
        
        echo -e "${GREEN}=== 所有服务已启动 ===${NC}"
        echo ""
        echo "服务地址:"
        echo "  - Leader Agent:  http://localhost:8080"
        echo "  - Device Agent:  http://localhost:8081"
        echo "  - Quality Agent: http://localhost:8082"
        echo "  - Material Agent: http://localhost:8083"
        echo ""
        echo "测试命令:"
        echo "  curl -X POST http://localhost:8080/api/leader/chat -H 'Content-Type: application/json' -d '{\"message\": \"查询所有设备状态\"}'"
        ;;
    
    stop)
        echo -e "${YELLOW}=== 停止洛神系统 ===${NC}"
        stop_service "Leader Agent"
        stop_service "Device Agent"
        stop_service "Quality Agent"
        stop_service "Material Agent"
        echo -e "${GREEN}=== 所有服务已停止 ===${NC}"
        ;;
    
    status)
        echo -e "${GREEN}=== 服务状态 ===${NC}"
        for name in "Leader Agent" "Device Agent" "Quality Agent" "Material Agent"; do
            if [ -f "$PROJECT_DIR/logs/$name.pid" ]; then
                local pid=$(cat "$PROJECT_DIR/logs/$name.pid")
                if ps -p $pid > /dev/null 2>&1; then
                    echo -e "${GREEN}$name: 运行中 (PID: $pid)${NC}"
                else
                    echo -e "${RED}$name: 已停止${NC}"
                fi
            else
                echo -e "${YELLOW}$name: 未启动${NC}"
            fi
        done
        ;;
    
    *)
        echo "用法: $0 {build|start|stop|status}"
        echo ""
        echo "命令说明:"
        echo "  build  - 编译项目"
        echo "  start  - 启动所有服务"
        echo "  stop   - 停止所有服务"
        echo "  status - 查看服务状态"
        exit 1
        ;;
esac