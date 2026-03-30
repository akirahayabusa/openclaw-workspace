<template>
  <div class="skill-list">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建 Skill
      </el-button>
      <el-button @click="showUploadDialog">
        <el-icon><Upload /></el-icon>
        上传 Skill
      </el-button>
      <el-button @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Skill 列表 -->
    <el-table :data="skills" stripe v-loading="loading">
      <el-table-column prop="skillId" label="ID" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="description" label="描述" width="200" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'info'">
            {{ scope.row.enabled ? '已启用' : '未启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="250">
        <template #default="scope">
          <el-button size="small" @click="previewSkill(scope.row)">
            预览
          </el-button>
          <el-button 
            size="small"
            type="primary"
            @click="editSkill(scope.row)"
          >
            编辑
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

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑 Skill' : '创建 Skill'"
      width="800px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="Skill ID" prop="skillId">
          <el-input 
            v-model="formData.skillId" 
            :disabled="isEdit"
            placeholder="唯一标识符"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="Skill 名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="2"
            placeholder="Skill 描述"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="formData.content" 
            type="textarea"
            :rows="10"
            placeholder="Markdown 格式的 Skill 内容"
          />
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

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传 Skill" width="600px">
      <el-upload
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".md"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 .md 文件，且不超过 1MB
          </div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="Skill 预览" width="900px">
      <div class="preview-content" v-html="previewHtml"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Refresh, UploadFilled } from '@element-plus/icons-vue'
import { skillApi, type SkillConfig } from '@/api'
import { marked } from 'marked'

// 响应式数据
const skills = ref<SkillConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const isEdit = ref(false)
const previewHtml = ref('')
const formRef = ref()

const formData = ref<SkillConfig>({
  skillId: '',
  name: '',
  description: '',
  content: '',
  enabled: true
})

const rules = {
  skillId: [{ required: true, message: '请输入 Skill ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 加载 Skill 列表
const refreshList = async () => {
  loading.value = true
  try {
    const data = await skillApi.list()
    skills.value = data
    ElMessage.success('列表已刷新')
  } catch (error) {
    console.error('获取 Skill 列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  formData.value = {
    skillId: '',
    name: '',
    description: '',
    content: '',
    enabled: true
  }
  dialogVisible.value = true
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

// 处理文件上传
const handleFileChange = async (file: any) => {
  if (file.size > 1024 * 1024) {
    ElMessage.error('文件大小不能超过 1MB')
    return
  }

  try {
    await skillApi.upload(file.raw)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    refreshList()
  } catch (error) {
    console.error('上传失败:', error)
  }
}

// 编辑 Skill
const editSkill = (skill: SkillConfig) => {
  isEdit.value = true
  formData.value = { ...skill }
  dialogVisible.value = true
}

// 预览 Skill
const previewSkill = async (skill: SkillConfig) => {
  previewHtml.value = marked(skill.content) as string
  previewDialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await skillApi.update(formData.value.skillId, formData.value)
          ElMessage.success('Skill 更新成功')
        } else {
          await skillApi.create(formData.value)
          ElMessage.success('Skill 创建成功')
        }
        dialogVisible.value = false
        refreshList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

// 删除 Skill
const deleteSkill = async (skillId: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该 Skill 吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await skillApi.delete(skillId)
    ElMessage.success('Skill 已删除')
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 安装 marked 库
// npm install marked

// 加载数据
onMounted(() => {
  refreshList()
})
</script>

<style scoped lang="scss">
.skill-list {
  padding: 20px;

  .action-bar {
    margin-bottom: 20px;
  }

  .preview-content {
    padding: 20px;
    background-color: #f9f9f9;
    border-radius: 4px;
    max-height: 500px;
    overflow-y: auto;

    :deep(h1), :deep(h2), :deep(h3) {
      margin-top: 1rem;
      margin-bottom: 0.5rem;
    }

    :deep(pre) {
      background-color: #f5f5f5;
      padding: 10px;
      border-radius: 4px;
      overflow-x: auto;
    }

    :deep(code) {
      background-color: #f5f5f5;
      padding: 2px 6px;
      border-radius: 3px;
    }
  }
}
</style>
