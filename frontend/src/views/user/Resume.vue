<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, MagicStick } from '@element-plus/icons-vue'
import { analyzeResumeAdvice, createResume, deleteResume, getResumes, updateResume } from '../../api/resume'

const loading = ref(false)
const saving = ref(false)
const aiLoadingId = ref(null)
const dialogVisible = ref(false)
const adviceVisible = ref(false)
const editingId = ref(null)
const resumes = ref([])
const advice = ref(null)

const emptyForm = {
  title: '',
  realName: '',
  gender: 0,
  birthday: '',
  education: '',
  workYears: 0,
  phone: '',
  email: '',
  expectedPosition: '',
  expectedCity: '',
  selfIntro: '',
}

const form = reactive({ ...emptyForm })

async function loadResumes() {
  loading.value = true
  try {
    const res = await getResumes()
    resumes.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, emptyForm)
  editingId.value = null
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    title: row.title || '',
    realName: row.realName || '',
    gender: row.gender ?? 0,
    birthday: row.birthday || '',
    education: row.education || '',
    workYears: row.workYears || 0,
    phone: row.phone || '',
    email: row.email || '',
    expectedPosition: row.expectedPosition || '',
    expectedCity: row.expectedCity || '',
    selfIntro: row.selfIntro || '',
  })
  editingId.value = row.id
  dialogVisible.value = true
}

async function saveResume() {
  if (!form.title || !form.realName || !form.phone) {
    ElMessage.warning('请填写简历标题、姓名和联系电话')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateResume(editingId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createResume(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadResumes()
  } finally {
    saving.value = false
  }
}

async function removeResume(row) {
  await ElMessageBox.confirm(`确认删除简历“${row.title}”？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteResume(row.id)
  ElMessage.success('删除成功')
  loadResumes()
}

async function openAdvice(row) {
  aiLoadingId.value = row.id
  try {
    const res = await analyzeResumeAdvice(row.id)
    advice.value = res.data
    adviceVisible.value = true
  } finally {
    aiLoadingId.value = null
  }
}

function genderText(value) {
  return value === 1 ? '男' : value === 2 ? '女' : '未知'
}

onMounted(loadResumes)
</script>

<template>
  <main class="resume-page">
    <section class="content">
      <div class="toolbar">
        <div>
          <h2>我的简历</h2>
          <span>建议至少维护一份可用于投递的简历</span>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增简历</el-button>
      </div>

      <el-table v-loading="loading" :data="resumes" border class="resume-table">
        <el-table-column prop="title" label="简历标题" min-width="160" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="110" />
        <el-table-column prop="workYears" label="工作年限" width="100" />
        <el-table-column prop="expectedPosition" label="期望岗位" min-width="140" />
        <el-table-column prop="expectedCity" label="期望城市" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" :icon="MagicStick" :loading="aiLoadingId === row.id" @click="openAdvice(row)">AI 优化</el-button>
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="removeResume(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑简历' : '新增简历'" width="720px">
      <el-form label-position="top" class="resume-form">
        <el-form-item label="简历标题">
          <el-input v-model="form.title" placeholder="例如：我的求职简历" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="form.education" placeholder="请选择学历">
            <el-option label="高中及以下" value="高中及以下" />
            <el-option label="大专" value="大专" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士及以上" value="硕士及以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作年限">
          <el-input-number v-model="form.workYears" :min="0" :max="50" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="期望岗位">
          <el-input v-model="form.expectedPosition" placeholder="请输入期望岗位" />
        </el-form-item>
        <el-form-item label="期望城市">
          <el-input v-model="form.expectedCity" placeholder="请输入期望城市" />
        </el-form-item>
        <el-form-item class="full-row" label="自我介绍">
          <el-input v-model="form.selfIntro" type="textarea" :rows="4" placeholder="简单介绍个人经验和求职优势" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveResume">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="adviceVisible" title="AI 简历优化建议" size="420px">
      <div v-if="advice" class="advice-panel">
        <div class="score-card">
          <el-progress type="dashboard" :percentage="advice.score || 0" :color="advice.score >= 80 ? '#16a34a' : advice.score >= 60 ? '#0f766e' : '#d97706'" />
          <div>
            <h3>{{ advice.score || 0 }} 分</h3>
            <el-tag :type="advice.source === 'deepseek' ? 'success' : 'warning'">
              {{ advice.source === 'deepseek' ? 'DeepSeek 分析' : '规则建议' }}
            </el-tag>
          </div>
        </div>

        <p class="advice-summary">{{ advice.summary }}</p>

        <section class="advice-block">
          <h4>简历优势</h4>
          <ul>
            <li v-for="item in advice.advantages" :key="item">{{ item }}</li>
          </ul>
        </section>

        <section class="advice-block">
          <h4>优化建议</h4>
          <ul>
            <li v-for="item in advice.suggestions" :key="item">{{ item }}</li>
          </ul>
        </section>

        <section class="advice-block">
          <h4>建议补充关键词</h4>
          <div class="keyword-list">
            <el-tag v-for="item in advice.keywords" :key="item" effect="plain">{{ item }}</el-tag>
          </div>
        </section>
      </div>
      <el-empty v-else description="暂无优化建议" />
    </el-drawer>
  </main>
</template>

<style scoped>
.resume-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: 100%;
  margin: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.toolbar span {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.resume-table {
  border-radius: 8px;
  overflow: hidden;
}

.resume-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-row {
  grid-column: 1 / -1;
}

.advice-panel {
  display: grid;
  gap: 18px;
}

.score-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
}

.score-card h3 {
  margin: 0 0 10px;
  font-size: 28px;
}

.advice-summary {
  margin: 0;
  color: #334155;
  line-height: 1.8;
}

.advice-block {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.advice-block h4 {
  margin: 0 0 12px;
  font-size: 16px;
}

.advice-block ul {
  display: grid;
  gap: 10px;
  padding-left: 18px;
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 900px) {
  .toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .resume-form {
    display: flex;
    flex-direction: column;
  }
}
</style>
