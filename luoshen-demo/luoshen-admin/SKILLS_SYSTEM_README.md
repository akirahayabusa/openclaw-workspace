# 洛神平台 - 技能管理系统

## 功能概述

本次更新实现了技能包上传和管理功能，支持：

- ✅ 上传 .skill 压缩包（包含 SKILL.md、脚本、资源等）
- ✅ 灵活的类型系统（分类 + 标签 + 自定义类型）
- ✅ 自动安全检查（检测敏感文件和危险脚本）
- ✅ Web 管理界面（表单 + 上传混合模式）

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

### 1. 上传技能包

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
  -F "subCategory=vcs" \
  -F 'tags=["github","git","api"]'
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
    "fileSize": 12345,
    "checksum": "abc123...",
    "category": "dev",
    "subCategory": "vcs",
    "tags": "[\"github\",\"git\",\"api\"]",
    "enabled": true
  }
}
```

### 2. 创建纯文本技能

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

## Web 界面

### 访问上传页面

```
http://localhost:9090/skills/upload
```

### 功能特性

1. **拖拽上传**
   - 支持拖拽文件到上传区域
   - 或点击上传区域选择文件

2. **表单填写**
   - 技能 ID（可选，自动生成）
   - 技能名称（可选，从 SKILL.md 提取）
   - 技能描述（可选）
   - 分类选择（可选）
   - 标签输入（可选）

3. **实时验证**
   - 文件格式检查
   - 文件大小限制（50MB）
   - 实时进度显示

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

## 下一步开发

- [ ] Phase 2: 技能包解析和验证（进行中）
- [ ] Phase 3: Agent 技能加载
- [ ] Phase 4: 技能绑定 API
- [ ] Phase 5: 前端管理界面
- [ ] Phase 6: 测试和文档

## 测试

### 启动服务

```bash
cd luoshen-demo
mvn clean install
cd luoshen-admin
mvn spring-boot:run
```

### 测试上传

访问：`http://localhost:9090/skills/upload`

### 测试 API

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

A: 更新 SkillConfigEntity 的 `enabled` 字段为 `false`。

---

**创建时间：** 2026-04-01
**版本：** v1.0
**状态：** Phase 1 完成
