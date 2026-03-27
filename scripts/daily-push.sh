#!/bin/bash
# 每日推送脚本 - GitHub热门、贴吧忍龙、B站忍龙
# 每天 9:00 执行

DATE=$(date "+%Y-%m-%d %A")
WORKSPACE="/root/.openclaw/workspace"
LOG_DIR="$WORKSPACE/logs"
mkdir -p "$LOG_DIR"

# 临时文件
TMP_FILE=$(mktemp)
EMAIL_FILE=$(mktemp)

echo "========================================" >> "$TMP_FILE"
echo "📅 每日推送 - $DATE" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"
echo "" >> "$TMP_FILE"

# ============ 1. GitHub 热门项目 ============
echo "🔥 GitHub 今日热门项目" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

GITHUB_DATA=$(curl -s "https://api.github.com/search/repositories?q=stars:>1000&sort=stars&order=desc&per_page=5" 2>/dev/null)

if [ -n "$GITHUB_DATA" ]; then
    echo "$GITHUB_DATA" | jq -r '.items[] | "• \(.full_name) (\(.stargazers_count)⭐)\n  \(.description // "无描述")\n  🔗 \(.html_url)\n"' 2>/dev/null >> "$TMP_FILE" || echo "获取失败" >> "$TMP_FILE"
else
    echo "获取失败" >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"

# ============ 2. 百度贴吧 - 忍龙 ============
echo "🎮 百度贴吧 - 忍龙热门帖子" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

# 使用移动端API获取贴吧帖子
TIEBA_DATA=$(curl -s -A "Mozilla/5.0" "https://tieba.baidu.com/mo/q/newmoindex?kw=%E5%BF%8D%E9%BE%99&pn=0&rn=5" 2>/dev/null)

if [ -n "$TIEBA_DATA" ]; then
    # 尝试解析贴吧数据
    THREADS=$(echo "$TIEBA_DATA" | jq -r '.data.thread_list[]? | "• \(.title)\n  👁 \(.view_num) | 💬 \(.reply_num)\n  🔗 https://tieba.baidu.com/p/\(.tid)\n"' 2>/dev/null | head -20)
    if [ -n "$THREADS" ]; then
        echo "$THREADS" >> "$TMP_FILE"
    else
        # 备用方案：直接访问贴吧搜索
        echo "正在尝试备用方案..." >> "$TMP_FILE"
        curl -s -A "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)" \
            "https://tieba.baidu.com/f?kw=%E5%BF%8D%E9%BE%99&ie=utf-8&pn=0" 2>/dev/null | \
            grep -oP 'href="/p/[^"]+"[^>]*>[^<]+' | head -5 | \
            sed 's/href="\/p\//🔗 https:\/\/tieba.baidu.com\/p\//g; s/".*>/ /g' >> "$TMP_FILE" 2>/dev/null || \
            echo "贴吧获取失败，请稍后重试" >> "$TMP_FILE"
    fi
else
    echo "贴吧获取失败" >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"

# ============ 3. B站 - 忍龙视频 ============
echo "📺 B站 - 忍龙热门视频" >> "$TMP_FILE"
echo "----------------------------------------" >> "$TMP_FILE"

# B站搜索API
BILI_DATA=$(curl -s -G \
    --data-urlencode "keyword=忍龙" \
    --data-urlencode "search_type=video" \
    --data-urlencode "page=1" \
    --data-urlencode "page_size=5" \
    --data-urlencode "order=click" \
    -H "User-Agent: Mozilla/5.0" \
    "https://api.bilibili.com/x/web-interface/search/type" 2>/dev/null)

if [ -n "$BILI_DATA" ]; then
    echo "$BILI_DATA" | jq -r '.data.result[]? | "• \(.title | gsub("<em[^>]*>|</em>"; ""))\n  👁 \(.play) | 💬 \(.danmaku)\n  🔗 https://www.bilibili.com/video/\(.bvid)\n"' 2>/dev/null >> "$TMP_FILE" || echo "B站获取失败" >> "$TMP_FILE"
else
    echo "B站获取失败" >> "$TMP_FILE"
fi

echo "" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"
echo "🤖 由绫音自动推送 | $(date '+%H:%M:%S')" >> "$TMP_FILE"
echo "========================================" >> "$TMP_FILE"

# ============ 发送邮件 ============
CONTENT=$(cat "$TMP_FILE")

# 构建邮件（UTF-8编码）
{
    echo "Subject: =?UTF-8?B?$(echo "每日推送 - $DATE" | base64)?="
    echo "From: 绫音助手 <2682557218@qq.com>"
    echo "To: 蔡昭 <2682557218@qq.com>"
    echo "Content-Type: text/plain; charset=UTF-8"
    echo ""
    echo "$CONTENT"
} > "$EMAIL_FILE"

msmtp -t < "$EMAIL_FILE"

# 记录日志
echo "[$(date)] Daily push sent" >> "$LOG_DIR/daily-push.log"
echo "$CONTENT" >> "$LOG_DIR/daily-push.log"
echo "" >> "$LOG_DIR/daily-push.log"

# 清理
rm -f "$TMP_FILE" "$EMAIL_FILE"

echo "推送完成！"