# 技能类型扩展性设计

## 🤔 你的问题非常好！

确实，固定的三种类型（tool、workflow、knowledge）有局限性：

### 现有问题

1. **类型固定**
   - 只能选择预定义的三种
   - 无法适应新的业务场景
   - 用户可能不知道选哪个

2. **语义模糊**
   - `tool` 和 `workflow` 边界不清
   - `knowledge` 定义不明确
   - 混合型技能怎么办？

3. **扩展困难**
   - 添加新类型需要改代码
   - 数据库字段是 ENUM，修改麻烦
   - 前端下拉框需要硬编码

---

## 💡 推荐方案：灵活的类型系统

### 方案 1：标签化类型（Tag-based）⭐ 推荐

#### 核心思想
不限制类型，而是让用户自由定义，通过标签和分类来组织。

#### 数据模型

```java
@Data
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    
    // ... 现有字段 ...
    
    /**
     * 技能类型（可选，不再必填）
     * 如果不知道类型，可以为空
     */
    @Column(length = 50)
    private String type;  // 改为可选字段
    
    /**
     * 技能标签（JSON 数组）
     * 用于灵活分类和搜索
     */
    @Column(columnDefinition = "TEXT")
    private String tags;  // ["github", "devops", "automation"]
    
    /**
     * 技能分类（可选）
     * 可以多级分类：devops/ci, devops/cd
     */
    @Column(length = 100)
    private String category;
    
    /**
     * 技能特性（JSON 对象）
     * 记录技能的能力特征
     */
    @Column(columnDefinition = "TEXT")
    private String features;  // {"hasScripts": true, "hasAssets": true}
}
```

#### 类型定义

**预定义类型（作为建议）：**
```javascript
const SKILL_TYPES = {
  // 开发相关
  'dev': '开发工具',
  'devops': 'DevOps',
  'testing': '测试',
  'deploy': '部署',

  // 数据处理
  'data-analysis': '数据分析',
  'data-transform': '数据转换',
  'visualization': '可视化',

  // 通信集成
  'messaging': '消息通知',
  'email': '邮件',
  'webhook': 'Webhook',

  // 文档管理
  'document': '文档处理',
  'knowledge': '知识库',
  'wiki': 'Wiki',

  // 业务领域
  'manufacturing': '制造业',
  'quality': '质量管理',
  'inventory': '库存管理',

  // AI/ML
  'ai-chat': 'AI 对话',
  'ai-vision': 'AI 视觉',
  'ai-audio': 'AI 语音',

  // 其他
  'utility': '通用工具',
  'automation': '自动化',
  'integration': '集成'
};
```

**使用方式：**
```javascript
// 用户可以选择预定义类型
<el-select v-model="skill.type">
  <el-option label="开发工具" value="dev" />
  <el-option label="DevOps" value="devops" />
  <el-option label="数据分析" value="data-analysis" />
  <!-- ... 更多选项 ... -->
  <el-option label="自定义" value="custom" />
</el-select>

// 或者自由输入
<el-input
  v-if="skill.type === 'custom'"
  v-model="skill.customType"
  placeholder="输入自定义类型"
/>
```

#### 标签系统

**标签示例：**
```javascript
// 技能可以有多个标签
skill.tags = [
  "github",
  "git",
  "devops",
  "version-control",
  "automation"
];
```

**标签的好处：**
- ✅ 多维度分类（一个技能可以有多个标签）
- ✅ 灵活搜索（按标签筛选）
- ✅ 易于扩展（随时添加新标签）
- ✅ 用户友好（比类型更直观）

**标签管理界面：**
```
┌──────────────────────────────────────────────────────────┐
│  技能标签                                  [+ 添加标签]   │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  github  [×]     git  [×]     devops  [×]                │
│                                                           │
│  [+ 添加标签]                                            │
│  ┌─────────────────────────────────────────────────┐    │
│  │ 🔍 搜索或创建标签...                             │    │
│  │                                                  │    │
│  │ 常用标签：                                       │    │
│  │ [github] [git] [api] [automation] [devops]       │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

### 方案 2：分类层级（Category Hierarchy）

#### 核心思想
使用多级分类，而不是单一类型。

#### 分类结构

```
技能分类
├── 开发 (dev)
│   ├── 版本控制 (vcs)
│   │   ├── git
│   │   └── svn
│   ├── 代码编辑 (editor)
│   └── 调试 (debug)
│
├── 运维 (ops)
│   ├── CI/CD (cicd)
│   ├── 监控 (monitoring)
│   └── 部署 (deploy)
│
├── 数据 (data)
│   ├── 分析 (analysis)
│   ├── 可视化 (visualization)
│   └── 转换 (transform)
│
└── 集成 (integration)
    ├── API (api)
    ├── 消息 (messaging)
    └── 存储 (storage)
```

#### 数据模型

```java
/**
 * 技能分类实体
 */
@Data
@Entity
@Table(name = "luoshen_skill_category")
public class SkillCategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分类代码（如：dev/git, dev/vcs/git）
     */
    @Column(unique = true, nullable = false)
    private String code;
    
    /**
     * 分类名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 父分类（支持层级）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 描述
     */
    private String description;
}
```

#### 使用示例

```javascript
// 用户选择分类（级联选择器）
<el-cascader
  v-model="skill.category"
  :options="categoryTree"
  :props="{
    value: 'code',
    label: 'name',
    children: 'children'
  }"
/>
```

**效果：**
```
用户选择：开发 → 版本控制 → Git
保存为：skill.category = "dev/vcs/git"
```

---

### 方案 3：混合模式（推荐）⭐⭐⭐

结合方案 1 和方案 2，提供最大的灵活性。

#### 数据模型

```java
@Data
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    
    // ... 现有字段 ...
    
    /**
     * 主分类（可选）
     * 一级分类，便于快速筛选
     */
    @Column(length = 50)
    private String category;  // "dev", "ops", "data", "integration"
    
    /**
     * 子分类（可选）
     * 二级分类，更细粒度
     */
    @Column(length = 50)
    private String subCategory;  // "git", "cicd", "analysis"
    
    /**
     * 技能标签（JSON 数组）
     * 多维度标记
     */
    @Column(columnDefinition = "TEXT")
    private String tags;  // ["github", "automation", "api"]
    
    /**
     * 自定义类型（可选）
     * 如果预定义类型不满足，可以自定义
     */
    @Column(length = 100)
    private String customType;
    
    /**
     * 技能能力（JSON 对象）
     * 描述技能能做什么
     */
    @Column(columnDefinition = "TEXT")
    private String capabilities;  // {"canRead": true, "canWrite": false}
}
```

#### 界面设计

```
┌──────────────────────────────────────────────────────────┐
│  新增技能                                                 │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  📋 基本信息                                             │
│  ┌─────────────────────────────────────────────────┐    │
│  │ 技能 ID *                                         │    │
│  │ [github-integration________________]              │    │
│  │                                                   │    │
│  │ 技能名称 *                                         │    │
│  │ [GitHub 集成__________________________]           │    │
│  │                                                   │    │
│  │ 技能描述 *                                         │    │
│  │ ┌──────────────────────────────────────────┐    │    │
│  │ │ 用于 GitHub 仓库操作，包括创建 issue、     │    │    │
│  │ │ 管理 PR、查询仓库信息等                   │    │    │
│  │ └──────────────────────────────────────────┘    │    │
│  │                                                   │    │
│  │ 技能分类（可选）                                   │    │
│  │ ┌──────────┐  ┌──────────┐                      │    │
│  │ │ 开发 ▼   │  │ 版本控制 ▼│  [+ 添加子分类]    │    │
│  │ └──────────┘  └──────────┘                      │    │
│  │                                                   │    │
│  │ 技能标签（可多选）                                 │    │
│  │ [github] [git] [api] [automation] [+ 添加]       │    │
│  │                                                   │    │
│  │ 自定义类型（可选）                                 │    │
│  │ 如果预定义分类不满足，可以输入自定义类型          │    │
│  │ [_______________________________]                 │    │
│  │                                                   │    │
│  │ 技能能力（自动检测）                               │    │
│  │ ☑ 包含可执行脚本                                  │    │
│  │ ☐ 包含资源文件                                    │    │
│  │ ☑ 提供 API 接口                                   │    │
│  │ ☐ 需要网络访问                                    │    │
│  │                                                   │    │
│  │ 风险等级 *                                         │    │
│  │ ○ 低   ● 中   ○ 高                                │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  📦 技能内容 ...                                          │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

## 🎯 推荐实现方案

### 综合方案（灵活 + 易用）

#### 1. 字段设计

```java
public class SkillConfigEntity {
    
    // 必填字段
    private String skillId;
    private String name;
    private String description;
    
    // 分类（可选，不限制）
    private String category;      // 主分类（可选）
    private String subCategory;   // 子分类（可选）
    
    // 标签（推荐使用）
    private String tags;          // JSON 数组，多标签
    
    // 自定义类型（兜底）
    private String customType;    // 用户自定义
    
    // 自动检测的能力
    private String capabilities;  // JSON 对象，自动生成
    
    // 风险等级
    private String riskLevel;
}
```

#### 2. 预定义分类（作为建议）

```javascript
// 系统提供的预定义分类
const PREDEFINED_CATEGORIES = {
  'dev': {
    name: '开发',
    subCategories: ['vcs', 'editor', 'debug', 'testing']
  },
  'ops': {
    name: '运维',
    subCategories: ['cicd', 'monitoring', 'deploy']
  },
  'data': {
    name: '数据',
    subCategories: ['analysis', 'visualization', 'transform']
  },
  'integration': {
    name: '集成',
    subCategories: ['api', 'messaging', 'storage']
  }
};

// 系统提供的常用标签
const COMMON_TAGS = [
  'github', 'git', 'api',
  'automation', 'script',
  'webhook', 'notification',
  'database', 'cache',
  'monitoring', 'logging'
];
```

#### 3. 界面交互

**场景 1：不知道类型怎么办？**

```
用户：我不知道这个技能应该选什么类型
系统：没关系，可以：
  1. 留空（不填分类）
  2. 只填标签（如：github, automation）
  3. 输入自定义类型（如：github-helper）
系统会根据技能包内容自动建议分类和标签
```

**场景 2：混合型技能**

```
技能：GitHub 集成
分类：开发 → 版本控制
标签：[github, git, api, automation, devops]
能力：{"hasScripts": true, "hasApi": true, "needsNetwork": true}
```

**场景 3：完全自定义**

```
技能：公司内部 ERP 集成
分类：（留空）
自定义类型：erp-integration
标签：[internal, erp, company-specific]
```

---

## 🔍 搜索和筛选

### 按分类筛选

```javascript
// 筛选开发类技能
GET /api/admin/skills?category=dev

// 筛选版本控制类技能
GET /api/admin/skills?category=dev&subCategory=vcs
```

### 按标签筛选

```javascript
// 筛选包含 github 标签的技能
GET /api/admin/skills?tags=github

// 筛选同时包含 github 和 api 标签的技能
GET /api/admin/skills?tags=github,api

// 搜索标签或名称包含 "git" 的技能
GET /api/admin/skills?search=git
```

### 按能力筛选

```javascript
// 筛选包含脚本的技能
GET /api/admin/skills?hasScripts=true

// 筛选需要网络的技能
GET /api/admin/skills?needsNetwork=true
```

---

## 📊 数据库查询

```sql
-- 查询所有开发类技能
SELECT * FROM luoshen_skill_config
WHERE category = 'dev';

-- 查询包含特定标签的技能（JSON 查询）
SELECT * FROM luoshen_skill_config
WHERE JSON_CONTAINS(tags, '"github"');

-- 查询有脚本的技能
SELECT * FROM luoshen_skill_config
WHERE JSON_EXTRACT(capabilities, '$.hasScripts') = true;

-- 全文搜索（名称或描述）
SELECT * FROM luoshen_skill_config
WHERE name LIKE '%git%' OR description LIKE '%git%';
```

---

## ✅ 最终建议

### 推荐方案：**混合模式**

1. **分类可选**
   - 不强制要求填写分类
   - 提供预定义分类作为建议
   - 支持自定义分类

2. **标签推荐**
   - 鼓励使用标签（更灵活）
   - 提供常用标签列表
   - 支持自定义标签

3. **自动检测**
   - 系统自动分析技能包
   - 建议分类和标签
   - 用户可以调整

4. **能力标记**
   - 自动检测技能特性
   - hasScripts、hasAssets、needsNetwork 等
   - 用于筛选和展示

### 界面优化

```
┌──────────────────────────────────────────────────────────┐
│  技能类型（三选一）                                       │
│                                                           │
│  ○ 使用预定义分类（推荐）                                 │
│     ┌──────────┐  ┌──────────┐                          │
│     │ 开发 ▼   │  │ 版本控制 ▼│                          │
│     └──────────┘  └──────────┘                          │
│                                                           │
│  ○ 只用标签分类（灵活）                                   │
│     [github] [git] [api] [+ 添加标签]                    │
│                                                           │
│  ○ 自定义类型（完全自由）                                 │
│     [输入自定义类型________________]                     │
│                                                           │
│  💡 系统建议：                                           │
│     根据技能包内容，建议使用"开发 → 版本控制"             │
│     并添加标签：[github] [git] [api]                     │
│     [采纳建议]                                           │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

这样既保证了灵活性，又提供了良好的引导！你觉得这个方案怎么样？
