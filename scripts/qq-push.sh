#!/bin/bash
# QQ推送脚本 - 将内容推送到QQ聊天
# 使用 OpenClaw message 工具

DATE=$(date "+%Y-%m-%d %A")
WORKSPACE="/root/.openclaw/workspace"
LOG_DIR="$WORKSPACE/logs"
QQ_MSG_FILE="$WORKSPACE/logs/qq-push-msg.txt"
mkdir -p "$LOG_DIR"

# 构建QQ消息
{
    echo "📅 每日推送 - $DATE"
    echo ""
    
    # ============ 1. GitHub 热门项目 ============
    echo "🔥 GitHub 今日热门项目"
    echo "─────────────────"
    
    GITHUB_DATA=$(curl -s "https://api.github.com/search/repositories?q=stars:>1000&sort=stars&order=desc&per_page=5" 2>/dev/null)
    
    if [ -n "$GITHUB_DATA" ]; then
        echo "$GITHUB_DATA" | jq -r '.items[] | "• \(.full_name) (\(.stargazers_count)⭐)\n  \(.description // "无描述")\n"' 2>/dev/null || echo "获取失败"
    else
        echo "获取失败"
    fi
    
    echo ""
    
    # ============ 2. 百度贴吧 - 忍龙 ============
    echo "🎮 百度贴吧 - 忍龙热门帖子"
    echo "─────────────────"
    
    TIEBA_DATA=$(curl -s -A "Mozilla/5.0" "https://tieba.baidu.com/mo/q/newmoindex?kw=%E5%BF%8D%E9%BE%99&pn=0&rn=5" 2>/dev/null)
    
    if [ -n "$TIEBA_DATA" ]; then
        THREADS=$(echo "$TIEBA_DATA" | jq -r '.data.thread_list[]? | "• \(.title)\n  👁\(.view_num) 💬\(.reply_num)\n"' 2>/dev/null | head -15)
        if [ -n "$THREADS" ]; then
            echo "$THREADS"
        else
            echo "贴吧获取失败"
        fi
    else
        echo "贴吧获取失败"
    fi
    
    echo ""
    
    # ============ 3. B站 - 忍龙视频 ============
    echo "📺 B站 - 忍龙热门视频"
    echo "─────────────────"
    
    BILI_DATA=$(curl -s -G \
        --data-urlencode "keyword=忍龙" \
        --data-urlencode "search_type=video" \
        --data-urlencode "page=1" \
        --data-urlencode "page_size=5" \
        --data-urlencode "order=click" \
        -H "User-Agent: Mozilla/5.0" \
        "https://api.bilibili.com/x/web-interface/search/type" 2>/dev/null)
    
    if [ -n "$BILI_DATA" ]; then
        echo "$BILI_DATA" | jq -r '.data.result[]? | "• \(.title | gsub("<em[^>]*>|</em>"; ""))\n  👁\(.play) 💬\(.danmaku)\n"' 2>/dev/null || echo "B站获取失败"
    else
        echo "B站获取失败"
    fi
    
    echo ""
    echo "🤖 绫音自动推送"
} > "$QQ_MSG_FILE"

# 记录日志
echo "[$(date)] QQ push prepared" >> "$LOG_DIR/qq-push.log"
cat "$QQ_MSG_FILE" >> "$LOG_DIR/qq-push.log"
echo "" >> "$LOG_DIR/qq-push.log"

echo "QQ消息已准备: $QQ_MSG_FILE"