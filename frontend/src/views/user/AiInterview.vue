<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, MagicStick, Reading } from '@element-plus/icons-vue'
import { analyzeInterviewAnswer, generateInterviewQuestions } from '../../api/ai'
import { commonPositionOptions } from '../../constants/jobOptions'

const generating = ref(false)
const evaluating = ref(false)
const questions = ref([])
const tips = ref([])
const summary = ref('')
const source = ref('')
const activeQuestionIndex = ref(0)
const feedback = ref(null)

const form = reactive({
  position: 'AI Agent工程师',
  difficulty: '基础',
})

const answerForm = reactive({
  answer: '',
})

const difficultyOptions = ['基础', '进阶', '项目实战']

const currentQuestion = computed(() => questions.value[activeQuestionIndex.value] || '')

const sourceText = computed(() => {
  if (!source.value) {
    return ''
  }
  return source.value === 'deepseek' ? 'DeepSeek 生成' : '规则生成'
})

async function handleGenerate() {
  if (!form.position) {
    ElMessage.warning('请选择面试岗位')
    return
  }
  generating.value = true
  feedback.value = null
  answerForm.answer = ''
  try {
    const res = await generateInterviewQuestions({
      position: form.position,
      difficulty: form.difficulty,
    })
    questions.value = res.data.questions || []
    tips.value = res.data.tips || []
    summary.value = res.data.summary || ''
    source.value = res.data.source || ''
    activeQuestionIndex.value = 0
    ElMessage.success('面试题生成成功')
  } finally {
    generating.value = false
  }
}

function selectQuestion(index) {
  activeQuestionIndex.value = index
  feedback.value = null
  answerForm.answer = ''
}

async function handleEvaluate() {
  if (!currentQuestion.value) {
    ElMessage.warning('请先生成面试题')
    return
  }
  if (!answerForm.answer.trim()) {
    ElMessage.warning('请先填写你的回答')
    return
  }
  evaluating.value = true
  try {
    const res = await analyzeInterviewAnswer({
      position: form.position,
      difficulty: form.difficulty,
      question: currentQuestion.value,
      answer: answerForm.answer,
    })
    feedback.value = res.data
    ElMessage.success('AI 评价完成')
  } finally {
    evaluating.value = false
  }
}

handleGenerate()
</script>

<template>
  <main class="interview-page">
    <section class="hero-panel">
      <div>
        <div class="hero-title">
          <el-icon><MagicStick /></el-icon>
          <h1>AI 模拟面试</h1>
        </div>
        <p>根据岗位方向生成面试题，提交回答后获得评分、优点、改进建议和参考回答。</p>
      </div>
      <el-tag v-if="sourceText" :type="source === 'deepseek' ? 'success' : 'warning'" effect="plain">
        {{ sourceText }}
      </el-tag>
    </section>

    <section class="content-grid">
      <article class="panel setup-panel">
        <h2>面试设置</h2>
        <el-form label-position="top">
          <el-form-item label="岗位方向">
            <el-select v-model="form.position" filterable allow-create default-first-option>
              <el-option
                v-for="item in commonPositionOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-segmented v-model="form.difficulty" :options="difficultyOptions" />
          </el-form-item>
        </el-form>
        <el-button type="primary" :icon="Reading" :loading="generating" @click="handleGenerate">
          生成面试题
        </el-button>

        <div v-if="summary" class="summary-box">
          {{ summary }}
        </div>

        <div class="tips-box">
          <h3>答题建议</h3>
          <ul>
            <li v-for="item in tips" :key="item">{{ item }}</li>
          </ul>
        </div>
      </article>

      <article class="panel question-panel">
        <div class="panel-head">
          <h2>面试题</h2>
          <span>{{ questions.length }} 道</span>
        </div>
        <div class="question-list">
          <button
            v-for="(item, index) in questions"
            :key="item"
            class="question-item"
            :class="{ active: index === activeQuestionIndex }"
            @click="selectQuestion(index)"
          >
            <strong>第 {{ index + 1 }} 题</strong>
            <span>{{ item }}</span>
          </button>
        </div>
        <el-empty v-if="questions.length === 0" description="暂无面试题" />
      </article>

      <article class="panel answer-panel">
        <div class="panel-head">
          <h2>作答与评价</h2>
          <el-tag v-if="currentQuestion" size="small">第 {{ activeQuestionIndex + 1 }} 题</el-tag>
        </div>

        <div v-if="currentQuestion" class="current-question">
          {{ currentQuestion }}
        </div>

        <el-input
          v-model="answerForm.answer"
          type="textarea"
          :rows="8"
          maxlength="1000"
          show-word-limit
          placeholder="请输入你的回答，可以结合课程项目、技术栈、个人职责和结果进行说明。"
        />

        <div class="answer-actions">
          <el-button type="primary" :icon="ChatLineRound" :loading="evaluating" @click="handleEvaluate">
            AI 评价回答
          </el-button>
        </div>

        <div v-if="feedback" class="feedback-card">
          <div class="score-row">
            <el-progress type="dashboard" :percentage="feedback.score" :width="104" />
            <div>
              <h3>{{ feedback.summary }}</h3>
              <el-tag :type="feedback.source === 'deepseek' ? 'success' : 'warning'" effect="plain">
                {{ feedback.source === 'deepseek' ? 'DeepSeek 评价' : '规则评价' }}
              </el-tag>
            </div>
          </div>

          <div class="feedback-grid">
            <section>
              <h4>回答优点</h4>
              <ul>
                <li v-for="item in feedback.strengths" :key="item">{{ item }}</li>
              </ul>
            </section>
            <section>
              <h4>改进建议</h4>
              <ul>
                <li v-for="item in feedback.improvements" :key="item">{{ item }}</li>
              </ul>
            </section>
          </div>

          <section class="sample-answer">
            <h4>参考回答</h4>
            <p>{{ feedback.sampleAnswer }}</p>
          </section>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.interview-page {
  min-height: calc(100vh - 124px);
}

.hero-panel,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px;
  margin-bottom: 18px;
}

.hero-title {
  display: flex;
  gap: 10px;
  align-items: center;
}

.hero-title .el-icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #2563eb;
  font-size: 22px;
}

.hero-title h1 {
  margin: 0;
  font-size: 24px;
}

.hero-panel p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.content-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 0.9fr) minmax(0, 1.2fr);
  gap: 18px;
  align-items: start;
}

.panel {
  padding: 20px;
}

.panel h2 {
  margin: 0 0 16px;
  font-size: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-head h2 {
  margin: 0;
}

.setup-panel .el-button {
  width: 100%;
}

.summary-box {
  padding: 12px;
  margin-top: 16px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  color: #1e3a8a;
  background: #eff6ff;
  line-height: 1.6;
}

.tips-box {
  margin-top: 18px;
}

.tips-box h3,
.feedback-card h3,
.feedback-card h4 {
  margin: 0;
}

.tips-box ul,
.feedback-card ul {
  padding-left: 18px;
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.8;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-item {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #1f2937;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
}

.question-item.active,
.question-item:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.question-item strong {
  color: #2563eb;
}

.question-item span {
  line-height: 1.6;
}

.current-question {
  padding: 14px;
  margin-bottom: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
  color: #111827;
  font-weight: 600;
  line-height: 1.7;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.feedback-card {
  padding: 16px;
  margin-top: 18px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fafc;
}

.score-row {
  display: flex;
  gap: 18px;
  align-items: center;
}

.score-row h3 {
  margin-bottom: 10px;
  line-height: 1.5;
}

.feedback-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.feedback-grid section,
.sample-answer {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.sample-answer {
  margin-top: 16px;
}

.sample-answer p {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .hero-panel,
  .score-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .feedback-grid {
    grid-template-columns: 1fr;
  }
}
</style>
