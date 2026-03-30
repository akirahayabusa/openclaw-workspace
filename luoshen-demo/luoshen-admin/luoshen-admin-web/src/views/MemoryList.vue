<template>
  <div class="memory-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索记忆内容"
        style="width: 300px"
        @keyup.enter="searchMemories"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select 
        v-model="selectedType" 
        placeholder="记忆类型"
        style="width: 150px"
        clearable
        @change="searchMemories"
      >
        <el-option label="短期记忆" value="short_term" />
        <el-option label="长期记忆" value="long_term" />
      </el-select>

      <el-button type="primary" @click="searchMemories">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>

      <el-button @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Memory 列表 -->
    <el-table :data="memories" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="200" />
      <el-table-column prop="agentId" label="Agent ID" width="150" />
      <el-table-column prop="sessionId" label="Session ID" width="200" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'short_term' ? 'primary' : 'success'">
            {{ scope.row.type === 'short_term' ? '短期' : '长期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" width="300">
        <template #default="scope">
          <el-tooltip 
            :content="scope.row.content" 
            placement="top"
            :disabled="scope.row.content.length < 50"
          >
            <div class="content-preview">
              {{ scope.row.content.substring(0, 50) }}
              {{ scope.row.content.length > 50 ? '...' : '' }}
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="timestamp" label="时间" width="180" />
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="scope">
          <el-button size="small" @click="viewMemory(scope.row)">
            查看
          </el-button>
          <el-button 
            size="small" 
            type="danger"
            @click="deleteMemory(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadMemories"
        @current-change="loadMemories"
      />
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="记忆详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentMemory.id }}</el-descriptions-item>
        <el-descriptions-item label="Agent ID">{{ currentMemory.agentId }}</el-descriptions-item>
        <el-descriptions-item label="Session ID">{{ currentMemory.sessionId }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentMemory.type === 'short_term' ? 'primary' : 'success'">
            {{ currentMemory.type === 'short_term' ? '短期记忆' : '长期记忆' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间">{{ currentMemory.timestamp }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="memory-content">{{ currentMemory.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { memoryApi, type MemoryInfo } from '@/api'

// 响应式数据
const memories = ref<MemoryInfo[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchKeyword = ref('')
const selectedType = ref('')
const viewDialogVisible = ref(false)
const currentMemory = ref<MemoryInfo>({
  id: '',
  agentId: '',
  sessionId: '',
  type: 'short_term',
  content: '',
  timestamp: ''
})

// 加载记忆列表
const loadMemories = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (selectedType.value) {
      params.type = selectedType.value
    }

    const data = await memoryApi.list(params)
    memories.value = data.content
    totalElements.value = data.totalElements
  } catch (error) {
    console.error('加载记忆列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索记忆
const searchMemories = () => {
  currentPage.value = 1
  loadMemories()
}

// 刷新列表
const refreshList = async () => {
  await loadMemories()
  ElMessage.success('列表已刷新')
}

// 查看记忆详情
const viewMemory = (memory: MemoryInfo) => {
  currentMemory.value = { ...memory }
  viewDialogVisible.value = true
}

// 删除记忆
const deleteMemory = async (memoryId: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该记忆吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await memoryApi.delete(memoryId)
    ElMessage.success('记忆已删除')
    loadMemories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 加载数据
onMounted(() => {
  loadMemories()
})
</script>

<style scoped lang="scss">
.memory-list {
  padding: 20px;

  .search-bar {
    margin-bottom: 20px;
    display: flex;
    gap: 15px;
    align-items: center;
  }

  .content-preview {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 300px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .memory-content {
    max-height: 300px;
    overflow-y: auto;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
