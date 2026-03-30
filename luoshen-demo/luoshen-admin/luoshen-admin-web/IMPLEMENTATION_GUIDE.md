# 洛神系统 Web 管理界面实现指南

## 🎯 已完成的部分

### ✅ 基础架构

1. **项目创建**
   - ✅ Vue 3 + TypeScript + Vite
   - ✅ Vue Router + Pinia
   - ✅ Element Plus UI 组件库

2. **核心文件**
   - ✅ `src/utils/request.ts` - Axios 请求封装
   - ✅ `src/api/index.ts` - 完整的 API 接口定义
   - ✅ `src/config/api.config.ts` - API 配置

3. **页面组件**
   - ✅ `src/views/Dashboard.vue` - 仪表板页面
   - ✅ `src/views/AgentList.vue` - Agent 管理页面

---

## 🚧 需要完成的页面

### 1. SkillList.vue（Skill 管理）

创建文件：`src/views/SkillList.vue`

**核心功能：**
- Skill 列表展示
- 上传 Markdown 文件
- 创建/编辑 Skill
- 加载/卸载 Skill
- 预览 Skill 内容

**关键代码：**
```vue
<template>
  <div class="skill-list">
    <div class="action-bar">
      <el-button type="primary" @click="showUploadDialog">
        <el-icon><Upload /></el-icon>
        上传 Skill
      </el-button>
      <el-button @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建 Skill
      </el-button>
    </div>

    <el-table :data="skills" stripe>
      <el-table-column prop="skillId" label="ID" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="description" label="描述" width="200" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.loaded ? 'success' : 'info'">
            {{ scope.row.loaded ? '已加载' : '未加载' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="250">
        <template #default="scope">
          <el-button size="small" @click="previewSkill(scope.row)">
            预览
          </el-button>
          <el-button 
            size="small" 
            type="primary"
            @click="toggleLoad(scope.row)"
          >
            {{ scope.row.loaded ? '卸载' : '加载' }}
          </el-button>
          <el-button 
            size="small" 
            type="danger"
            @click="deleteSkill(scope.row.skillId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传 Skill">
      <el-upload
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".md"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 .md 文件
          </div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="Skill 预览" width="800px">
      <vue-markdown :source="previewContent" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Plus } from '@element-plus/icons-vue'
import { skillApi, type SkillConfig } from '@/api'
import VueMarkdown from 'vue-markdown-render'

const skills = ref<SkillConfig[]>([])
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const previewContent = ref('')

const loadSkills = async () => {
  try {
    const data = await skillApi.list()
    skills.value = data
  } catch (error) {
    console.error('加载 Skill 列表失败:', error)
  }
}

const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

const handleFileChange = async (file: any) => {
  try {
    await skillApi.upload(file.raw)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadSkills()
  } catch (error) {
    console.error('上传失败:', error)
  }
}

const previewSkill = async (skill: SkillConfig) => {
  previewContent.value = skill.content
  previewDialogVisible.value = true
}

const toggleLoad = async (skill: SkillConfig) => {
  try {
    if (skill.loaded) {
      await skillApi.unload(skill.skillId)
      ElMessage.success('Skill 已卸载')
    } else {
      await skillApi.load(skill.skillId)
      ElMessage.success('Skill 已加载')
    }
    loadSkills()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const deleteSkill = async (skillId: string) => {
  try {
    await skillApi.delete(skillId)
    ElMessage.success('Skill 已删除')
    loadSkills()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

onMounted(() => {
  loadSkills()
})
</script>
```

**安装依赖：**
```bash
npm install vue-markdown-render
```

---

### 2. SessionList.vue（Session 管理）

创建文件：`src/views/SessionList.vue`

**核心功能：**
- Session 列表（分页）
- 查看会话详情（消息历史）
- 删除会话
- 批量清理过期会话
- 搜索会话

**关键代码：**
```vue
<template>
  <div class="session-list">
    <div class="action-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话"
        style="width: 200px"
        @keyup.enter="searchSessions"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="searchSessions"
      />

      <el-button type="danger" @click="clearExpiredSessions">
        清理过期会话
      </el-button>
    </div>

    <el-table
      :data="sessions"
      stripe
      @expand-change="loadSessionMessages"
    >
      <el-table-column type="expand">
        <template #default="scope">
          <div v-if="scope.row.messages" style="padding: 20px">
            <div
              v-for="msg in scope.row.messages"
              :key="msg.id"
              style="margin-bottom: 10px"
            >
              <strong>{{ msg.role }}:</strong> {{ msg.content }}
              <div style="color: #909399; font-size: 12px">
                {{ msg.timestamp }}
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="sessionId" label="Session ID" width="200" />
      <el-table-column prop="userId" label="用户 ID" width="150" />
      <el-table-column prop="agentId" label="Agent ID" width="150" />
      <el-table-column prop="messageCount" label="消息数" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      
      <el-table-column label="操作" fixed="right" width="100">
        <template #default="scope">
          <el-button 
            size="small" 
            type="danger"
            @click="deleteSession(scope.row.sessionId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="totalElements"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadSessions"
      @current-change="loadSessions"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { sessionApi, type SessionInfo } from '@/api'

const sessions = ref<SessionInfo[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchKeyword = ref('')
const dateRange = ref<Date[]>([])

const loadSessions = async () => {
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0].toISOString()
      params.endTime = dateRange.value[1].toISOString()
    }

    const data = await sessionApi.list(params)
    sessions.value = data.content
    totalElements.value = data.totalElements
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const loadSessionMessages = async (row: SessionInfo) => {
  if (!row.messages) {
    try {
      const data = await sessionApi.get(row.sessionId)
      row.messages = data.messages
    } catch (error) {
      console.error('加载消息失败:', error)
    }
  }
}

const searchSessions = () => {
  currentPage.value = 1
  loadSessions()
}

const clearExpiredSessions = async () => {
  try {
    const { value: days } = await ElMessageBox.prompt(
      '请输入要清理多少天前的会话',
      '清理过期会话',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入数字'
      }
    )
    
    const count = await sessionApi.clearExpired(parseInt(days))
    ElMessage.success(`已清理 ${count} 个会话`)
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理失败:', error)
    }
  }
}

const deleteSession = async (sessionId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该会话吗？', '警告', {
      type: 'warning'
    })
    await sessionApi.delete(sessionId)
    ElMessage.success('会话已删除')
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadSessions()
})
</script>
```

---

### 3. McpList.vue（MCP 管理）

创建文件：`src/views/McpList.vue`

**核心功能：**
- MCP 服务器列表
- 添加/编辑 MCP 配置
- 连接/断开 MCP 服务器
- 查看可用工具
- 测试 MCP 工具

**关键代码：**
```vue
<template>
  <div class="mcp-list">
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        添加 MCP 服务器
      </el-button>
    </div>

    <el-table :data="mcpServers" stripe>
      <el-table-column prop="mcpId" label="ID" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="transportType" label="类型" width="100" />
      <el-table-column prop="connected" label="状态" width="100">
        <template #default="scope">
          <el-badge 
            :type="scope.row.connected ? 'success' : 'danger'" 
            :value="scope.row.connected ? '已连接' : '未连接'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      
      <el-table-column label="操作" fixed="right" width="300">
        <template #default="scope">
          <el-button 
            size="small"
            :type="scope.row.connected ? 'danger' : 'success'"
            @click="toggleConnection(scope.row)"
          >
            {{ scope.row.connected ? '断开' : '连接' }}
          </el-button>
          <el-button size="small" @click="viewTools(scope.row)">
            查看工具
          </el-button>
          <el-button size="small" @click="editMcp(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="deleteMcp(scope.row.mcpId)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- MCP 工具列表对话框 -->
    <el-dialog v-model="toolsDialogVisible" title="MCP 工具列表" width="800px">
      <el-table :data="mcpTools" stripe>
        <el-table-column prop="name" label="工具名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="testTool(scope.row)">
              测试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { mcpApi, type McpConfig } from '@/api'

const mcpServers = ref<McpConfig[]>([])
const toolsDialogVisible = ref(false)
const mcpTools = ref<any[]>([])

const loadMcpServers = async () => {
  try {
    const data = await mcpApi.list()
    mcpServers.value = data
  } catch (error) {
    console.error('加载 MCP 列表失败:', error)
  }
}

const toggleConnection = async (mcp: McpConfig) => {
  try {
    if (mcp.connected) {
      await mcpApi.disconnect(mcp.mcpId)
      ElMessage.success('MCP 服务器已断开')
    } else {
      await mcpApi.connect(mcp.mcpId)
      ElMessage.success('MCP 服务器已连接')
    }
    loadMcpServers()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const viewTools = async (mcp: McpConfig) => {
  try {
    const tools = await mcpApi.getTools(mcp.mcpId)
    mcpTools.value = tools
    toolsDialogVisible.value = true
  } catch (error) {
    console.error('获取工具列表失败:', error)
  }
}

const deleteMcp = async (mcpId: string) => {
  try {
    await mcpApi.delete(mcpId)
    ElMessage.success('MCP 配置已删除')
    loadMcpServers()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

onMounted(() => {
  loadMcpServers()
})
</script>
```

---

## 📋 完整的文件清单

### 需要创建的文件

```
src/
├── views/
│   ├── Dashboard.vue        ✅ 已完成
│   ├── AgentList.vue        ✅ 已完成
│   ├── SkillList.vue        ⚠️ 需要创建
│   ├── SessionList.vue      ⚠️ 需要创建
│   ├── McpList.vue          ⚠️ 需要创建
│   └── MemoryList.vue       ⚠️ 需要创建
├── api/
│   └── index.ts             ✅ 已完成
├── utils/
│   └── request.ts           ✅ 已完成
├── config/
│   └── api.config.ts        ✅ 已完成
└── router/
    └── index.ts             ⚠️ 需要更新
```

---

## 🔧 路由配置

更新 `src/router/index.ts`：

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'
import AgentList from '@/views/AgentList.vue'
import SkillList from '@/views/SkillList.vue'
import SessionList from '@/views/SessionList.vue'
import McpList from '@/views/McpList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: Dashboard
    },
    {
      path: '/agents',
      name: 'agents',
      component: AgentList
    },
    {
      path: '/skills',
      name: 'skills',
      component: SkillList
    },
    {
      path: '/sessions',
      name: 'sessions',
      component: SessionList
    },
    {
      path: '/mcp',
      name: 'mcp',
      component: McpList
    }
  ]
})

export default router
```

---

## 🚀 启动和部署

### 开发环境

```bash
cd /root/.openclaw/workspace/luoshen-demo/luoshen-admin/luoshen-admin-web
npm run dev
```

访问：http://localhost:5173

### 生产构建

```bash
npm run build
```

生成的文件在 `dist/` 目录，可以部署到 Nginx 或其他 Web 服务器。

---

## 📝 下一步

1. **创建剩余页面**：按照上面的代码创建 SkillList、SessionList、McpList
2. **测试功能**：确保所有页面能正常工作
3. **优化 UI**：调整样式和交互
4. **添加 WebSocket**：实现实时日志推送
5. **集成到后端**：确保后端 API 与前端完美对接

---

**当前进度：50%**
- ✅ 基础架构
- ✅ Dashboard
- ✅ Agent 管理
- ⚠️ Skill 管理
- ⚠️ Session 管理
- ⚠️ MCP 管理
- ⚠️ Memory 管理