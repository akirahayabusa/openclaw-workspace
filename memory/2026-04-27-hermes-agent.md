# Hermes Agent v1.3 学习笔记
> 来源：https://mp.weixin.qq.com/s/tr1sosQFdhX0VgBoi8Q4iQ
> 学习时间：2026-04-27

## 一、Hermes 是什么？

Hermes Agent 是一个**会进化的 AI 工作伙伴**。核心差异：
- **持久记忆**：跨 session 保留对话记忆、用户偏好、错误经验
- **对话式配置**：不用记命令，自然语言驱动
- **定位**："你说话，它干活，越用越懂你"

## 二、核心能力

### 模型支持（20+ 供应商）
- OpenAI（GPT-5）、Anthropic（Claude）、xAI（Grok）
- DeepSeek、Qwen3.5、Gemma 3、MiniMax M2.5
- Ollama（本地模型）、OpenRouter（聚合 200+ 模型）
- Azure OpenAI（企业内网）、AWS Bedrock（原生调用）

### 消息平台接入（16 个）
- 飞书、微信（iLink Bot API）、企业微信、钉钉
- Telegram、Discord、WhatsApp、Signal
- Slack、iMessage（BlueBubbles）、邮件、SMS（Twilio）

### 技能系统（Skill）
- 网页搜索、GitHub 管理、arXiv 论文搜索
- 图片生成、定时任务管理
- 社区市场可搜索安装

### 自动化任务
- 定时任务（天气、新闻、监控）
- Webhook 监控（PR 通知等）

## 三、v0.10.0 新功能

| 功能 | 说明 |
|------|------|
| **MCP Server & Client** | Model Context Protocol，可做服务端暴露工具，也可做客户端调用外部工具 |
| **NousBridge** | 桥接 Nous Research 生态外部服务（订阅制） |
| **RAG Memory** | 语义搜索记忆，自然语言查找历史内容 |
| **Claude Agentic Coding** | 自主编编程模式，适合复杂长任务 |
| **新模型** | Qwen3.5、Gemma 3、DeepSeek V3/R2 |
| **Azure OpenAI** | 企业级原生接入 |
| **OpenRouter v2 API** | 聚合平台升级 |
| **Bedrock Transport** | AWS 原生调用，低延迟低成本 |

## 四、与 OpenClaw 的对比和迁移

### 可迁移内容
- MEMORY.md、SOUL.md、USER.md
- Skill 配置、模型 API Key
- Telegram / Discord 等海外平台配置

### 需重建内容
- 飞书 / 企业微信（接入方式不同）
- Cron 定时任务
- 自定义插件

### 选型参考
- Hermes 优势：飞书/企业微信原生支持、学习循环（越用越懂你）、16 平台接入、微信 iLink Bot API、手机 Termux 支持、Dashboard 图形界面
- 两者都完全开源

## 五、安装

```bash
curl -fsSL https://raw.githubusercontent.com/NamespaceResearch/hermes-agent/main/scripts/install.sh | bash
source ~/.bashrc
hermes
```

支持：Linux、macOS、Windows（WSL2）、安卓（Termux）

## 六、关键设计理念

1. **自然语言优先**：所有操作都通过对话完成，slash 命令是备用
2. **持久记忆**：跨 session 保留，关闭再打开依然记得
3. **技能扩展**：官方+社区技能，装上即用
4. **多平台统一**：一个 AI 跨 16 个平台使用
5. **会进化**：记住偏好和错误，越用越好

---

*备注：这篇文章本质是 Hermes Agent 的推广文，定位对标 OpenClaw。核心差异化卖点是"学习循环"和更多平台原生支持。*
