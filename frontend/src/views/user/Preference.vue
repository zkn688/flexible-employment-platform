<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPreference, savePreference } from '../../api/user'
import { cityOptions, commonPositionOptions, industryOptions, jobTypeOptions } from '../../constants/jobOptions'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  industry: '',
  position: '',
  salaryMin: 0,
  salaryMax: 0,
  workCity: '',
  jobType: '',
})

async function loadPreference() {
  loading.value = true
  try {
    const res = await getPreference()
    if (res.data) {
      Object.assign(form, {
        industry: res.data.industry || '',
        position: res.data.position || '',
        salaryMin: res.data.salaryMin || 0,
        salaryMax: res.data.salaryMax || 0,
        workCity: res.data.workCity || '',
        jobType: res.data.jobType || '',
      })
    }
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (form.salaryMax && form.salaryMin && Number(form.salaryMax) < Number(form.salaryMin)) {
    ElMessage.warning('最高薪资不能低于最低薪资')
    return
  }
  saving.value = true
  try {
    await savePreference(form)
    ElMessage.success('求职偏好保存成功')
  } finally {
    saving.value = false
  }
}

function queryPosition(queryString, callback) {
  const keyword = queryString.trim()
  const options = commonPositionOptions.map((item) => ({ value: item }))
  callback(keyword ? options.filter((item) => item.value.includes(keyword)) : options)
}

onMounted(loadPreference)
</script>

<template>
  <main class="preference-page">
    <section v-loading="loading" class="content">
      <article class="panel">
        <div class="panel-head">
          <h2>偏好设置</h2>
          <p>平台会根据行业、城市、岗位类型和薪资范围推荐更匹配的岗位。</p>
        </div>
        <el-form label-position="top" class="preference-form">
          <el-form-item label="期望行业">
            <el-select v-model="form.industry" clearable filterable placeholder="请选择行业">
              <el-option v-for="item in industryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="期望岗位">
            <el-autocomplete
              v-model="form.position"
              clearable
              :fetch-suggestions="queryPosition"
              placeholder="例如：前端开发工程师、线上客服"
            />
          </el-form-item>
          <el-form-item label="最低薪资">
            <el-input-number v-model="form.salaryMin" :min="0" :step="500" />
          </el-form-item>
          <el-form-item label="最高薪资">
            <el-input-number v-model="form.salaryMax" :min="0" :step="500" />
          </el-form-item>
          <el-form-item label="期望城市">
            <el-select v-model="form.workCity" clearable filterable placeholder="请选择城市">
              <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类型">
            <el-select v-model="form.jobType" clearable filterable placeholder="请选择岗位类型">
              <el-option v-for="item in jobTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="submit">保存偏好</el-button>
          <el-button @click="router.push('/user/home')">查看推荐岗位</el-button>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.preference-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: min(860px, calc(100% - 48px));
  margin: 0 auto;
}

.panel {
  padding: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.panel-head {
  margin-bottom: 18px;
}

.panel h2 {
  margin: 0 0 18px;
  font-size: 20px;
}

.panel-head h2 {
  margin-bottom: 6px;
}

.panel-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.preference-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

@media (max-width: 900px) {
  .actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .preference-form {
    grid-template-columns: 1fr;
  }
}
</style>
