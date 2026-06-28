<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, register } from '../../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  gender: 1,
  captchaId: '',
  captchaCode: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const res = await getCaptcha('register')
    form.captchaId = res.data.captchaId
    form.captchaCode = ''
    captchaImage.value = res.data.image
  } finally {
    captchaLoading.value = false
  }
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      gender: form.gender,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode,
    }
    await register(payload)
    ElMessage.success('注册成功，请登录')
    router.push('/user/login')
  } catch (error) {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <main class="register-page">
    <section class="register-panel">
      <div class="brand">
        <div class="brand-mark">灵</div>
        <div>
          <h1>用户注册</h1>
          <p>创建个人求职账号</p>
        </div>
      </div>

      <el-form ref="formRef" class="register-form" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-row :gutter="14">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="form.username" size="large" placeholder="用于登录" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model.trim="form.realName" size="large" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :xs="24" :sm="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" size="large" type="password" placeholder="至少 6 位" show-password />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                size="large"
                type="password"
                placeholder="再次输入密码"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" size="large" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio-button :label="1">男</el-radio-button>
                <el-radio-button :label="2">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="form.email" size="large" placeholder="可选，用于接收通知" />
        </el-form-item>

        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model.trim="form.captchaCode"
              size="large"
              maxlength="4"
              placeholder="请输入验证码"
              @keyup.enter="handleRegister"
            />
            <button class="captcha-image" type="button" :disabled="captchaLoading" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>

        <el-button class="register-button" type="primary" size="large" :loading="loading" @click="handleRegister">
          注册
        </el-button>
        <div class="form-footer">
          <span>已有账号？</span>
          <el-button link type="primary" @click="router.push('/user/login')">返回登录</el-button>
        </div>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    linear-gradient(rgba(18, 31, 48, 0.55), rgba(18, 31, 48, 0.45)),
    url("https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=1600&q=80") center/cover;
}

.register-panel {
  width: min(680px, 100%);
  padding: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.25);
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 26px;
}

.brand-mark {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #2563eb;
  font-size: 24px;
  font-weight: 700;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  line-height: 1.25;
}

.brand p {
  margin: 6px 0 0;
  color: #64748b;
}

.register-form {
  margin-top: 8px;
}

.register-button {
  width: 100%;
  margin-top: 8px;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 10px;
  width: 100%;
}

.captcha-image {
  display: grid;
  height: 40px;
  place-items: center;
  padding: 0;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #eff6ff;
  cursor: pointer;
}

.captcha-image img {
  display: block;
  width: 124px;
  height: 44px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 16px;
  color: #64748b;
  font-size: 14px;
}

@media (max-width: 640px) {
  .register-panel {
    padding: 24px;
  }
}
</style>
