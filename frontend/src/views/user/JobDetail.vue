<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, MagicStick, Star } from '@element-plus/icons-vue'
import { analyzeJobMatch, favoriteJob, getJobDetail } from '../../api/job'
import { getResumes } from '../../api/resume'
import { createApplication } from '../../api/application'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const favoriteLoading = ref(false)
const applyLoading = ref(false)
const aiLoading = ref(false)
const applyDialogVisible = ref(false)
const aiVisible = ref(false)
const job = ref(null)
const resumes = ref([])
const selectedResumeId = ref(null)
const aiResult = ref(null)

async function loadDetail() {
  loading.value = true
  try {
    const res = await getJobDetail(route.params.id)
    job.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleFavorite() {
  favoriteLoading.value = true
  try {
    await favoriteJob(job.value.id)
    ElMessage.success('收藏成功')
  } finally {
    favoriteLoading.value = false
  }
}

async function goApply() {
  const res = await getResumes()
  resumes.value = res.data || []
  if (resumes.value.length === 0) {
    ElMessage.warning('请先新增一份简历')
    router.push('/user/resumes')
    return
  }
  selectedResumeId.value = resumes.value[0].id
  applyDialogVisible.value = true
}

async function submitApplication() {
  if (!selectedResumeId.value) {
    ElMessage.warning('请选择简历')
    return
  }
  applyLoading.value = true
  try {
    await createApplication({
      jobId: job.value.id,
      resumeId: selectedResumeId.value,
    })
    ElMessage.success('投递成功')
    applyDialogVisible.value = false
    router.push('/user/applications')
  } finally {
    applyLoading.value = false
  }
}

async function handleAiMatch() {
  if (!job.value) {
    return
  }
  aiLoading.value = true
  try {
    const res = await analyzeJobMatch(job.value.id)
    aiResult.value = res.data
    aiVisible.value = true
  } finally {
    aiLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <main class="detail-page">
    <div class="detail-actions">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
    </div>

    <section class="content">
      <el-skeleton :loading="loading" animated :rows="10">
        <el-empty v-if="!job" description="岗位不存在" />
        <div v-else class="detail-layout">
          <article class="main-panel">
            <div class="title-row">
              <div>
                <h1>{{ job.title }}</h1>
                <p>{{ job.industry }} · {{ job.jobType }} · {{ job.workCity }}</p>
              </div>
              <el-tag>{{ job.status === 1 ? '招聘中' : '已下架' }}</el-tag>
            </div>

            <div class="salary">{{ job.salaryMin }} - {{ job.salaryMax }} / {{ job.salaryUnit }}</div>

            <div class="info-grid">
              <div>
                <span>招聘人数</span>
                <strong>{{ job.recruitCount }} 人</strong>
              </div>
              <div>
                <span>浏览次数</span>
                <strong>{{ job.viewCount || 0 }}</strong>
              </div>
              <div>
                <span>工作地点</span>
                <strong>{{ job.workAddress }}</strong>
              </div>
            </div>

            <section class="block">
              <h2>岗位描述</h2>
              <p>{{ job.description }}</p>
            </section>

            <section class="block">
              <h2>岗位要求</h2>
              <p>{{ job.requirement }}</p>
            </section>
          </article>

          <aside class="side-panel">
            <h2>操作</h2>
            <el-button :icon="MagicStick" type="success" size="large" :loading="aiLoading" @click="handleAiMatch">
              AI 匹配分析
            </el-button>
            <el-button type="primary" size="large" @click="goApply">投递岗位</el-button>
            <el-button :icon="Star" size="large" :loading="favoriteLoading" @click="handleFavorite">
              收藏岗位
            </el-button>
            <el-button size="large" @click="router.push('/user/favorites')">查看我的收藏</el-button>
            <p>选择一份已维护的简历后，即可提交岗位投递。</p>
          </aside>
        </div>
      </el-skeleton>
    </section>

    <el-dialog v-model="applyDialogVisible" title="选择简历投递" width="520px">
      <el-radio-group v-model="selectedResumeId" class="resume-options">
        <el-radio v-for="resume in resumes" :key="resume.id" :label="resume.id" border>
          <div class="resume-option">
            <strong>{{ resume.title }}</strong>
            <span>{{ resume.realName }} · {{ resume.expectedPosition || '未填写期望岗位' }}</span>
          </div>
        </el-radio>
      </el-radio-group>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="submitApplication">确认投递</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="aiVisible" title="AI 岗位匹配分析" size="420px">
      <div v-if="aiResult" class="ai-panel">
        <div class="score-card">
          <el-progress type="dashboard" :percentage="aiResult.score || 0" :color="aiResult.score >= 80 ? '#16a34a' : aiResult.score >= 60 ? '#0f766e' : '#d97706'" />
          <div>
            <h3>{{ aiResult.score || 0 }} 分</h3>
            <el-tag :type="aiResult.source === 'deepseek' ? 'success' : 'warning'">
              {{ aiResult.source === 'deepseek' ? 'DeepSeek 分析' : '规则匹配' }}
            </el-tag>
          </div>
        </div>

        <p class="ai-summary">{{ aiResult.summary }}</p>

        <section class="ai-block">
          <h4>匹配理由</h4>
          <ul>
            <li v-for="item in aiResult.reasons" :key="item">{{ item }}</li>
          </ul>
        </section>

        <section class="ai-block">
          <h4>提升建议</h4>
          <ul>
            <li v-for="item in aiResult.suggestions" :key="item">{{ item }}</li>
          </ul>
        </section>
      </div>
      <el-empty v-else description="暂无分析结果" />
    </el-drawer>
  </main>
</template>

<style scoped>
.detail-page {
  min-height: calc(100vh - 124px);
}

.detail-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 16px;
}

.content {
  width: 100%;
  margin: 0;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  align-items: start;
}

.main-panel,
.side-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.main-panel {
  padding: 28px;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.side-panel h2 {
  margin: 0 0 4px;
  font-size: 18px;
}

.side-panel p {
  margin: 4px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.ai-panel {
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

.ai-summary {
  margin: 0;
  color: #334155;
  line-height: 1.8;
}

.ai-block {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.ai-block h4 {
  margin: 0 0 12px;
  font-size: 16px;
}

.ai-block ul {
  display: grid;
  gap: 10px;
  padding-left: 18px;
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.title-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.title-row h1 {
  margin: 0;
  font-size: 28px;
}

.title-row p {
  margin: 10px 0 0;
  color: #64748b;
}

.salary {
  margin: 22px 0;
  color: #dc2626;
  font-size: 24px;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 14px;
  border-radius: 8px;
  background: #f8fafc;
}

.info-grid div {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-grid span {
  color: #64748b;
  font-size: 13px;
}

.info-grid strong {
  font-size: 16px;
}

.block {
  margin-top: 26px;
}

.block h2 {
  margin: 0 0 12px;
  font-size: 20px;
}

.block p {
  margin: 0;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.resume-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.resume-options :deep(.el-radio) {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 12px;
}

.resume-option {
  display: flex;
  flex-direction: column;
  gap: 6px;
  line-height: 1.4;
}

.resume-option span {
  color: #64748b;
}

@media (max-width: 900px) {
  .detail-layout,
  .info-grid {
    grid-template-columns: 1fr;
  }

}
</style>
