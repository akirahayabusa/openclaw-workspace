<template>
  <div class="mcp-list">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        添加 MCP 服务器
      </el-button>
      <el-button @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- MCP 列表 -->
    <el-table :data="mcpServers" stripe v-loading="loading">
      <el-table-column prop="mcpId" label="ID" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="transportType" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.transportType === 'stdio' ? 'primary' : 'success'">
            {{ scope.row.transportType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="connected" label="连接状态" width="100">
        <template #default="scope">
          <el-badge 
            :type="scope.row.connected ? 'success' : 'danger'" 
            :value="scope.row.connected ? '已连接' : '未连接'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="command" label="命令/URL" width="200">
        <template #default="scope">
          {{ scope.row.transportType === 'stdio' ? scope.row.command : scope.row.url }}
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
            工具列表
          </el-button>
          <el-button size="small" @click="editMcp(scope.row)">
            编辑
          </el-button>
          <el-button 
            size="small" 
            type="danger"
            @click="deleteMcp(scope.row.mcpId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑 MCP 服务器' : '添加 MCP 服务器'"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="MCP ID" prop="mcpId">
          <el-input 
            v-model="formData.mcpId" 
            :disabled="isEdit"
            placeholder="唯一标识符"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="MCP 服务器名称" />
        </el-form-item>
        <el-form-item label="传输类型" prop="transportType">
          <el-radio-group v-model="formData.transportType">
            <el-radio label="stdio">STDIO</el-radio>
            <el-radio label="websocket">WebSocket</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- STDIO 配置 -->
        <template v-if="formData.transportType === 'stdio'">
          <el-form-item label="命令" prop="command">
            <el-input v-model="formData.command" placeholder="可执行命令" />
          </el-form-item>
          <el-form-item label="参数">
            <el-select
              v-model="formData.args"
              multiple
              filterable
              allow-create
              placeholder="命令行参数"
            >
            </el-select>
          </el-form-item>
          <el-form-item label="环境变量">
            <el-input
              v-model="envJson"
              type="textarea"
              :rows="4"
              placeholder='{"KEY": "value"}'
            />
          </el-form-item>
        </template>
        
        <!-- WebSocket 配置 -->
        <template v-else>
          <el-form-item label="WebSocket URL" prop="url">
            <el-input v-model="formData.url" placeholder="ws://localhost:8080" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- MCP 工具列表对话框 -->
    <el-dialog v-model="toolsDialogVisible" title="MCP 工具列表" width="900px">
      <el-table :data="mcpTools" stripe>
        <el-table-column prop="name" label="工具名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="inputSchema" label="输入参数" width="200">
          <template #default="scope">
            <el-button size="small" @click="showSchema(scope.row)">
              查看参数
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="primary" @click="testTool(scope.row)">
              测试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 工具参数对话框 -->
    <el-dialog v-model="schemaDialogVisible" title="工具参数定义" width="600px">
      <pre class="schema-content">{{ currentSchema }}</pre>
    </el-dialog>

    <!-- 测试工具对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试 MCP 工具" width="600px">
      <el-form :model="testFormData" label-width="120px">
        <el-form-item label="工具名称">
          <el-input v-model="testFormData.name" disabled />
        </el-form-item>
        <el-form-item label="参数 (JSON)">
          <el-input
            v-model="testFormData.params"
            type="textarea"
            :rows="6"
            placeholder='{"arg1": "value1"}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="executeTest">执行</el-button>
      </template>
      <div v-if="testResult" class="test-result">
        <h4>执行结果：</h4>
        <pre>{{ testResult }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { mcpApi, type McpConfig } from '@/api'

// 响应式数据
const mcpServers = ref<McpConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const toolsDialogVisible = ref(false)
const schemaDialogVisible = ref(false)
const testDialogVisible = ref(false)
const isEdit = ref(false)
const mcpTools = ref<any[]>([])
const currentSchema = ref('')
const testResult = ref('')
const formRef = ref()

const formData = ref<McpConfig>({
  mcpId: '',
  name: '',
  transportType: 'stdio',
  command: '',
  url: '',
  args: [],
  env: {},
  connected: false
})

const testFormData = ref({
  name: '',
  params: ''
})

const rules = {
  mcpId: [{ required: true, message: '请输入 MCP ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  transportType: [{ required: true, message: '请选择传输类型', trigger: 'change' }]
}

// 环境变量 JSON 字符串
const envJson = computed({
  get: () => JSON.stringify(formData.value.env || {}, null, 2),
  set: (val: string) => {
    try {
      formData.value.env = JSON.parse(val)
    } catch (e) {
      // 忽略解析错误
    }
  }
})

// 加载 MCP 列表
const refreshList = async () => {
  loading.value = true
  try {
    const data = await mcpApi.list()
    mcpServers.value = data
    ElMessage.success('列表已刷新')
  } catch (error) {
    console.error('加载 MCP 列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  formData.value = {
    mcpId: '',
    name: '',
    transportType: 'stdio',
    command: '',
    url: '',
    args: [],
    env: {},
    connected: false
  }
  dialogVisible.value = true
}

// 编辑 MCP
const editMcp = (mcp: McpConfig) => {
  isEdit.value = true
  formData.value = { ...mcp }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await mcpApi.update(formData.value.mcpId, formData.value)
          ElMessage.success('MCP 更新成功')
        } else {
          await mcpApi.create(formData.value)
          ElMessage.success('MCP 创建成功')
        }
        dialogVisible.value = false
        refreshList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

// 切换连接状态
const toggleConnection = async (mcp: McpConfig) => {
  try {
    if (mcp.connected) {
      await mcpApi.disconnect(mcp.mcpId)
      ElMessage.success('MCP 服务器已断开')
    } else {
      await mcpApi.connect(mcp.mcpId)
      ElMessage.success('MCP 服务器已连接')
    }
    refreshList()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 查看工具列表
const viewTools = async (mcp: McpConfig) => {
  try {
    const tools = await mcpApi.getTools(mcp.mcpId)
    mcpTools.value = tools
    toolsDialogVisible.value = true
  } catch (error) {
    console.error('获取工具列表失败:', error)
  }
}

// 显示参数定义
const showSchema = (tool: any) => {
  currentSchema.value = JSON.stringify(tool.inputSchema, null, 2)
  schemaDialogVisible.value = true
}

// 测试工具
const testTool = (tool: any) => {
  testFormData.value = {
    name: tool.name,
    params: '{}'
  }
  testResult.value = ''
  testDialogVisible.value = true
}

// 执行测试
const executeTest = async () => {
  try {
    const params = JSON.parse(testFormData.value.params)
    const result = await mcpApi.test(testFormData.value.name, params)
    testResult.value = JSON.stringify(result, null, 2)
    ElMessage.success('测试完成')
  } catch (error) {
    console.error('测试失败:', error)
    testResult.value = '执行失败: ' + error
  }
}

// 删除 MCP
const deleteMcp = async (mcpId: string) => {
  try {
    await mcpApi.delete(mcpId)
    ElMessage.success('MCP 配置已删除')
    refreshList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 扩展 API 类型定义
declare module '@/api' {
  interface mcpApi {
    test(toolName: string, params: any): Promise<any>
  }
}

// 加载数据
onMounted(() => {
  refreshList()
})
</script>

<style scoped lang="scss">
.mcp-list {
  padding: 20px;

  .action-bar {
    margin-bottom: 20px;
  }

  .schema-content {
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    overflow-x: auto;
    font-size: 13px;
  }

  .test-result {
    margin-top: 20px;

    h4 {
      margin-bottom: 10px;
    }

    pre {
      background-color: #f5f7fa;
      padding: 15px;
      border-radius: 4px;
      overflow-x: auto;
      max-height: 300px;
    }
  }
}
</style>
