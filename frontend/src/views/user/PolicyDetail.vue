<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Link, UploadFilled } from '@element-plus/icons-vue'
import { applyPolicy, getPolicyDetail } from '../../api/content'
import { uploadMaterial } from '../../api/user'
import { resolveFileUrl } from '../../utils/url'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const materialUploading = ref(false)
const policy = ref(null)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const form = reactive({
  applicantName: userInfo.realName || '',
  phone: userInfo.phone || '',
  materialUrl: '',
})

async function loadPolicy() {
  loading.value = true
  try {
    const res = await getPolicyDetail(route.params.id)
    policy.value = res.data
  } finally {
    loading.value = false
  }
}

async function submitApply() {
  if (!form.applicantName || !form.phone || !form.materialUrl) {
    ElMessage.warning('请填写申请人、联系电话和材料地址')
    return
  }
  submitting.value = true
  try {
    await applyPolicy({
      policyId: policy.value.id,
      applicantName: form.applicantName,
      phone: form.phone,
      materialUrl: form.materialUrl,
    })
    ElMessage.success('申报提交成功')
    router.push('/user/policies')
  } finally {
    submitting.value = false
  }
}

function beforeMaterialUpload(file) {
  const extension = file.name.split('.').pop()?.toLowerCase()
  const allowed = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'webp']
  const isAllowed = allowed.includes(extension)
  const isLt10M = file.size / 1024 / 1024 <= 10
  if (!isAllowed) {
    ElMessage.error('仅支持 PDF、Word 和图片材料')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('材料大小不能超过 10MB')
    return false
  }
  return true
}

async function handleMaterialUpload(options) {
  materialUploading.value = true
  try {
    const res = await uploadMaterial(options.file)
    form.materialUrl = res.data.url
    ElMessage.success('材料上传成功')
    options.onSuccess(res)
  } catch (error) {
    options.onError(error)
  } finally {
    materialUploading.value = false
  }
}

function getFileUrl(url) {
  return resolveFileUrl(url)
}

function openFile(url) {
  const fileUrl = getFileUrl(url)
  if (fileUrl) {
    window.open(fileUrl, '_blank')
  }
}

onMounted(loadPolicy)
</script>

<template>
  <main class="policy-detail-page">
    <div class="detail-actions">
      <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
    </div>

    <section class="content">
      <el-skeleton :loading="loading" animated :rows="10">
        <el-empty v-if="!policy" description="政策不存在" />
        <div v-else class="detail-layout">
          <article class="main-panel">
            <h1>{{ policy.title }}</h1>
            <section class="block">
              <h2>政策内容</h2>
              <p>{{ policy.content }}</p>
            </section>
            <section class="block">
              <h2>申报条件</h2>
              <p>{{ policy.applyCondition }}</p>
            </section>
            <section class="block">
              <h2>所需材料</h2>
              <p>{{ policy.materialRequired }}</p>
            </section>
          </article>

          <aside class="apply-panel">
            <h2>在线申报</h2>
            <el-form label-position="top">
              <el-form-item label="申请人">
                <el-input v-model="form.applicantName" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="form.phone" />
              </el-form-item>
              <el-form-item label="材料地址">
                <div class="material-upload-row">
                  <el-upload
                    :show-file-list="false"
                    :http-request="handleMaterialUpload"
                    :before-upload="beforeMaterialUpload"
                    accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.webp"
                  >
                    <el-button :loading="materialUploading" :icon="UploadFilled">上传材料</el-button>
                  </el-upload>
                  <el-input
                    v-model="form.materialUrl"
                    placeholder="上传后自动生成，也可以填写已有材料地址"
                  />
                  <el-button
                    v-if="form.materialUrl"
                    :icon="Link"
                    @click="openFile(form.materialUrl)"
                  >
                    查看
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
            <el-button type="primary" size="large" :loading="submitting" @click="submitApply">
              提交申报
            </el-button>
          </aside>
        </div>
      </el-skeleton>
    </section>
  </main>
</template>

<style scoped>
.policy-detail-page {
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
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  align-items: start;
}

.main-panel,
.apply-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.main-panel {
  padding: 28px;
}

.main-panel h1 {
  margin: 0;
  font-size: 28px;
}

.apply-panel {
  padding: 20px;
}

.apply-panel h2 {
  margin: 0 0 16px;
  font-size: 20px;
}

.apply-panel .el-button {
  width: 100%;
}

.material-upload-row {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;
}

.material-upload-row .el-input {
  min-width: 0;
  flex: 1;
}

.material-upload-row .el-button {
  width: auto;
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

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .material-upload-row {
    align-items: stretch;
    flex-direction: column;
  }

  .material-upload-row .el-button {
    width: 100%;
  }
}
</style>
