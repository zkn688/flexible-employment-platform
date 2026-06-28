<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, UploadFilled } from '@element-plus/icons-vue'
import { getProfile, updatePassword, updateProfile, uploadAvatar } from '../../api/user'
import { resolveFileUrl } from '../../utils/url'

const profileFormRef = ref()
const passwordFormRef = ref()
const loading = ref(false)
const saving = ref(false)
const passwordSaving = ref(false)
const avatarUploading = ref(false)

const profileForm = reactive({
  realName: '',
  gender: 0,
  phone: '',
  email: '',
  idCard: '',
  avatar: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  idCard: [
    {
      pattern: /(^\d{15}$)|(^\d{17}[\dXx]$)/,
      message: '请输入正确的身份证号',
      trigger: 'blur',
    },
  ],
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

const completionItems = computed(() => [
  { label: '真实姓名', filled: Boolean(profileForm.realName) },
  { label: '手机号', filled: Boolean(profileForm.phone) },
  { label: '邮箱', filled: Boolean(profileForm.email) },
  { label: '身份证号', filled: Boolean(profileForm.idCard) },
  { label: '性别', filled: Number(profileForm.gender) !== 0 },
  { label: '头像', filled: Boolean(profileForm.avatar) },
])

const completionPercent = computed(() => {
  const filledCount = completionItems.value.filter((item) => item.filled).length
  return Math.round((filledCount / completionItems.value.length) * 100)
})

const completionStatus = computed(() => {
  if (completionPercent.value >= 80) {
    return 'success'
  }
  if (completionPercent.value >= 50) {
    return 'warning'
  }
  return 'exception'
})

const avatarText = computed(() => profileForm.realName?.slice(0, 1) || '用')

const avatarSrc = computed(() => {
  return resolveFileUrl(profileForm.avatar)
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    Object.assign(profileForm, {
      realName: res.data.realName || '',
      gender: res.data.gender ?? 0,
      phone: res.data.phone || '',
      email: res.data.email || '',
      idCard: res.data.idCard || '',
      avatar: res.data.avatar || '',
    })
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  await profileFormRef.value.validate()
  saving.value = true
  try {
    const res = await updateProfile(profileForm)
    const oldInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    localStorage.setItem('userInfo', JSON.stringify({ ...oldInfo, ...res.data }))
    ElMessage.success('资料保存成功')
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  await passwordFormRef.value.validate()
  passwordSaving.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码修改成功')
  } finally {
    passwordSaving.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 <= 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(options.file)
    profileForm.avatar = res.data.url
    ElMessage.success('头像上传成功，请记得保存资料')
    options.onSuccess(res)
  } catch (error) {
    options.onError(error)
  } finally {
    avatarUploading.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <main class="profile-page">
    <section v-loading="loading" class="content">
      <article class="panel">
        <div class="profile-head">
          <div class="avatar-box">
            <el-avatar v-if="avatarSrc" :size="68" :src="avatarSrc" />
            <el-avatar v-else :size="68" class="text-avatar">{{ avatarText }}</el-avatar>
          </div>
          <div>
            <h2>基本资料</h2>
            <p>完善资料后，可用于简历投递、政策申请和社保服务。</p>
          </div>
        </div>

        <div class="completion-card">
          <div class="completion-title">
            <span>资料完整度</span>
            <strong>{{ completionPercent }}%</strong>
          </div>
          <el-progress :percentage="completionPercent" :status="completionStatus" />
          <div class="completion-tags">
            <el-tag
              v-for="item in completionItems"
              :key="item.label"
              size="small"
              :type="item.filled ? 'success' : 'info'"
              effect="plain"
            >
              {{ item.filled ? '已填' : '待填' }} · {{ item.label }}
            </el-tag>
          </div>
        </div>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-position="top"
          class="profile-form"
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model.trim="profileForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
              <el-radio :label="0">未知</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model.trim="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model.trim="profileForm.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="头像">
            <div class="avatar-upload-row">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                :before-upload="beforeAvatarUpload"
                accept="image/*"
              >
                <el-button :loading="avatarUploading" :icon="UploadFilled">选择图片</el-button>
              </el-upload>
              <el-input
                v-model.trim="profileForm.avatar"
                class="avatar-url-input"
                placeholder="上传后自动生成，也可以填写图片地址"
              />
            </div>
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
      </article>

      <article class="panel">
        <div class="security-head">
          <div class="security-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div>
            <h2>账号安全</h2>
            <p>定期修改密码，保护个人求职信息。</p>
          </div>
        </div>
        <el-alert
          class="security-alert"
          title="修改成功后，请使用新密码进行下一次登录。"
          type="info"
          show-icon
          :closable="false"
        />
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="passwordSaving" @click="savePassword">修改密码</el-button>
      </article>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  min-height: calc(100vh - 124px);
}

.content {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  margin: 0;
}

.panel {
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.panel h2 {
  margin: 0;
  font-size: 20px;
}

.panel p {
  margin: 6px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.profile-head,
.security-head {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 18px;
}

.avatar-box {
  flex: 0 0 auto;
}

.text-avatar {
  background: #2563eb;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.completion-card {
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #eff6ff;
}

.completion-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.completion-title span {
  color: #1e3a8a;
  font-weight: 600;
}

.completion-title strong {
  color: #1d4ed8;
  font-size: 20px;
}

.completion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.profile-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.avatar-upload-row {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;
}

.avatar-url-input {
  min-width: 0;
  flex: 1;
}

.security-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #16a34a;
  font-size: 22px;
}

.security-alert {
  margin-bottom: 18px;
}

@media (max-width: 900px) {
  .content {
    grid-template-columns: 1fr;
  }

  .profile-form {
    grid-template-columns: 1fr;
  }

  .avatar-upload-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
