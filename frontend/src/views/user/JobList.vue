<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getJobs } from '../../api/job'
import { cityOptions, industryOptions, jobTypeOptions } from '../../constants/jobOptions'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  industry: '',
  jobType: '',
  workCity: '',
  pageNum: 1,
  pageSize: 9,
})

async function loadJobs() {
  loading.value = true
  try {
    const res = await getJobs(query)
    jobs.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  loadJobs()
}

function reset() {
  query.keyword = ''
  query.industry = ''
  query.jobType = ''
  query.workCity = ''
  query.pageNum = 1
  loadJobs()
}

function pageChange(page) {
  query.pageNum = page
  loadJobs()
}

function goDetail(id) {
  router.push(`/user/jobs/${id}`)
}

onMounted(loadJobs)
</script>

<template>
  <main class="job-list-page">
    <section class="content">
      <div class="filter-panel">
        <el-input v-model="query.keyword" clearable placeholder="搜索岗位名称或描述" @keyup.enter="search" />
        <el-select v-model="query.industry" clearable placeholder="行业">
          <el-option v-for="item in industryOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.jobType" clearable placeholder="岗位类型">
          <el-option v-for="item in jobTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.workCity" clearable placeholder="城市">
          <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </div>

      <el-skeleton :loading="loading" animated :rows="8">
        <el-empty v-if="jobs.length === 0" description="暂无岗位" />
        <div v-else class="job-grid">
          <article v-for="job in jobs" :key="job.id" class="job-card" @click="goDetail(job.id)">
            <div class="job-title">
              <h2>{{ job.title }}</h2>
              <el-tag size="small">{{ job.jobType }}</el-tag>
            </div>
            <p class="salary">{{ job.salaryMin }} - {{ job.salaryMax }} / {{ job.salaryUnit }}</p>
            <p class="meta">{{ job.industry }} · {{ job.workCity }} · 招 {{ job.recruitCount }} 人</p>
            <p class="address">{{ job.workAddress }}</p>
            <p class="desc">{{ job.description }}</p>
          </article>
        </div>
      </el-skeleton>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="pageChange"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.job-list-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: 100%;
  margin: 0;
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) repeat(3, minmax(140px, 0.8fr)) auto auto;
  gap: 12px;
  padding: 16px;
  margin-bottom: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.job-card {
  min-height: 220px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.job-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.job-title {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.job-title h2 {
  margin: 0;
  font-size: 18px;
}

.salary {
  margin: 14px 0 8px;
  color: #dc2626;
  font-weight: 700;
}

.meta,
.address,
.desc {
  color: #64748b;
  line-height: 1.6;
}

.address {
  margin: 8px 0;
}

.desc {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

@media (max-width: 1024px) {
  .filter-panel,
  .job-grid {
    grid-template-columns: 1fr;
  }

}
</style>
