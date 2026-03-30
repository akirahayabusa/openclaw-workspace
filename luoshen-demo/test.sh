#!/bin/bash

# 洛神系统测试脚本
# 用于测试各个智能体的 REST API 接口

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 测试 Leader Agent
test_leader() {
    echo -e "${YELLOW}=== 测试 Leader Agent ===${NC}"
    
    echo "1. 查询所有设备状态"
    curl -s -X POST http://localhost:8080/api/leader/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询所有设备状态"}' \
        -G --data-urlencode "sessionId=test-session" | jq .
    
    echo ""
    echo "2. 查询物料库存"
    curl -s -X POST http://localhost:8080/api/leader/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询所有物料库存"}' \
        -G --data-urlencode "sessionId=test-session" | jq .
    
    echo ""
}

# 测试 Device Agent
test_device() {
    echo -e "${YELLOW}=== 测试 Device Agent ===${NC}"
    
    echo "1. 查询设备状态"
    curl -s -X POST http://localhost:8081/api/device/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询设备 device-001 的状态"}' | jq .
    
    echo ""
    echo "2. 查询所有设备"
    curl -s -X POST http://localhost:8081/api/device/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询所有设备状态"}' | jq .
    
    echo ""
    echo "3. 设备故障诊断"
    curl -s -X POST http://localhost:8081/api/device/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "诊断设备 device-004 的故障，现象是运行效率下降"}' | jq .
    
    echo ""
}

# 测试 Quality Agent
test_quality() {
    echo -e "${YELLOW}=== 测试 Quality Agent ===${NC}"
    
    echo "1. 执行质量检测"
    curl -s -X POST http://localhost:8082/api/quality/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "对产品 batch-001 执行质量检测"}' | jq .
    
    echo ""
    echo "2. 查询质量标准"
    curl -s -X POST http://localhost:8082/api/quality/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询所有质量标准"}' | jq .
    
    echo ""
    echo "3. 生成质量报告"
    curl -s -X POST http://localhost:8082/api/quality/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "生成本周质量报告"}' | jq .
    
    echo ""
}

# 测试 Material Agent
test_material() {
    echo -e "${YELLOW}=== 测试 Material Agent ===${NC}"
    
    echo "1. 查询物料库存"
    curl -s -X POST http://localhost:8083/api/material/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询物料 MAT-001 的库存信息"}' | jq .
    
    echo ""
    echo "2. 查询所有物料"
    curl -s -X POST http://localhost:8083/api/material/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "查询所有物料库存概览"}' | jq .
    
    echo ""
    echo "3. 申请采购"
    curl -s -X POST http://localhost:8083/api/material/chat \
        -H "Content-Type: application/json" \
        -d '{"message": "申请采购物料 MAT-005，数量 100，原因是库存不足"}' | jq .
    
    echo ""
}

# 主测试流程
echo -e "${GREEN}=== 洛神系统 API 测试 ===${NC}"
echo ""

# 检查 jq 是否安装
if ! command -v jq &> /dev/null; then
    echo "提示: jq 未安装，输出将不会格式化"
    echo "安装: sudo apt-get install jq"
fi

# 检查服务是否运行
check_service() {
    local port=$1
    local name=$2
    
    if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}$name: 运行中${NC}"
        return 0
    else
        echo -e "${YELLOW}$name: 未运行或端口 $port 不可访问${NC}"
        return 1
    fi
}

echo "检查服务状态..."
check_service 8080 "Leader Agent" || echo "  请先启动服务: ./start.sh start"
check_service 8081 "Device Agent" || true
check_service 8082 "Quality Agent" || true
check_service 8083 "Material Agent" || true

echo ""
read -p "是否继续测试? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 0
fi

# 执行测试
test_leader
test_device
test_quality
test_material

echo -e "${GREEN}=== 测试完成 ===${NC}"