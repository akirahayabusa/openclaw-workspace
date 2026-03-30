# OpenClaw 子智能体配置偏好

## 子智能体超时时间

- **默认超时时间：** 1 小时（3600 秒）
- **原因：** 复杂任务（如学习研究、项目开发）需要更长的执行时间

## 使用方式

调用 `sessions_spawn` 时，自动设置 `runTimeoutSeconds: 3600`

## 其他配置

- `maxConcurrent`: 8（最大并发子智能体数量）
- `mode`: run（一次性执行）或 session（持久会话）

---

**注意：** OpenClaw 配置文件 (`openclaw.json`) 目前不支持 `agents.defaults.subagents.timeoutSeconds` 配置项。此文件仅作为偏好记录，实际调用时需要手动传递参数。