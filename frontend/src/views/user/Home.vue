<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Briefcase, Document, MagicStick, Message, Money, Tickets } from '@element-plus/icons-vue'
import { getApplications } from '../../api/application'
import { getNotices } from '../../api/content'
import { getRecommendJobs } from '../../api/job'
import { getMessages } from '../../api/message'
import { getResumes } from '../../api/resume'
import { getSocialPayments } from '../../api/social'
import { getPreference } from '../../api/user'

const router = useRouter()
const loading = ref(false)
const recommendJobs = ref([])
const notices = ref([])
const resumes = ref([])
const applications = ref([])
const messages = ref([])
const payments = ref([])
const preference = ref(null)

const preferenceTags = computed(() => {
  if (!preference.value) {
    return []
  }
  return [
    preference.value.industry,
    preference.value.workCity,
    preference.value.jobType,
    preference.value.position,
    preference.value.salaryMin || preference.value.salaryMax
      ? `${preference.value.salaryMin || 0} - ${preference.value.salaryMax || '不限'}`
      : '',
  ].filter(Boolean)
})

const stats = computed(() => [
  {
    label: '我的简历',
    value: resumes.value.length,
    icon: Document,
    path: '/user/resumes',
    color: '#2563eb',
  },
  {
    label: '我的投递',
    value: applications.value.length,
    icon: Tickets,
    path: '/user/applications',
    color: '#16a34a',
  },
  {
    label: '未读消息',
    value: messages.value.filter((item) => item.isRead === 0).length,
    icon: Message,
    path: '/user/messages',
    color: '#dc2626',
  },
  {
    label: '待缴费账单',
    value: payments.value.filter((item) => item.status === 0).length,
    icon: Money,
    path: '/user/social-security',
    color: '#d97706',
  },
])

const quickActions = [
  { label: '浏览岗位', path: '/user/jobs', icon: Briefcase },
  { label: '维护简历', path: '/user/resumes', icon: Document },
  { label: '公告政策', path: '/user/policies', icon: Bell },
  { label: '消息中心', path: '/user/messages', icon: Message },
]

async function loadDashboard() {
  loading.value = true
  try {
    const [jobRes, noticeRes, resumeRes, applicationRes, messageRes, paymentRes, preferenceRes] = await Promise.all([
      getRecommendJobs({ pageNum: 1, pageSize: 6 }),
      getNotices(),
      getResumes(),
      getApplications(),
      getMessages(),
      getSocialPayments(),
      getPreference(),
    ])
    recommendJobs.value = jobRes.data.records || []
    notices.value = (noticeRes.data || []).slice(0, 5)
    resumes.value = resumeRes.data || []
    applications.value = applicationRes.data || []
    messages.value = messageRes.data || []
    payments.value = paymentRes.data || []
    preference.value = preferenceRes.data || null
  } finally {
    loading.value = false
  }
}

function goJobDetail(id) {
  router.push(`/user/jobs/${id}`)
}

function noticeTypeText(type) {
  return type === 2 ? '通知' : type === 3 ? '就业资讯' : '公告'
}

onMounted(loadDashboard)
</script>

<template>
  <main class="home-page">
    <el-skeleton :loading="loading" animated :rows="10">
      <section class="stats-grid">
        <article v-for="item in stats" :key="item.label" class="stat-card" @click="router.push(item.path)">
          <div class="stat-icon" :style="{ background: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
          </div>
        </article>
      </section>

      <section class="dashboard-grid">
        <article class="panel recommend-panel">
          <div class="panel-head">
            <div>
              <div class="title-with-tag">
                <h2>推荐岗位</h2>
                <el-tag size="small" type="success" effect="plain">AI 可分析</el-tag>
              </div>
              <span>根据求职偏好展示匹配岗位</span>
            </div>
            <el-button type="primary" link @click="router.push('/user/jobs')">查看全部</el-button>
          </div>

          <div class="preference-hint">
            <template v-if="preferenceTags.length">
              <span class="hint-label">匹配依据</span>
              <el-tag v-for="item in preferenceTags" :key="item" size="small" effect="plain">{{ item }}</el-tag>
              <el-button type="primary" link @click="router.push('/user/preference')">修改偏好</el-button>
            </template>
            <template v-else>
              <span>完善求职偏好后，系统将优先推荐更匹配的岗位。</span>
              <el-button type="primary" link @click="router.push('/user/preference')">去完善</el-button>
            </template>
          </div>

          <div class="job-grid">
            <article v-for="job in recommendJobs" :key="job.id" class="job-card" @click="goJobDetail(job.id)">
              <div class="job-title">
                <h3>{{ job.title }}</h3>
                <el-tag size="small">{{ job.jobType }}</el-tag>
              </div>
              <div class="match-row">
                <el-progress
                  :percentage="job.matchScore || 0"
                  :stroke-width="8"
                  :show-text="false"
                  :color="job.matchScore >= 80 ? '#16a34a' : job.matchScore >= 60 ? '#2563eb' : '#d97706'"
                />
                <strong>匹配度 {{ job.matchScore || 0 }}%</strong>
              </div>
              <p class="salary">{{ job.salaryMin }} - {{ job.salaryMax }} / {{ job.salaryUnit }}</p>
              <p class="meta">{{ job.industry }} · {{ job.workCity }} · 招 {{ job.recruitCount }} 人</p>
              <p v-if="job.matchReasons?.length" class="match-reason">{{ job.matchReasons[0] }}</p>
              <p class="desc">{{ job.description }}</p>
            </article>
          </div>
          <el-empty v-if="recommendJobs.length === 0" description="暂无推荐岗位" />
        </article>

        <aside class="side-stack">
          <article class="panel ai-card">
            <div class="ai-head">
              <div class="ai-icon">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div>
                <h2>AI 求职助手</h2>
                <p>DeepSeek 已接入，支持简历优化、岗位匹配分析与模拟面试。</p>
              </div>
            </div>
            <div class="ai-actions">
              <el-button type="primary" @click="router.push('/user/resumes')">优化我的简历</el-button>
              <el-button @click="router.push('/user/jobs')">查看匹配岗位</el-button>
              <el-button @click="router.push('/user/ai-interview')">模拟面试</el-button>
            </div>
          </article>

          <article class="panel">
            <div class="panel-head compact">
              <h2>快捷入口</h2>
            </div>
            <div class="quick-grid">
              <button v-for="item in quickActions" :key="item.path" class="quick-action" @click="router.push(item.path)">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </button>
            </div>
          </article>

          <article class="panel">
            <div class="panel-head compact">
              <h2>最新公告</h2>
              <el-button type="primary" link @click="router.push('/user/policies')">更多</el-button>
            </div>
            <div class="notice-list">
              <div v-for="notice in notices" :key="notice.id" class="notice-item">
                <div>
                  <strong>{{ notice.title }}</strong>
                  <p>{{ notice.content }}</p>
                </div>
                <el-tag size="small" :type="notice.isTop ? 'danger' : 'info'">
                  {{ notice.isTop ? '置顶' : noticeTypeText(notice.type) }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无公告" />
          </article>
        </aside>
      </section>
    </el-skeleton>
  </main>
</template>

<style scoped>
.home-page {
  min-height: calc(100vh - 124px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.stat-card:hover,
.job-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.stat-icon {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 22px;
}

.stat-card p {
  margin: 0 0 6px;
  color: #64748b;
}

.stat-card strong {
  font-size: 28px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.panel {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.panel-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-head.compact {
  align-items: center;
}

.panel-head h2 {
  margin: 0;
  font-size: 20px;
}

.title-with-tag {
  display: flex;
  gap: 10px;
  align-items: center;
}

.panel-head span {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.preference-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 12px 14px;
  margin-bottom: 16px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  color: #475569;
  background: #eff6ff;
  font-size: 14px;
}

.hint-label {
  color: #1d4ed8;
  font-weight: 600;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.job-card {
  min-height: 190px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.job-title {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.job-title h3 {
  margin: 0;
  font-size: 18px;
}

.salary {
  margin: 14px 0 8px;
  color: #dc2626;
  font-weight: 700;
}

.match-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  margin-top: 14px;
}

.match-row strong {
  color: #1d4ed8;
  font-size: 13px;
  white-space: nowrap;
}

.meta,
.match-reason,
.desc {
  color: #64748b;
  line-height: 1.6;
}

.match-reason {
  margin: 8px 0;
  color: #2563eb;
  font-size: 13px;
}

.desc {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ai-card {
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 46%, #ecfdf5 100%);
}

.ai-head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.ai-icon {
  display: grid;
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #2563eb;
  font-size: 22px;
}

.ai-head h2 {
  margin: 0;
  font-size: 20px;
}

.ai-head p {
  margin: 8px 0 0;
  color: #475569;
  line-height: 1.6;
}

.ai-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  margin-top: 16px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-action {
  display: flex;
  min-height: 78px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #1f2937;
  background: #f8fafc;
  cursor: pointer;
}

.quick-action:hover {
  border-color: #93c5fd;
  color: #2563eb;
}

.quick-action .el-icon {
  font-size: 22px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  gap: 12px;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.notice-item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.notice-item strong {
  display: block;
  margin-bottom: 6px;
}

.notice-item p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #64748b;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

@media (max-width: 1100px) {
  .stats-grid,
  .dashboard-grid,
  .job-grid {
    grid-template-columns: 1fr;
  }
}
</style>
