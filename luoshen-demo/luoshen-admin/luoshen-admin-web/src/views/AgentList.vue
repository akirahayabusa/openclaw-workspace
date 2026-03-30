<template>
  <div class="agent-list">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建 Agent
      </el-button>
      <el-button @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Agent 列表 -->
    <el-table :data="agents" stripe style="width: 100%">
      <el-table-column prop="agentId" label="ID" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="getTypeTag(scope.row.type)">
            {{ scope.row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" width="200" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.enabled"
            @change="toggleAgent(scope.row.agentId)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="280">
        <template #default="scope">
          <el-button size="small" @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="primary" @click="testAgent(scope.row)">
            测试
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="deleteAgent(scope.row.agentId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑 Agent' : '创建 Agent'"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="Agent ID" prop="agentId">
          <el-input 
            v-model="formData.agentId" 
            :disabled="isEdit"
            placeholder="唯一标识符"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="Agent 名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="选择类型">
            <el-option label="Leader Agent" value="leader" />
            <el-option label="Core Agent" value="core" />
            <el-option label="Sub Agent" value="sub" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="2"
            placeholder="Agent 描述"
          />
        </el-form-item>
        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input 
            v-model="formData.systemPrompt" 
            type="textarea"
            :rows="5"
            placeholder="系统提示词"
          />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="formData.modelName" placeholder="qwen-max" />
        </el-form-item>
        <el-form-item label="父级 Agent">
          <el-select 
            v-model="formData.parentAgentId" 
            placeholder="选择父级 Agent（可选）"
            clearable
          >
            <el-option
              v-for="agent in parentAgents"
              :key="agent.agentId"
              :label="agent.name"
              :value="agent.agentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试 Agent" width="600px">
      <el-input
        v-model="testMessage"
        type="textarea"
        :rows="3"
        placeholder="输入测试消息"
      />
      <el-button 
        type="primary" 
        @click="sendTestMessage"
        style="margin-top: 10px"
      >
        发送
      </el-button>
      <div v-if="testResponse" style="margin-top: 20px">
        <h4>响应：</h4>
        <pre>{{ testResponse }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { agentApi, type AgentConfig } from '@/api'

// 响应式数据
const agents = ref<AgentConfig[]>([])
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const isEdit = ref(false)
const currentAgent = ref<AgentConfig | null>(null)
const testMessage = ref('')
const testResponse = ref('')
const formRef = ref()

const formData = ref<AgentConfig>({
  agentId: '',
  name: '',
  description: '',
  type: 'sub',
  systemPrompt: '',
  modelName: 'qwen-max',
  enabled: true
})

const rules = {
  agentId: [{ required: true, message: '请输入 Agent ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  systemPrompt: [{ required: true, message: '请输入系统提示词', trigger: 'blur' }]
}

// 计算父级 Agent 列表
const parentAgents = computed(() => {
  return agents.value.filter(a => 
    a.type === 'leader' || a.type === 'core'
  )
})

// 获取类型标签
const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    leader: 'danger',
    core: 'warning',
    sub: 'success'
  }
  return map[type] || 'info'
}

// 刷新列表
const refreshList = async () => {
  try {
    const data = await agentApi.list()
    agents.value = data
    ElMessage.success('列表已刷新')
  } catch (error) {
    console.error('获取 Agent 列表失败:', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  currentAgent.value = null
  formData.value = {
    agentId: '',
    name: '',
    description: '',
    type: 'sub',
    systemPrompt: '',
    modelName: 'qwen-max',
    enabled: true
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (agent: AgentConfig) => {
  isEdit.value = true
  currentAgent.value = agent
  formData.value = { ...agent }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await agentApi.update(formData.value.agentId, formData.value)
          ElMessage.success('Agent 更新成功')
        } else {
          await agentApi.create(formData.value)
          ElMessage.success('Agent 创建成功')
        }
        dialogVisible.value = false
        refreshList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

// 切换 Agent 状态
const toggleAgent = async (agentId: string) => {
  try {
    await agentApi.toggle(agentId)
    ElMessage.success('状态已更新')
    refreshList()
  } catch (error) {
    console.error('切换状态失败:', error)
  }
}

// 删除 Agent
const deleteAgent = async (agentId: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该 Agent 吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await agentApi.delete(agentId)
    ElMessage.success('Agent 已删除')
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 测试 Agent
const testAgent = (agent: AgentConfig) => {
  currentAgent.value = agent
  testMessage.value = ''
  testResponse.value = ''
  testDialogVisible.value = true
}

// 发送测试消息
const sendTestMessage = async () => {
  if (!currentAgent.value || !testMessage.value) return
  
  try {
    const response = await agentApi.test(currentAgent.value.agentId, testMessage.value)
    testResponse.value = JSON.stringify(response, null, 2)
    ElMessage.success('测试完成')
  } catch (error) {
    console.error('测试失败:', error)
  }
}

// 加载数据
onMounted(() => {
  refreshList()
})
</script>

<style scoped lang="scss">
.agent-list {
  padding: 20px;

  .action-bar {
    margin-bottom: 20px;
  }

  pre {
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    overflow-x: auto;
  }
}
</style>
