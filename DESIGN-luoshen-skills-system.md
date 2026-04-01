# 洛神平台 - Skills 系统增强设计

## 项目背景

洛神平台是基于 AgentScope-Java 的多智能体协作平台，目前已实现：
- ✅ 多级 Agent 架构（Leader → Core → Sub）
- ✅ 动态 Agent 配置（luoshen-admin 模块）
- ✅ Skill 配置存储（数据库）

需要增强的功能：
- 📦 **技能包上传** - 支持上传 .skill 压缩包（包含脚本、资源等）
- 🎯 **Agent 技能绑定** - 每个 Agent 可以指定自己的技能列表

## 现有架构分析

### 数据模型

```java
// SkillConfigEntity - 技能配置实体
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    private Long id;
    private String skillId;        // 技能唯一标识
    private String name;           // 技能名称
    private String description;    // 技能描述
    private String content;        // 技能内容（Markdown）
    private String type;           // tool, workflow, knowledge
    private String agentId;        // 所属 Agent（可选）
    private Boolean enabled;       // 是否启用
    private String version;        // 版本号
    // ...
}

// AgentConfigEntity - Agent 配置实体
@Entity
@Table(name = "luoshen_agent_config")
public class AgentConfigEntity {
    private Long id;
    private String agentId;        // Agent 唯一标识
    private String name;           // Agent 名称
    private String systemPrompt;   // 系统提示词
    private String type;           // main, sub
    private Boolean enabled;       // 是否启用
    // ...
}
```

### 现有问题

1. **Skill 只存储文本内容** - 无法包含脚本、资源文件
2. **Agent 和 Skill 关联弱** - agentId 字段不够灵活
3. **没有技能包上传** - 只能通过 API 创建文本 Skill

## 设计方案

### 1. 技能包存储结构

```
~/.luoshen/
├── skills/                     # 技能包存储目录
│   ├── builtin/                # 内置技能（只读）
│   │   ├── github/
│   │   │   ├── SKILL.md
│   │   │   ├── scripts/
│   │   │   └── references/
│   │   └── obsidian/
│   │
│   └── user/                   # 用户技能（可管理）
│       ├── my-skill/
│       │   ├── SKILL.md
│       │   ├── scripts/
│       │   │   └── helper.py
│       │   ├── references/
│       │   │   └── api-docs.md
│       │   └── assets/
│       │       └── template.png
│       └── another-skill/
│           └── SKILL.md
│
├── packages/                   # 上传的技能包临时存储
│   └── uploads/
│
└── index.json                  # 技能索引文件
```

### 2. 数据模型扩展

#### 2.1 新增 AgentSkillRelation 实体

```java
/**
 * Agent 技能关联实体
 * 
 * 定义 Agent 和 Skill 的多对多关系
 */
@Data
@Entity
@Table(name = "luoshen_agent_skill_relation")
public class AgentSkillRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Agent ID
     */
    @Column(nullable = false)
    private String agentId;
    
    /**
     * Skill ID
     */
    @Column(nullable = false)
    private String skillId;
    
    /**
     * 绑定模式: inherit, custom, none
     * - inherit: 继承父级技能
     * - custom: 使用自定义技能列表
     * - none: 禁用所有技能
     */
    @Column(nullable = false)
    private String bindMode = "custom";
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 优先级（数字越小优先级越高）
     * 用于控制技能加载顺序
     */
    private Integer priority = 0;
    
    /**
     * 配置参数（JSON 格式）
     * 用于存储技能特定的配置
     */
    @Column(columnDefinition = "TEXT")
    private String config;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### 2.2 扩展 SkillConfigEntity

```java
@Data
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    
    // ... 现有字段 ...
    
    /**
     * 技能包路径（本地文件系统路径）
     * 如果是压缩包上传的技能，存储解压后的目录路径
     */
    @Column(name = "package_path")
    private String packagePath;
    
    /**
     * 技能来源: builtin, user_upload, marketplace
     */
    @Column(nullable = false)
    private String source = "user_upload";
    
    /**
     * 风险等级: low, medium, high
     */
    @Column(nullable = false)
    private String riskLevel = "medium";
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 校验和（用于验证完整性）
     */
    private String checksum;
    
    /**
     * 依赖的其他技能（JSON 数组）
     */
    @Column(columnDefinition = "TEXT")
    private String dependencies;
    
    /**
     * 是否包含可执行脚本
     */
    private Boolean hasScripts = false;
    
    /**
     * 脚本列表（JSON 数组，相对路径）
     */
    @Column(columnDefinition = "TEXT")
    private String scriptFiles;
}
```

### 3. API 设计

#### 3.1 技能包上传 API

```java
@RestController
@RequestMapping("/api/admin/skills")
public class SkillManagementController {
    
    /**
     * 上传技能包
     * 
     * @param file 技能包文件（.skill 或 .zip）
     * @param skillId 技能 ID（可选，自动生成）
     * @param override 是否覆盖已存在的技能
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<SkillUploadResponse> uploadSkillPackage(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "skillId", required = false) String skillId,
        @RequestParam(value = "override", defaultValue = "false") Boolean override
    ) {
        // 1. 验证文件格式
        // 2. 解压到临时目录
        // 3. 验证 SKILL.md
        // 4. 安全检查
        // 5. 移动到目标位置
        // 6. 更新数据库
        // 7. 返回结果
    }
    
    /**
     * 下载技能包
     * 
     * @param skillId 技能 ID
     * @return 技能包文件
     */
    @GetMapping("/{skillId}/download")
    public ResponseEntity<Resource> downloadSkillPackage(@PathVariable String skillId) {
        // 打包成 ZIP 并返回
    }
    
    /**
     * 验证技能包（不上传）
     * 
     * @param file 技能包文件
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<SkillValidationResult> validateSkillPackage(
        @RequestParam("file") MultipartFile file
    ) {
        // 验证但不安装
    }
}
```

#### 3.2 Agent 技能绑定 API

```java
@RestController
@RequestMapping("/api/admin/agents")
public class AgentManagementController {
    
    /**
     * 为 Agent 绑定技能
     * 
     * @param agentId Agent ID
     * @param request 绑定请求
     * @return 绑定结果
     */
    @PostMapping("/{agentId}/skills/bind")
    public ResponseEntity<AgentSkillRelation> bindSkill(
        @PathVariable String agentId,
        @RequestBody BindSkillRequest request
    ) {
        // {
        //   "skillId": "my-skill",
        //   "bindMode": "custom",
        //   "enabled": true,
        //   "priority": 0,
        //   "config": {}
        // }
    }
    
    /**
     * 解绑 Agent 的技能
     * 
     * @param agentId Agent ID
     * @param skillId 技能 ID
     * @return 操作结果
     */
    @DeleteMapping("/{agentId}/skills/{skillId}")
    public ResponseEntity<Void> unbindSkill(
        @PathVariable String agentId,
        @PathVariable String skillId
    ) {
        // 删除关联
    }
    
    /**
     * 获取 Agent 的所有技能
     * 
     * @param agentId Agent ID
     * @return 技能列表
     */
    @GetMapping("/{agentId}/skills")
    public List<SkillConfigEntity> getAgentSkills(@PathVariable String agentId) {
        // 查询并返回
    }
    
    /**
     * 批量绑定技能
     * 
     * @param agentId Agent ID
     * @param request 批量绑定请求
     * @return 绑定结果
     */
    @PostMapping("/{agentId}/skills/batch")
    public List<AgentSkillRelation> bindSkillsBatch(
        @PathVariable String agentId,
        @RequestBody BatchBindSkillsRequest request
    ) {
        // {
        //   "skillIds": ["skill1", "skill2"],
        //   "bindMode": "custom"
        // }
    }
}
```

### 4. 技能加载器

#### 4.1 AgentSkillLoader 服务

```java
@Service
public class AgentSkillLoader {
    
    @Autowired
    private SkillConfigRepository skillConfigRepository;
    
    @Autowired
    private AgentSkillRelationRepository relationRepository;
    
    @Autowired
    private FileSystemSkillRepository fileSystemSkillRepository;
    
    /**
     * 为 Agent 加载技能
     * 
     * @param agentId Agent ID
     * @return 技能列表
     */
    public List<AgentSkill> loadSkillsForAgent(String agentId) {
        // 1. 查询 Agent 的技能绑定关系
        List<AgentSkillRelation> relations = relationRepository
            .findByAgentIdAndEnabled(agentId, true);
        
        // 2. 按 bindMode 分组
        Map<String, List<AgentSkillRelation>> grouped = relations.stream()
            .collect(Collectors.groupingBy(AgentSkillRelation::getBindMode));
        
        List<AgentSkill> skills = new ArrayList<>();
        
        // 3. 处理 inherit 模式（递归获取父级技能）
        if (grouped.containsKey("inherit")) {
            skills.addAll(loadInheritSkills(agentId));
        }
        
        // 4. 处理 custom 模式（加载指定技能）
        if (grouped.containsKey("custom")) {
            List<String> skillIds = grouped.get("custom").stream()
                .map(AgentSkillRelation::getSkillId)
                .toList();
            skills.addAll(loadCustomSkills(skillIds));
        }
        
        // 5. 按优先级排序
        skills.sort(Comparator.comparingInt(AgentSkill::getPriority));
        
        return skills;
    }
    
    /**
     * 加载自定义技能列表
     */
    private List<AgentSkill> loadCustomSkills(List<String> skillIds) {
        List<AgentSkill> skills = new ArrayList<>();
        
        for (String skillId : skillIds) {
            // 从数据库查询技能配置
            SkillConfigEntity config = skillConfigRepository
                .findBySkillId(skillId)
                .orElse(null);
            
            if (config == null || !config.getEnabled()) {
                continue;
            }
            
            // 如果有技能包路径，从文件系统加载
            if (config.getPackagePath() != null) {
                AgentSkill skill = loadSkillFromPackage(config);
                if (skill != null) {
                    skills.add(skill);
                }
            } else {
                // 从内容加载
                AgentSkill skill = loadSkillFromContent(config);
                if (skill != null) {
                    skills.add(skill);
                }
            }
        }
        
        return skills;
    }
    
    /**
     * 从技能包加载
     */
    private AgentSkill loadSkillFromPackage(SkillConfigEntity config) {
        try {
            Path skillPath = Path.of(config.getPackagePath());
            
            // 使用 AgentScope 的 FileSystemSkillRepository 加载
            return fileSystemSkillRepository.getSkill(config.getSkillId());
            
        } catch (Exception e) {
            log.error("加载技能包失败: {}", config.getSkillId(), e);
            return null;
        }
    }
}
```

#### 4.2 动态 Agent 创建时的技能注入

```java
@Service
public class AgentDynamicService {
    
    @Autowired
    private AgentSkillLoader skillLoader;
    
    /**
     * 动态创建 Agent（增强版）
     */
    public ReActAgent createAgent(String agentId) {
        // 1. 查询 Agent 配置
        AgentConfigEntity config = agentConfigRepository
            .findByAgentId(agentId)
            .orElseThrow();
        
        // 2. 加载 Agent 的技能
        List<AgentSkill> skills = skillLoader.loadSkillsForAgent(agentId);
        
        // 3. 创建 SkillBox
        SkillBox skillBox = new SkillBox(toolkit);
        for (AgentSkill skill : skills) {
            skillBox.registration().skill(skill).apply();
        }
        
        // 4. 创建 Agent
        return ReActAgent.builder()
            .name(agentId)
            .sysPrompt(config.getSystemPrompt())
            .model(model)
            .toolkit(toolkit)
            .skillBox(skillBox)
            .memory(new InMemoryMemory())
            .build();
    }
}
```

### 5. 技能包上传流程

```
用户上传 ZIP 文件
      │
      ▼
验证文件格式（.skill 或 .zip）
      │
      ▼
解压到临时目录 (~/.luoshen/packages/uploads/temp_xxx/)
      │
      ▼
验证 SKILL.md
  - 检查必需字段（name, description）
  - 验证 YAML frontmatter 格式
      │
      ▼
安全检查
  - 检测敏感文件（.ssh, token, password）
  - 扫描脚本中的危险操作（rm -rf, eval）
  - 检查文件大小限制
      │
      ▼
提取元数据
  - 读取 SKILL.md 内容
  - 扫描脚本文件
  - 计算校验和
      │
      ▼
移动到目标目录 (~/.luoshen/skills/user/{skill-id}/)
      │
      ▼
保存到数据库
  - 插入 luoshen_skill_config 表
      │
      ▼
更新索引文件 (~/.luoshen/index.json)
      │
      ▼
返回成功响应
```

### 6. 安全检查实现

```java
@Service
public class SkillSecurityValidator {
    
    /**
     * 验证技能包
     */
    public ValidationResult validateSkillPackage(Path tempDir) {
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 1. 检查必需文件
        Path skillMd = tempDir.resolve("SKILL.md");
        if (!Files.exists(skillMd)) {
            errors.add("缺少必需的 SKILL.md 文件");
            return result.withErrors(errors);
        }
        
        // 2. 验证 SKILL.md 格式
        try {
            String content = Files.readString(skillMd);
            SkillMetadata metadata = parseSkillMetadata(content);
            if (metadata.getName() == null || metadata.getDescription() == null) {
                errors.add("SKILL.md 缺少必需的 name 或 description 字段");
            }
        } catch (Exception e) {
            errors.add("SKILL.md 格式错误: " + e.getMessage());
        }
        
        // 3. 安全检查
        errors.addAll(checkSecurityIssues(tempDir));
        
        // 4. 警告检查
        warnings.addAll(checkWarnings(tempDir));
        
        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        result.setWarnings(warnings);
        
        return result;
    }
    
    /**
     * 安全检查
     */
    private List<String> checkSecurityIssues(Path dir) {
        List<String> issues = new ArrayList<>();
        
        try {
            // 检测敏感文件
            List<String> sensitivePatterns = List.of(
                ".ssh", ".aws", "token", "password", "secret", "key"
            );
            
            Files.walk(dir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    for (String pattern : sensitivePatterns) {
                        if (fileName.contains(pattern)) {
                            issues.add("检测到敏感文件: " + file);
                        }
                    }
                });
            
            // 检测脚本中的危险操作
            List<Path> scripts = findScripts(dir);
            for (Path script : scripts) {
                String content = Files.readString(script);
                
                // 危险模式
                List<Pattern> dangerousPatterns = List.of(
                    Pattern.compile("rm\\s+-rf"),
                    Pattern.compile("eval\\s*\\("),
                    Pattern.compile("exec\\s*\\("),
                    Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec")
                );
                
                for (Pattern pattern : dangerousPatterns) {
                    if (pattern.matcher(content).find()) {
                        issues.add("脚本包含危险操作: " + script);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("安全检查失败", e);
        }
        
        return issues;
    }
    
    /**
     * 警告检查
     */
    private List<String> checkWarnings(Path dir) {
        List<String> warnings = new ArrayList<>();
        
        try {
            // 检查大文件
            Files.walk(dir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        long size = Files.size(file);
                        if (size > 10 * 1024 * 1024) { // 10MB
                            warnings.add(String.format(
                                "包含大文件: %s (%.2f MB)",
                                file, size / 1024.0 / 1024.0
                            ));
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                });
            
            // 检查多余文档
            if (Files.exists(dir.resolve("README.md"))) {
                warnings.add("技能包不应包含 README.md（说明应放在 SKILL.md 中）");
            }
            
        } catch (Exception e) {
            log.error("警告检查失败", e);
        }
        
        return warnings;
    }
    
    /**
     * 查找所有脚本文件
     */
    private List<Path> findScripts(Path dir) throws IOException {
        List<String> scriptExtensions = List.of(
            ".sh", ".bash", ".py", ".js", ".ts", ".java", ".go", ".rs"
        );
        
        return Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(file -> {
                String fileName = file.getFileName().toString().toLowerCase();
                return scriptExtensions.stream().anyMatch(fileName::endsWith);
            })
            .toList();
    }
}
```

### 7. 前端界面（Vue.js）

#### 7.1 技能管理页面

```vue
<template>
  <div class="skill-management">
    <!-- 上传区域 -->
    <div class="upload-section">
      <el-upload
        :action="uploadUrl"
        :on-success="handleUploadSuccess"
        :before-upload="beforeUpload"
        accept=".skill,.zip"
        :show-file-list="false"
      >
        <el-button type="primary">上传技能包</el-button>
      </el-upload>
    </div>
    
    <!-- 技能列表 -->
    <el-table :data="skills" style="width: 100%">
      <el-table-column prop="name" label="技能名称" />
      <el-table-column prop="skillId" label="技能 ID" />
      <el-table-column prop="source" label="来源">
        <template #default="scope">
          <el-tag :type="scope.row.source === 'builtin' ? 'info' : 'success'">
            {{ scope.row.source === 'builtin' ? '内置' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riskLevel" label="风险等级">
        <template #default="scope">
          <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
            {{ scope.row.riskLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态">
        <template #default="scope">
          <el-switch v-model="scope.row.enabled" @change="toggleSkill(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="viewSkill(scope.row)">查看</el-button>
          <el-button size="small" @click="downloadSkill(scope.row)">下载</el-button>
          <el-button size="small" type="danger" @click="deleteSkill(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

#### 7.2 Agent 技能绑定页面

```vue
<template>
  <div class="agent-skills">
    <!-- Agent 选择 -->
    <el-select v-model="selectedAgentId" @change="loadAgentSkills">
      <el-option
        v-for="agent in agents"
        :key="agent.agentId"
        :label="agent.name"
        :value="agent.agentId"
      />
    </el-select>
    
    <!-- 技能绑定 -->
    <div class="skill-binding">
      <h3>可用技能</h3>
      <el-transfer
        v-model="boundSkillIds"
        :data="availableSkills"
        :props="{
          key: 'skillId',
          label: 'name'
        }"
        @change="handleSkillChange"
      />
    </div>
    
    <!-- 已绑定技能列表 -->
    <div class="bound-skills">
      <h3>已绑定技能</h3>
      <el-table :data="boundSkills">
        <el-table-column prop="name" label="技能名称" />
        <el-table-column prop="bindMode" label="绑定模式" />
        <el-table-column prop="priority" label="优先级" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="unbindSkill(scope.row)">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
```

### 8. 实现计划

| 阶段 | 任务 | 预计时间 |
|------|------|----------|
| **Phase 1** | 数据模型扩展 | 1 天 |
| - | 创建 AgentSkillRelation 实体 | |
| - | 扩展 SkillConfigEntity | |
| - | 创建 Repository | |
| **Phase 2** | 技能包上传功能 | 2 天 |
| - | 实现上传 API | |
| - | 实现安全检查 | |
| - | 实现文件管理 | |
| **Phase 3** | Agent 技能加载 | 2 天 |
| - | 实现 AgentSkillLoader | |
| - | 集成到 AgentDynamicService | |
| - | 测试技能绑定 | |
| **Phase 4** | 前端界面 | 2 天 |
| - | 技能管理页面 | |
| - | Agent 技能绑定页面 | |
| - | API 对接 | |
| **Phase 5** | 测试和文档 | 1 天 |
| - | 集成测试 | |
| - | 编写文档 | |
| **总计** | | **8 天** |

### 9. 技术栈

- **后端**: Spring Boot 3.2.0, JPA, H2/MySQL
- **前端**: Vue.js 3, Element Plus
- **文件处理**: Apache Commons Compress
- **验证**: Spring Boot Validation
- **日志**: SLF4J + Logback

### 10. 依赖添加

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Apache Commons Compress（用于 ZIP 处理） -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-compress</artifactId>
        <version>1.25.0</version>
    </dependency>
    
    <!-- 文件上传 -->
    <dependency>
        <groupId>commons-fileupload</groupId>
        <artifactId>commons-fileupload</artifactId>
        <version>1.5</version>
    </dependency>
    
    <!-- 校验和计算 -->
    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
        <version>1.16.0</version>
    </dependency>
</dependencies>
```

## 总结

这个设计方案为洛神平台提供了完整的技能包管理能力：

✅ **技能包上传** - 支持上传包含脚本、资源的 .skill 压缩包
✅ **安全检查** - 自动检测危险文件和操作
✅ **Agent 技能绑定** - 灵活的多对多关联关系
✅ **动态加载** - 创建 Agent 时自动注入技能
✅ **可视化管理** - Web 界面管理技能和绑定关系

---

**创建时间**: 2026-04-01
**适用项目**: luoshen-demo (洛神平台)
**状态**: 设计方案 v1.0
