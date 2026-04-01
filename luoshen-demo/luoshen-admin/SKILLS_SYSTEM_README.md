# 洛神平台 - 技能管理系统

## 功能概述

本次更新实现了洛神平台技能管理系统，支持：

- ✅ 上传 .skill 压缩包（包含 SKILL.md、脚本、资源等）
- ✅ 创建纯文本技能（不上传文件）
- ✅ 灵活的类型系统（分类 + 标签 + 自定义类型）
- ✅ 自动安全检查（检测敏感文件和危险脚本）
- ✅ Web 管理界面（上传页面、列表页面、绑定页面）
- ✅ 技能查询和筛选（分页、搜索、分类筛选）
- ✅ Agent 技能绑定（inherit/custom/none 三种模式）
- ✅ 完整的 REST API（15 个接口）

## 快速开始

### 访问管理后台

```
http://localhost:9090/
```

### 功能页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 系统概览和导航 |
| 技能管理 | `/skills` | 技能列表、搜索、筛选 |
| 上传技能包 | `/skills/upload` | 上传 .skill 压缩包 |
| Agent 技能绑定 | `/agents/skills` | 为 Agent 绑定技能 |
| Agent 树形结构 | `/api/admin/agents/tree` | API 返回 JSON |

## 技能包上传

### 上传页面

访问：`http://localhost:9090/skills/upload`

**功能：**
- 拖拽上传 .skill 或 .zip 文件
- 自动解压和验证
- 表单填写（技能 ID、名称、描述、分类、标签）
- 实时进度显示

**API：**

```bash
curl -X POST http://localhost:9090/api/admin/skills/upload \
  -F "file=@github.skill" \
  -F "skillId=github" \
  -F "name=GitHub 集成" \
  -F "category=dev" \
  -F 'tags=["github","git","api"]'
```

## 技能管理

### 技能列表

访问：`http://localhost:9090/skills`

**功能：**
- 分页展示所有技能
- 搜索和筛选
- 查看详情
- 启用/禁用
- 删除技能

### 创建纯文本技能

```bash
curl -X POST http://localhost:9090/api/admin/skills/create \
  -H "Content-Type: application/json" \
  -d '{
    "skillId": "prompt-template",
    "name": "提示词模板",
    "description": "用于生成提示词",
    "content": "# 提示词模板\n\n这是一个模板",
    "category": "dev",
    "riskLevel": "low"
  }'
```

## Agent 技能绑定

### 绑定页面

访问：`http://localhost:9090/agents/skills`

**功能：**
- 下拉选择 Agent
- 左侧：可用技能列表
- 右侧：已绑定技能列表
- 双击或拖拽绑定/解绑
- 保存配置

### API 示例

```bash
# 绑定单个技能
curl -X POST http://localhost:9090/api/admin/agents/device-agent/skills/bind \
  -H "Content-Type: application/json" \
  -d '{
    "skillId": "github",
    "bindMode": "custom",
    "priority": 0
  }'

# 批量绑定
curl -X POST http://localhost:9090/api/admin/agents/device-agent/skills/batch \
  -H "Content-Type: application/json" \
  -d '{
    "skillIds": ["github", "git"],
    "bindMode": "custom"
  }'

# 设置技能配置
curl -X PUT http://localhost:9090/api/admin/agents/device-agent/skills/config \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "custom",
    "allowed": ["github", "git"],
    "denied": []
  }'

# 查询 Agent 的技能
curl http://localhost:9090/api/admin/agents/device-agent/skills
```

## 技能包格式

### 目录结构

```
my-skill.skill (ZIP 文件)
├── SKILL.md              # 必需：技能定义文件
├── scripts/              # 可选：可执行脚本
│   ├── helper.py
│   └── setup.sh
├── references/           # 可选：参考文档
│   └── api-docs.md
└── assets/              # 可选：资源文件
    └── template.png
```

### SKILL.md 格式

```markdown
---
name: My Skill
description: 这个技能用于...
---

# 技能说明

详细描述技能的功能...

## 使用指南

1. 步骤一
2. 步骤二
```

## 技能类型系统

### 灵活的分类

**分类（可选）：**
- 主分类：dev, ops, data, integration
- 子分类：git, cicd, analysis

**标签（推荐）：**
- 多标签支持：["github", "git", "api"]
- 用于灵活组织

**自定义类型（兜底）：**
- 不满足预定义分类时使用
- 用户可自由定义

## 安全检查

系统会自动进行以下安全检查：

1. **敏感文件检测**
   - 检测 `.ssh`, `.aws`, `token`, `password`, `secret`, `key` 等关键词

2. **危险脚本检测**
   - 检测 `rm -rf`, `eval(` 等危险操作

3. **文件大小限制**
   - 单个文件不超过 10MB
   - 整个技能包不超过 50MB

4. **Zip Slip 防护**
   - 防止恶意文件路径穿越

## API 接口汇总

### 技能管理 API（8个）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/skills/upload` | POST | 上传技能包 |
| `/api/admin/skills/create` | POST | 创建纯文本技能 |
| `/api/admin/skills` | GET | 查询技能列表（分页、筛选） |
| `/api/admin/skills/{skillId}` | GET | 获取技能详情 |
| `/api/admin/skills/{skillId}` | PUT | 更新技能 |
| `/api/admin/skills/{skillId}` | DELETE | 删除技能 |
| `/api/admin/skills/{skillId}/toggle` | POST | 切换状态 |
| `/api/admin/skills/search` | GET | 搜索技能 |
| `/api/admin/skills/categories` | GET | 获取所有分类 |

### Agent 技能绑定 API（7个）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/agents/{agentId}/skills/bind` | POST | 绑定技能 |
| `/api/admin/agents/{agentId}/skills/{skillId}` | DELETE | 解绑技能 |
| `/api/admin/agents/{agentId}/skills` | GET | 查询 Agent 的技能 |
| `/api/admin/agents/{agentId}/skills/batch` | POST | 批量绑定 |
| `/api/admin/agents/{agentId}/skills/config` | PUT | 设置技能配置 |
| `/api/admin/agents/{agentId}/skills/{skillId}/toggle` | POST | 切换状态 |
| `/api/admin/agents/{agentId}/skills` | DELETE | 删除所有绑定 |

## 测试

### 启动服务

```bash
cd luoshen-demo
mvn clean install
cd luoshen-admin
mvn spring-boot:run
```

### 运行测试

```bash
# 所有测试
cd luoshen-demo/luoshen-admin
mvn test

# 特定测试
mvn test -Dtest=SkillQueryServiceTest
mvn test -Dtest=SkillBindingServiceTest
mvn test -Dtest=AgentSkillBindingIntegrationTest
```

## 实施进度

- ✅ Phase 1: 数据模型扩展
- ✅ Phase 2: 技能包上传功能
- ✅ Phase 3: Agent 技能加载和绑定
- ✅ Phase 4: 前端完善
- ⏳ Phase 5: 测试和文档（进行中）

---

**版本：** v1.0
**状态：** Phase 1-4 完成
**更新时间：** 2026-04-01
