#!/bin/bash
# 技术前沿每日推送 - 邮件版
# 每天 9:00 执行

DATE=$(date "+%Y-%m-%d %A")
WORKSPACE="/root/.openclaw/workspace"
LOG_DIR="$WORKSPACE/logs"
mkdir -p "$LOG_DIR"

TMP_FILE=$(mktemp)
EMAIL_FILE=$(mktemp)

echo "========================================" >> "$TMP_FILE"
echo "🚀 技术前沿日报 - $DATE" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"
echo "" >> "$TMP_FILE"

# ============ 1. GitHub 热门项目 ============
echo "📦 GitHub 今日热门" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

GITHUB_DATA=$(curl -s "https://api.github.com/search/repositories?q=stars:>1000&sort=stars&order=desc&per_page=5" 2>/dev/null)

if [ -n "$GITHUB_DATA" ]; then
    echo "$GITHUB_DATA" | jq -r '.items[] | "• \(.full_name) (\(.stargazers_count)⭐)\n  \(.description // "无描述")\n  🔗 \(.html_url)\n"' 2>/dev/null >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"

# ============ 2. Hacker News Top 10 ============
echo "🔥 Hacker News 热门" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

HN_DATA=$(curl -s "https://hacker-news.firebaseio.com/v0/topstories.json" 2>/dev/null)

if [ -n "$HN_DATA" ]; then
    # 获取前10个故事ID
    STORY_IDS=$(echo "$HN_DATA" | jq -r '.[:10][]' 2>/dev/null)
    
    for ID in $STORY_IDS; do
        STORY=$(curl -s "https://hacker-news.firebaseio.com/v0/item/$ID.json" 2>/dev/null)
        TITLE=$(echo "$STORY" | jq -r '.title // "无标题"' 2>/dev/null)
        URL=$(echo "$STORY" | jq -r '.url // "https://news.ycombinator.com/item?id=$ID"' 2>/dev/null)
        SCORE=$(echo "$STORY" | jq -r '.score // 0' 2>/dev/null)
        echo "• $TITLE ($SCORE👍)" >> "$TMP_FILE"
        echo "  🔗 $URL" >> "$TMP_FILE"
    done
fi

echo "" >> "$TMP_FILE"

# ============ 3. AI 动态 ============
echo "🤖 AI 领域动态" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

# 使用 GitHub API 搜索 AI 相关热门项目
AI_DATA=$(curl -s "https://api.github.com/search/repositories?q=topic:ai+topic:llm+topic:machine-learning&sort=updated&per_page=5" 2>/dev/null)

if [ -n "$AI_DATA" ]; then
    echo "$AI_DATA" | jq -r '.items[] | "• \(.full_name)\n  \(.description // "无描述")\n  🔗 \(.html_url)\n"' 2>/dev/null >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"

# ============ 4. Java/Spring 生态 ============
echo "☕ Java/Spring 生态" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

JAVA_DATA=$(curl -s "https://api.github.com/search/repositories?q=topic:java+topic:spring&sort=updated&per_page=5" 2>/dev/null)

if [ -n "$JAVA_DATA" ]; then
    echo "$JAVA_DATA" | jq -r '.items[] | "• \(.full_name)\n  \(.description // "无描述")\n  🔗 \(.html_url)\n"' 2>/dev/null >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"

# ============ 5. 技术小结 ============
echo "📝 今日小结" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"
echo "以上是今日技术前沿动态，建议重点关注：" >> "$TMP_FILE"
echo "1. GitHub 热门项目中的新兴技术趋势" >> "$TMP_FILE"
echo "2. Hacker News 上的技术讨论热点" >> "$TMP_FILE"
echo "3. AI 领域的最新进展和应用" >> "$TMP_FILE"
echo "4. Java/Spring 生态的更新和最佳实践" >> "$TMP_FILE"
echo "" >> "$TMP_FILE"
echo "💡 学习建议：每天花 15-30 分钟浏览，标记感兴趣的内容，周末深入实践。" >> "$TMP_FILE"

echo "" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"
echo "🤖 由绫音自动推送 | $(date '+%H:%M:%S')" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"

# ============ 发送邮件 ============
CONTENT=$(cat "$TMP_FILE")

{
    echo "Subject: =?UTF-8?B?$(echo "🚀 技术前沿日报 - $DATE" | base64)?="
    echo "From: 绫音助手 <2682557218@qq.com>"
    echo "To: 蔡昭 <2682557218@qq.com>"
    echo "Content-Type: text/plain; charset=UTF-8"
    echo ""
    echo "$CONTENT"
} > "$EMAIL_FILE"

msmtp -t < "$EMAIL_FILE"

# 记录日志
echo "[$(date)] Tech push sent" >> "$LOG_DIR/tech-push.log"

# 清理
rm -f "$TMP_FILE" "$EMAIL_FILE"

echo "推送完成！"