# 洛神系统 - 后端 API 接口文档

> **版本：** v1.0.0  
> **更新时间：** 2026-03-30  
> **基础地址：** `http://localhost:9090/api/admin`

---

## 📋 目录

1. [Dashboard API](#1-dashboard-api)
2. [Agent 管理 API](#2-agent-管理-api)
3. [Skill 管理 API](#3-skill-管理-api)
4. [Session 管理 API](#4-session-管理-api)
5. [Memory 管理 API](#5-memory-管理-api)
6. [MCP 工具管理 API](#6-mcp-工具管理-api)
7. [数据模型](#7-数据模型)
8. [错误码说明](#8-错误码说明)

---

## 1. Dashboard API

### 1.1 获取统计数据

**接口说明：** 获取系统统计数据

**请求方式：** `GET`

**接口地址：** `/dashboard/stats`

**请求示例：**
```bash
GET /api/admin/dashboard/stats
```

**响应示例：**
```json
{
  "totalAgents": 5,
  "enabledAgents": 3,
  "disabledAgents": 2,
  "totalSkills": 10,
  "activeSessions": 8,
  "totalMemories": 150,
  "status": "running",
  "timestamp": 1711804800000
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| totalAgents | Long | Agent 总数 |
| enabledAgents | Long | 已启用的 Agent 数量 |
| disabledAgents | Long | 已禁用的 Agent 数量 |
| totalSkills | Long | Skill 总数 |
| activeSessions | Long | 活跃 Session 数量 |
| totalMemories | Long | Memory 总数 |
| status | String | 系统状态：running/error |
| timestamp | Long | 当前时间戳（毫秒） |

---

### 1.2 获取最近活动

**接口说明：** 获取最近创建的 Agent 活动

**请求方式：** `GET`

**接口地址：** `/dashboard/recent`

**请求示例：**
```bash
GET /api/admin/dashboard/recent
```

**响应示例：**
```json
{
  "recentAgents": [
    {
      "id": 1,
      "agentId": "agent-001",
      "name": "智能助手",
      "description": "通用对话助手",
      "type": "LEADER",
      "enabled": true,
      "createdAt": "2026-03-30T10:00:00"
    }
  ]
}
```

---

## 2. Agent 管理 API

### 2.1 获取所有 Agent

**接口说明：** 获取所有 Agent 配置列表

**请求方式：** `GET`

**接口地址：** `/agents`

**请求示例：**
```bash
GET /api/admin/agents
```

**响应示例：**
```json
[
  {
    "id": 1,
    "agentId": "agent-001",
    "name": "智能助手",
    "description": "通用对话助手",
    "systemPrompt": "你是一个友好的AI助手",
    "type": "LEADER",
    "modelName": "qwen-max",
    "toolsJson": "[\"search\",\"weather\"]",
    "skillsJson": "[\"general-qa\",\"calculator\"]",
    "parentAgentId": null,
    "enabled": true,
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
]
```

---

### 2.2 获取单个 Agent

**接口说明：** 根据 AgentId 获取配置详情

**请求方式：** `GET`

**接口地址：** `/agents/{agentId}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| agentId | String | 是 | Agent 唯一标识符 |

**请求示例：**
```bash
GET /api/admin/agents/agent-001
```

**响应示例：**
```json
{
  "id": 1,
  "agentId": "agent-001",
  "name": "智能助手",
  "description": "通用对话助手",
  "systemPrompt": "你是一个友好的AI助手",
  "type": "LEADER",
  "modelName": "qwen-max",
  "enabled": true
}
```

---

### 2.3 创建 Agent

**接口说明：** 创建新的 Agent 配置

**请求方式：** `POST`

**接口地址：** `/agents`

**请求头：**
```
Content-Type: application/json
```

**请求体：**
```json
{
  "agentId": "agent-002",
  "name": "代码助手",
  "description": "代码编写和审查助手",
  "systemPrompt": "你是一个专业的编程助手，擅长多种编程语言",
  "type": "CORE",
  "modelName": "qwen-max",
  "toolsJson": "[\"code-executor\",\"git\"]",
  "skillsJson": "[\"code-review\",\"debug\"]",
  "parentAgentId": "agent-001",
  "enabled": true
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| agentId | String | 是 | Agent 唯一标识符（不可重复） |
| name | String | 是 | Agent 名称 |
| description | String | 否 | Agent 描述 |
| systemPrompt | String | 否 | 系统提示词 |
| type | String | 是 | Agent 类型：LEADER/CORE/SUB |
| modelName | String | 否 | 使用的模型名称 |
| toolsJson | String | 否 | 工具列表（JSON 字符串） |
| skillsJson | String | 否 | 技能列表（JSON 字符串） |
| parentAgentId | String | 否 | 父 Agent ID（层级关系） |
| enabled | Boolean | 是 | 是否启用 |

**响应示例：**
```json
{
  "id": 2,
  "agentId": "agent-002",
  "name": "代码助手",
  "description": "代码编写和审查助手",
  "type": "CORE",
  "enabled": true,
  "createdAt": "2026-03-30T11:00:00",
  "updatedAt": "2026-03-30T11:00:00"
}
```

---

### 2.4 更新 Agent

**接口说明：** 更新 Agent 配置

**请求方式：** `PUT`

**接口地址：** `/agents/{agentId}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| agentId | String | 是 | Agent 唯一标识符 |

**请求体：**
```json
{
  "name": "代码助手 Pro",
  "description": "高级代码编写和审查助手",
  "systemPrompt": "你是一个资深的编程专家",
  "enabled": true
}
```

**响应示例：**
```json
{
  "id": 2,
  "agentId": "agent-002",
  "name": "代码助手 Pro",
  "updatedAt": "2026-03-30T12:00:00"
}
```

---

### 2.5 删除 Agent

**接口说明：** 删除指定 Agent

**请求方式：** `DELETE`

**接口地址：** `/agents/{agentId}`

**请求示例：**
```bash
DELETE /api/admin/agents/agent-002
```

**响应示例：**
```
HTTP 200 OK
```

---

### 2.6 刷新所有 Agent

**接口说明：** 重新加载所有 Agent 配置

**请求方式：** `POST`

**接口地址：** `/agents/refresh`

**请求示例：**
```bash
POST /api/admin/agents/refresh
```

**响应示例：**
```
刷新成功
```

---

## 3. Skill 管理 API

### 3.1 获取所有 Skill

**接口说明：** 获取所有 Skill 配置列表

**请求方式：** `GET`

**接口地址：** `/skills`

**响应示例：**
```json
[
  {
    "id": 1,
    "skillId": "skill-001",
    "name": "天气查询",
    "description": "查询指定城市的天气信息",
    "content": "# 天气查询技能\n\n## 功能\n查询天气...",
    "type": "TOOL",
    "agentId": "agent-001",
    "enabled": true,
    "version": "1.0.0",
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00"
  }
]
```

---

### 3.2 获取单个 Skill

**接口说明：** 根据 ID 获取 Skill 详情

**请求方式：** `GET`

**接口地址：** `/skills/{id}`

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | Skill 数据库 ID |

**请求示例：**
```bash
GET /api/admin/skills/1
```

**响应示例：**
```json
{
  "id": 1,
  "skillId": "skill-001",
  "name": "天气查询",
  "content": "# 天气查询技能...",
  "type": "TOOL"
}
```

**错误响应：**
```
HTTP 404 Not Found
```

---

### 3.3 创建 Skill

**接口说明：** 创建新的 Skill

**请求方式：** `POST`

**接口地址：** `/skills`

**请求体：**
```json
{
  "skillId": "skill-002",
  "name": "数据分析",
  "description": "分析和处理数据",
  "content": "# 数据分析技能\n\n## 功能\n...",
  "type": "WORKFLOW",
  "agentId": "agent-001",
  "enabled": true,
  "version": "1.0.0"
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillId | String | 是 | Skill 唯一标识符 |
| name | String | 是 | Skill 名称 |
| description | String | 否 | Skill 描述 |
| content | String | 否 | Markdown 格式的技能内容 |
| type | String | 是 | 类型：TOOL/WORKFLOW/KNOWLEDGE |
| agentId | String | 否 | 所属 Agent ID |
| enabled | Boolean | 是 | 是否启用 |
| version | String | 否 | 版本号 |

---

### 3.4 更新 Skill

**接口说明：** 更新 Skill 配置

**请求方式：** `PUT`

**接口地址：** `/skills/{id}`

**请求示例：**
```bash
PUT /api/admin/skills/1
```

**请求体：**
```json
{
  "name": "天气查询 Pro",
  "version": "2.0.0"
}
```

---

### 3.5 删除 Skill

**接口说明：** 删除指定 Skill

**请求方式：** `DELETE`

**接口地址：** `/skills/{id}`

**请求示例：**
```bash
DELETE /api/admin/skills/1
```

**响应：**
```
HTTP 200 OK (删除成功)
HTTP 404 Not Found (Skill 不存在)
```

---

## 4. Session 管理 API

### 4.1 获取所有 Session

**接口说明：** 获取所有会话列表

**请求方式：** `GET`

**接口地址：** `/sessions`

**响应示例：**
```json
[
  {
    "id": 1,
    "sessionId": "session-001",
    "userId": "user-123",
    "agentId": "agent-001",
    "status": "active",
    "sessionData": "{}",
    "messageCount": 10,
    "createdAt": "2026-03-30T10:00:00",
    "lastActiveAt": "2026-03-30T10:30:00",
    "expiresAt": "2026-03-31T10:00:00"
  }
]
```

---

### 4.2 获取单个 Session

**接口说明：** 根据 SessionId 获取详情

**请求方式：** `GET`

**接口地址：** `/sessions/{sessionId}`

---

### 4.3 获取用户的 Session 列表

**接口说明：** 获取指定用户的所有 Session

**请求方式：** `GET`

**接口地址：** `/sessions/user/{userId}`

**请求示例：**
```bash
GET /api/admin/sessions/user/user-123
```

---

### 4.4 获取 Agent 的 Session 列表

**接口说明：** 获取指定 Agent 的所有 Session

**请求方式：** `GET`

**接口地址：** `/sessions/agent/{agentId}`

**请求示例：**
```bash
GET /api/admin/sessions/agent/agent-001
```

---

### 4.5 创建 Session

**接口说明：** 创建新会话

**请求方式：** `POST`

**接口地址：** `/sessions`

**请求体：**
```json
{
  "sessionId": "session-002",
  "userId": "user-456",
  "agentId": "agent-001"
}
```

---

### 4.6 更新 Session 状态

**接口说明：** 更新会话状态

**请求方式：** `PUT`

**接口地址：** `/sessions/{sessionId}/status?status={status}`

**请求示例：**
```bash
PUT /api/admin/sessions/session-001/status?status=paused
```

**状态值：**
- `active` - 活跃
- `paused` - 暂停
- `closed` - 关闭

---

### 4.7 删除 Session

**接口说明：** 删除指定会话

**请求方式：** `DELETE`

**接口地址：** `/sessions/{sessionId}`

---

### 4.8 清理过期 Session

**接口说明：** 清理所有过期的会话

**请求方式：** `POST`

**接口地址：** `/sessions/clean-expired`

**响应示例：**
```json
5
```
（返回清理的会话数量）

---

## 5. Memory 管理 API

### 5.1 获取所有 Memory

**接口说明：** 获取所有记忆记录

**请求方式：** `GET`

**接口地址：** `/memories`

**响应示例：**
```json
[
  {
    "id": 1,
    "memoryId": "memory-001",
    "sessionId": "session-001",
    "agentId": "agent-001",
    "type": "long_term",
    "content": "用户偏好使用中文交流",
    "tagsJson": "[\"语言\",\"偏好\"]",
    "importance": 0.8,
    "createdAt": "2026-03-30T10:00:00",
    "lastAccessedAt": "2026-03-30T10:30:00",
    "expiresAt": null
  }
]
```

---

### 5.2 获取单个 Memory

**接口说明：** 根据 MemoryId 获取详情

**请求方式：** `GET`

**接口地址：** `/memories/{memoryId}`

---

### 5.3 获取 Session 的 Memory

**接口说明：** 获取指定会话的所有记忆

**请求方式：** `GET`

**接口地址：** `/memories/session/{sessionId}`

---

### 5.4 获取 Agent 的 Memory

**接口说明：** 获取指定 Agent 的所有记忆

**请求方式：** `GET`

**接口地址：** `/memories/agent/{agentId}`

---

### 5.5 按类型获取 Memory

**接口说明：** 获取指定类型的记忆

**请求方式：** `GET`

**接口地址：** `/memories/type/{type}`

**类型值：**
- `short_term` - 短期记忆
- `long_term` - 长期记忆
- `semantic` - 语义记忆

---

### 5.6 搜索 Memory

**接口说明：** 关键词搜索记忆内容

**请求方式：** `GET`

**接口地址：** `/memories/search?keyword={keyword}`

**请求示例：**
```bash
GET /api/admin/memories/search?keyword=偏好
```

---

### 5.7 创建 Memory

**接口说明：** 创建新的记忆

**请求方式：** `POST`

**接口地址：** `/memories`

**请求体：**
```json
{
  "memoryId": "memory-002",
  "sessionId": "session-001",
  "agentId": "agent-001",
  "type": "long_term",
  "content": "用户是一名软件工程师"
}
```

---

### 5.8 更新 Memory 内容

**接口说明：** 更新记忆内容

**请求方式：** `PUT`

**接口地址：** `/memories/{memoryId}/content?content={content}`

---

### 5.9 更新 Memory 重要性

**接口说明：** 更新记忆的重要性评分

**请求方式：** `PUT`

**接口地址：** `/memories/{memoryId}/importance?importance={importance}`

**参数说明：**
- `importance`: 0.0 ~ 1.0 之间的数值

---

### 5.10 删除 Memory

**接口说明：** 删除指定记忆

**请求方式：** `DELETE`

**接口地址：** `/memories/{memoryId}`

---

### 5.11 删除 Session 的所有 Memory

**接口说明：** 删除指定会话的所有记忆

**请求方式：** `DELETE`

**接口地址：** `/memories/session/{sessionId}`

---

## 6. MCP 工具管理 API

### 6.1 获取所有 MCP 工具

**接口说明：** 获取所有 MCP 工具配置

**请求方式：** `GET`

**接口地址：** `/mcp`

**响应示例：**
```json
[
  {
    "id": 1,
    "toolId": "tool-001",
    "name": "网络搜索",
    "description": "在互联网上搜索信息",
    "type": "query",
    "parametersJson": "{\"query\":{\"type\":\"string\",\"required\":true}}",
    "implementation": "async function search(query) { ... }",
    "endpoint": null,
    "agentId": "agent-001",
    "enabled": true,
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00"
  }
]
```

---

### 6.2 获取单个 MCP 工具

**接口说明：** 根据 ToolId 获取详情

**请求方式：** `GET`

**接口地址：** `/mcp/{toolId}`

---

### 6.3 创建 MCP 工具

**接口说明：** 创建新的 MCP 工具

**请求方式：** `POST`

**接口地址：** `/mcp`

**请求体：**
```json
{
  "toolId": "tool-002",
  "name": "数据库查询",
  "description": "执行 SQL 查询",
  "type": "query",
  "parametersJson": "{\"sql\":{\"type\":\"string\",\"required\":true}}",
  "implementation": "function executeSql(sql) { ... }",
  "endpoint": null,
  "agentId": "agent-001",
  "enabled": true
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| toolId | String | 是 | 工具唯一标识符 |
| name | String | 是 | 工具名称 |
| description | String | 否 | 工具描述 |
| type | String | 是 | 类型：query/action/control |
| parametersJson | String | 否 | 参数定义（JSON 格式） |
| implementation | String | 否 | 执行脚本/代码 |
| endpoint | String | 否 | MCP 服务端点（远程工具） |
| agentId | String | 否 | 所属 Agent ID |
| enabled | Boolean | 是 | 是否启用 |

---

### 6.4 更新 MCP 工具

**接口说明：** 更新 MCP 工具配置

**请求方式：** `PUT`

**接口地址：** `/mcp/{toolId}`

---

### 6.5 删除 MCP 工具

**接口说明：** 删除指定 MCP 工具

**请求方式：** `DELETE`

**接口地址：** `/mcp/{toolId}`

---

### 6.6 刷新所有 MCP 工具

**接口说明：** 重新加载所有 MCP 工具配置

**请求方式：** `POST`

**接口地址：** `/mcp/refresh`

---

## 7. 数据模型

### 7.1 AgentConfigEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据库主键 |
| agentId | String | Agent 唯一标识符 |
| name | String | Agent 名称 |
| description | String | 描述 |
| systemPrompt | String | 系统提示词 |
| type | String | 类型：LEADER/CORE/SUB |
| modelName | String | 使用的模型 |
| toolsJson | String | 工具列表（JSON） |
| skillsJson | String | 技能列表（JSON） |
| parentAgentId | String | 父 Agent ID |
| enabled | Boolean | 是否启用 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| createdBy | String | 创建者 |
| updatedBy | String | 更新者 |

### 7.2 SkillConfigEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据库主键 |
| skillId | String | Skill 唯一标识符 |
| name | String | 名称 |
| description | String | 描述 |
| content | String | Markdown 内容 |
| type | String | 类型：TOOL/WORKFLOW/KNOWLEDGE |
| agentId | String | 所属 Agent |
| enabled | Boolean | 是否启用 |
| version | String | 版本号 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 7.3 SessionConfigEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据库主键 |
| sessionId | String | Session 唯一标识符 |
| userId | String | 用户 ID |
| agentId | String | Agent ID |
| status | String | 状态：active/paused/closed |
| sessionData | String | Session 数据（JSON） |
| messageCount | Integer | 消息数量 |
| createdAt | LocalDateTime | 创建时间 |
| lastActiveAt | LocalDateTime | 最后活跃时间 |
| expiresAt | LocalDateTime | 过期时间 |

### 7.4 MemoryConfigEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据库主键 |
| memoryId | String | Memory 唯一标识符 |
| sessionId | String | Session ID |
| agentId | String | Agent ID |
| type | String | 类型：short_term/long_term/semantic |
| content | String | 记忆内容 |
| tagsJson | String | 标签（JSON） |
| importance | Double | 重要性评分（0-1） |
| createdAt | LocalDateTime | 创建时间 |
| lastAccessedAt | LocalDateTime | 最后访问时间 |
| expiresAt | LocalDateTime | 过期时间 |

### 7.5 McpConfigEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据库主键 |
| toolId | String | 工具唯一标识符 |
| name | String | 工具名称 |
| description | String | 描述 |
| type | String | 类型：query/action/control |
| parametersJson | String | 参数定义（JSON） |
| implementation | String | 实现代码 |
| endpoint | String | MCP 服务端点 |
| agentId | String | 所属 Agent |
| enabled | Boolean | 是否启用 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

---

## 8. 错误码说明

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 OK | 请求成功 |
| 201 Created | 资源创建成功 |
| 400 Bad Request | 请求参数错误 |
| 404 Not Found | 资源不存在 |
| 409 Conflict | 资源冲突（如 ID 重复） |
| 500 Internal Server Error | 服务器内部错误 |

### 错误响应格式

```json
{
  "timestamp": "2026-03-30T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/admin/agents/nonexistent"
}
```

---

## 📝 附录

### A. cURL 命令示例

```bash
# 获取所有 Agent
curl -X GET http://localhost:9090/api/admin/agents

# 创建 Agent
curl -X POST http://localhost:9090/api/admin/agents \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "test-agent",
    "name": "测试助手",
    "type": "LEADER",
    "enabled": true
  }'

# 更新 Agent
curl -X PUT http://localhost:9090/api/admin/agents/test-agent \
  -H "Content-Type: application/json" \
  -d '{"name": "新名称"}'

# 删除 Agent
curl -X DELETE http://localhost:9090/api/admin/agents/test-agent
```

### B. Postman 导入

可以将以上接口导入 Postman 进行测试：
1. 创建新 Collection
2. 添加变量：`baseUrl` = `http://localhost:9090/api/admin`
3. 按照文档添加各接口请求

---

**文档版本：** v1.0.0  
**最后更新：** 2026-03-30  
**维护团队：** 洛神开发团队