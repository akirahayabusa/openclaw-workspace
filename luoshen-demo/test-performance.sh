#!/bin/bash
# 洛神平台技能系统 - 性能测试脚本

echo "=== 洛神平台技能系统 - 性能测试 ==="
echo ""

BASE_URL="http://localhost:9090/api/admin"

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 统计
total_tests=0
passed_tests=0
failed_tests=0

# 测试函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=${5:-200}

    echo -n "[$name] 测试 $method $url ... "

    if [ -n "$data" ]; then
        response=$(curl -s -w "%{http_code}" -X $method "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "%{http_code}" -X $method "$url")
    fi

    total_tests=$((total_tests + 1))

    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ 通过${NC} (HTTP $response)"
        passed_tests=$((passed_tests + 1))
    else
        echo -e "${RED}✗ 失败${NC} (HTTP $response，期望 $expected_status)"
        failed_tests=$((failed_tests + 1))
    fi
}

echo ""
echo "=== 1. 基础连通性测试 ==="
test_api "健康检查" "GET" "$BASE_URL/skills?size=1" ""

echo ""
echo "=== 2. 技能查询测试 ==="
test_api "查询技能列表" "GET" "$BASE_URL/skills?page=0&size=20" ""
test_api "查询所有分类" "GET" "$BASE_URL/skills/categories" ""

echo ""
echo "=== 3. Agent 查询测试 ==="
test_api "查询 Agent 列表" "GET" "http://localhost:9090/api/admin/agents" ""

echo ""
echo "=== 4. 技能创建测试 ==="
echo "创建测试技能..."
test_api "创建纯文本技能" "POST" "$BASE_URL/skills/create" \
    '{"skillId":"perf-test-skill","name":"性能测试技能","description":"用于性能测试","content":"# 测试\n\n这是一个性能测试技能","category":"test","riskLevel":"low"}'

echo ""
echo "=== 5. 技能删除测试 ==="
echo "删除测试技能..."
test_api "删除测试技能" "DELETE" "$BASE_URL/skills/perf-test-skill" "" "204"

echo ""
echo "=== 测试总结 ==="
echo "总测试数: $total_tests"
echo -e "${GREEN}通过: $passed_tests${NC}"
echo -e "${RED}失败: $failed_tests${NC}"

if [ $failed_tests -eq 0 ]; then
    echo -e "\n${GREEN}✅ 所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${RED}❌ 有 $failed_tests 个测试失败${NC}"
    exit 1
fi
