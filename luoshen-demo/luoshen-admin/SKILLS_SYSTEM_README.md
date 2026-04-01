# 洛神平台 - 技能管理系统

## 功能概述

本次更新实现了技能包上传和管理功能，支持：

- ✅ 上传 .skill 压缩包（包含 SKILL.md、脚本、资源等）
- ✅ 创建纯文本技能（不上传文件）
- ✅ 灵活的类型系统（分类 + 标签 + 自定义类型）
- ✅ 自动安全检查（检测敏感文件和危险脚本）
- ✅ Web 管理界面（上传页面、列表页面）
- ✅ 技能查询和筛选（分页、搜索、分类筛选）
- ✅ 技能管理（启用/禁用、更新、删除）

## 数据模型

### 1. SkillConfigEntity（扩展）

新增字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `packagePath` | String | 技能包路径 |
| `source` | String | 来源：builtin, user_upload, marketplace |
| `riskLevel` | String | 风险等级：low, medium, high |
| `fileSize` | Long | 文件大小 |
| `checksum` | String | SHA-256 校验和 |
| `hasScripts` | Boolean | 是否包含脚本 |
| `scriptFiles` | String | 脚本列表（JSON） |
| `category` | String | 主分类（可选） |
| `subCategory` | String | 子分类（可选） |
| `tags` | String | 标签数组（JSON） |
| `customType` | String | 自定义类型 |
| `capabilities` | String | 能力特征（JSON） |

### 2. AgentSkillRelationEntity（新增）

定义 Agent 和 Skill 的多对多关系：

| 字段 | 类型 | 说明 |
|------|------|------|
| `agentId` | String | Agent ID |
| `skillId` | String | Skill ID |
| `bindMode` | String | 绑定模式：inherit, custom, none |
| `enabled` | Boolean | 是否启用 |
| `priority` | Integer | 优先级 |
| `config` | String | 配置参数（JSON） |

## API 接口

### 1. 技能管理 API

#### 1.1 上传技能包

```bash
POST /api/admin/skills/upload
Content-Type: multipart/form-data

参数：
- file: 技能包文件（必填）
- skillId: 技能 ID（可选）
- name: 技能名称（可选）
- description: 技能描述（可选）
- category: 主分类（可选）
- subCategory: 子分类（可选）
- tags: 标签（JSON 数组，可选）
- customType: 自定义类型（可选）
```

**示例：**

```bash
curl -X POST http://localhost:9090/api/admin/skills/upload \
  -F "file=@github.skill" \
  -F "skillId=github" \
  -F "name=GitHub 集成" \
  -F "description=用于 GitHub 操作" \
  -F "category=dev" \
  -F "tags=[\"github\",\"git\"]"
```

**响应：**

```json
{
  "success": true,
  "message": "技能包上传成功",
  "skill": {
    "id": 1,
    "skillId": "github",
    "name": "GitHub 集成",
    "packagePath": "/root/.luoshen/skills/user/github",
    "source": "user_upload",
    "riskLevel": "low",
    "category": "dev"
  }
}
```

#### 1.2 创建纯文本技能

```bash
POST /api/admin/skills/create
Content-Type: application/json

{
  "skillId": "prompt-template",
  "name": "提示词模板",
  "description": "用于生成提示词...",
  "content": "# 技能说明\n...",
  "category": "dev",
  "tags": ["prompt", "template"]
}
```

#### 1.3 查询技能列表

```bash
GET /api/admin/skills?category=dev&enabled=true&page=0&size=20
```

**响应：**

```json
{
  "content": [
    {
      "id": 1,
      "skillId": "github",
      "name": "GitHub 集成",
      "category": "dev",
      "enabled": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Agent 技能绑定 API

#### 2.1 绑定技能到 Agent

```bash
POST /api/admin/agents/{agentId}/skills/bind
Content-Type: application/json

{
  "skillId": "github",
  "bindMode": "custom",
  "priority": 0,
  "config": {}
}
```

**响应：**

```json
{
  "success": true,
  "message": "技能绑定成功",
  "relation": {
    "id": 1,
    "agentId": "device-agent",
    "skillId": "github",
    "bindMode": "custom",
    "enabled": true,
    "priority": 0
  }
}
```

#### 2.2 查询 Agent 的技能

```bash
GET /api/admin/agents/{agentId}/skills
```

**响应：**

```json
[
  {
    "agentId": "device-agent",
    "skillId": "github",
    "name": "GitHub 集成",
    "bindMode": "custom",
    "enabled": true,
    "priority": 0,
    "category": "dev",
    "source": "user_upload",
    "riskLevel": "low"
  }
]
```

#### 2.3 批量绑定技能

```bash
POST /api/admin/agents/{agentId}/skills/batch
Content-Type: application/json

{
  "skillIds": ["github", "git"],
  "bindMode": "custom"
}
```

#### 2.4 设置 Agent 技能配置

```bash
PUT /api/admin/agents/{agentId}/skills/config
Content-Type: application/json

{
  "mode": "custom",
  "allowed": ["github", "git"],
  "denied": []
}
```

#### 2.5 解绑技能

```bash
DELETE /api/admin/agents/{agentId}/skills/{skillId}
```

#### 2.6 切换技能绑定状态

```bash
POST /api/admin/agents/{agentId}/skills/{skillId}/toggle
```

## Web 界面

### 1. 技能包上传页面

**访问地址：** `http://localhost:9090/skills/upload`

**功能：**
- 拖拽上传技能包
- 表单填写（技能 ID、名称、描述、分类、标签）
- 实时验证和进度显示

### 2. 技能管理列表页面

**访问地址：** `http://localhost:9090/skills`

**功能：**
- 技能列表展示（分页）
- 搜索和筛选（按分类、来源、状态）
- 查看详情
- 启用/禁用
- 删除技能

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

## 配置说明

### application.yml

```yaml
luoshen:
  skills:
    # 技能存储路径
    storage-path: ${user.home}/.luoshen/skills
    # 最大文件大小（字节，默认 50MB）
    max-file-size: 52428800
    # 允许的文件扩展名
    allowed-extensions: .skill,.zip
    # 是否自动创建技能目录
    auto-create-dir: true
```

## 存储结构

```
~/.luoshen/
├── skills/
│   ├── builtin/              # 内置技能（只读）
│   │   ├── github/
│   │   └── obsidian/
│   │
│   └── user/                 # 用户技能
│       ├── github/
│       │   ├── SKILL.md
│       │   ├── scripts/
│       │   └── references/
│       └── my-skill/
│
└── data/
    └── luoshen-admin.mdb     # H2 数据库
```

## 测试

### 单元测试

```bash
cd luoshen-demo/luoshen-admin
mvn test -Dtest=SkillQueryServiceTest
```

### 集成测试

```bash
cd luoshen-demo/luoshen-admin
mvn test -Dtest=SkillManagementIntegrationTest
```

### 启动服务

```bash
cd luoshen-demo
mvn clean install
cd luoshen-admin
mvn spring-boot:run
```

### 测试上传

访问上传页面：`http://localhost:9090/skills/upload`

### 测试列表

访问列表页面：`http://localhost:9090/skills`

### API 测试

```bash
# 准备测试技能包
cd /tmp
mkdir -p test-skill
echo -e "---\nname: Test Skill\ndescription: 测试技能\n---\n\n# 测试\n这是一个测试技能" > test-skill/SKILL.md
zip -r test-skill.skill test-skill/

# 上传
curl -X POST http://localhost:9090/api/admin/skills/upload \
  -F "file=@test-skill.skill"
```

## 实施进度

- ✅ **Phase 1**: 数据模型扩展（已完成）
- ✅ **Phase 2**: 技能包上传功能（已完成）
- ⏳ **Phase 3**: Agent 技能加载（待实现）
- ⏳ **Phase 4**: 前端完善（待实现）
- ⏳ **Phase 5**: 测试和文档（进行中）

## 注意事项

1. **文件上传限制**
   - 最大 50MB
   - 只支持 .skill 和 .zip 格式

2. **安全考虑**
   - 上传的脚本会经过安全检查
   - 高风险技能需要管理员确认

3. **兼容性**
   - 旧的 `type` 字段已标记为 `@Deprecated`
   - 建议使用新的 `category + tags + customType` 系统

## 常见问题

### Q: 技能包上传失败？

A: 检查以下几点：
- 文件格式是否正确（.skill 或 .zip）
- 文件大小是否超过 50MB
- SKILL.md 是否存在且格式正确

### Q: 如何创建纯文本技能？

A: 使用 `/api/admin/skills/create` 接口，只提供 content 字段，不上传文件。

### Q: 分类和标签有什么区别？

A:
- **分类**：层级结构（主分类 + 子分类），用于快速筛选
- **标签**：平面结构，可以多个，用于灵活组织

### Q: 如何禁用某个技能？

A: 使用 `POST /api/admin/skills/{skillId}/toggle` 接口切换启用状态。

---

**创建时间：** 2026-04-01
**版本：** v1.1
**状态：** Phase 2 完成
