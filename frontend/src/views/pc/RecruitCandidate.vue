<template>
  <div class="recruit-candidate-page">
    <el-card class="shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">候选人管理</span>
          <el-button
            type="primary"
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增报名
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.candidateName"
            placeholder="请输入候选人姓名"
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="来源渠道">
          <el-select
            v-model="searchForm.channelId"
            placeholder="请选择渠道"
            clearable
            filterable
            style="width: 200px"
            class="rounded-lg"
          >
            <el-option
              v-for="c in channelOptions"
              :key="c.id"
              :label="c.channelName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="当前阶段">
          <el-select
            v-model="searchForm.stage"
            placeholder="请选择阶段"
            clearable
            style="width: 160px"
            class="rounded-lg"
          >
            <el-option label="已报名" value="registered" />
            <el-option label="已面试" value="interviewed" />
            <el-option label="试岗中" value="trial" />
            <el-option label="已到岗" value="onboard" />
            <el-option label="留任七天" value="retained" />
            <el-option label="已离职" value="resigned" />
            <el-option label="已淘汰" value="eliminated" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">查询</el-button>
          <el-button @click="handleReset" class="rounded-lg">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="candidateName" label="姓名" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="来源渠道" min-width="150">
          <template #default="{ row }">
            {{ channelNameMap[row.channelId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="应聘岗位" width="120" />
        <el-table-column label="当前阶段" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStageType(row.stage)" class="rounded-full px-3">
              {{ getStageText(row.stage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流程时间" min-width="220">
          <template #default="{ row }">
            <div class="text-xs text-gray-500 space-y-1">
              <div>报名：{{ formatDateTime(row.applyTime) }}</div>
              <div v-if="row.interviewTime">面试：{{ formatDateTime(row.interviewTime) }}</div>
              <div v-if="row.trialTime">试岗：{{ formatDateTime(row.trialTime) }}</div>
              <div v-if="row.onboardTime">到岗：{{ formatDateTime(row.onboardTime) }}</div>
              <div v-if="row.retainTime">留任7天：{{ formatDateTime(row.retainTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="离职原因" min-width="150">
          <template #default="{ row }">
            <span v-if="row.resignReason" class="text-red-500 text-sm">{{ row.resignReason }}</span>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="nextStageAction(row.stage)"
              type="primary"
              size="small"
              link
              @click="handleAdvance(row)"
            >{{ nextStageText(row.stage) }}</el-button>
            <el-button
              v-if="['onboard', 'retained'].includes(row.stage)"
              type="warning"
              size="small"
              link
              @click="handleResign(row)"
            >离职</el-button>
            <el-button
              v-if="['registered', 'interviewed', 'trial'].includes(row.stage)"
              type="info"
              size="small"
              link
              @click="handleEliminate(row)"
            >淘汰</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑候选人对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-form-item label="姓名" prop="candidateName">
          <el-input v-model="formData.candidateName" placeholder="请输入姓名" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="来源渠道" prop="channelId">
          <el-select
            v-model="formData.channelId"
            placeholder="请选择来源渠道"
            filterable
            class="w-full rounded-lg"
            :disabled="!!formData.id"
          >
            <el-option
              v-for="c in activeChannelOptions"
              :key="c.id"
              :label="c.channelName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="应聘岗位" prop="position">
          <el-input v-model="formData.position" placeholder="请输入应聘岗位" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" class="rounded-lg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 离职登记对话框 -->
    <el-dialog
      v-model="resignDialogVisible"
      title="离职登记"
      width="480px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form :model="resignForm" :rules="resignRules" ref="resignFormRef" label-width="90px">
        <el-form-item label="候选人">
          <span class="font-medium">{{ resignForm.candidateName }}</span>
        </el-form-item>
        <el-form-item label="离职原因" prop="resignReason">
          <el-input
            v-model="resignForm.resignReason"
            type="textarea"
            :rows="4"
            placeholder="请填写离职原因，例如：工作强度大、薪资不达预期、个人原因等"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="resignDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="warning" @click="handleResignSubmit" :loading="submitting" class="rounded-lg">确认离职</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { recruitChannelApi, recruitCandidateApi } from '@/api/recruit'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增报名')
const formRef = ref(null)
const channelOptions = ref([])

// 离职登记
const resignDialogVisible = ref(false)
const resignFormRef = ref(null)
const resignForm = reactive({ id: null, candidateName: '', resignReason: '' })
const resignRules = {
  resignReason: [{ required: true, message: '请填写离职原因', trigger: 'blur' }]
}

const searchForm = reactive({
  candidateName: '',
  channelId: null,
  stage: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  candidateName: '',
  phone: '',
  channelId: null,
  position: '',
  remark: ''
})

const formRules = {
  candidateName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  channelId: [{ required: true, message: '请选择来源渠道', trigger: 'change' }],
  position: [{ required: true, message: '请输入应聘岗位', trigger: 'blur' }]
}

// 渠道名称映射
const channelNameMap = computed(() => {
  const map = {}
  channelOptions.value.forEach(c => { map[c.id] = c.channelName })
  return map
})

// 新增报名时只允许选择合作中的渠道
const activeChannelOptions = computed(() => {
  return channelOptions.value.filter(c => c.status === 'active')
})

const loadChannels = async () => {
  try {
    const res = await recruitChannelApi.list()
    if (res.code === 200) {
      channelOptions.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      candidateName: searchForm.candidateName || undefined,
      channelId: searchForm.channelId || undefined,
      stage: searchForm.stage || undefined
    }
    const res = await recruitCandidateApi.page(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, { candidateName: '', channelId: null, stage: '' })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增报名'
  Object.assign(formData, {
    id: null,
    candidateName: '',
    phone: '',
    channelId: null,
    position: '',
    remark: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑候选人'
  try {
    const res = await recruitCandidateApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        candidateName: res.data.candidateName,
        phone: res.data.phone,
        channelId: res.data.channelId,
        position: res.data.position,
        remark: res.data.remark || ''
      })
      dialogVisible.value = true
      if (formRef.value) formRef.value.clearValidate()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await recruitCandidateApi.update(formData.id, formData)
        } else {
          res = await recruitCandidateApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '报名成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 阶段推进
const stageFlow = {
  registered: { action: 'interview', text: '安排面试', confirm: '确认已面试？' },
  interviewed: { action: 'trial', text: '安排试岗', confirm: '确认进入试岗？' },
  trial: { action: 'onboard', text: '确认到岗', confirm: '确认真实到岗？' },
  onboard: { action: 'retain', text: '留任7天', confirm: '确认已稳定留任满7天？' }
}

const nextStageAction = (stage) => stageFlow[stage]?.action
const nextStageText = (stage) => stageFlow[stage]?.text

const handleAdvance = async (row) => {
  const flow = stageFlow[row.stage]
  if (!flow) return
  try {
    await ElMessageBox.confirm(`${flow.confirm}（${row.candidateName}）`, '阶段推进', { type: 'info' })
    const res = await recruitCandidateApi.advanceStage(row.id, flow.action)
    if (res.code === 200) {
      ElMessage.success('阶段推进成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 淘汰
const handleEliminate = async (row) => {
  try {
    await ElMessageBox.confirm(`确定淘汰候选人「${row.candidateName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await recruitCandidateApi.advanceStage(row.id, 'eliminate')
    if (res.code === 200) {
      ElMessage.success('已淘汰')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 离职登记
const handleResign = (row) => {
  Object.assign(resignForm, { id: row.id, candidateName: row.candidateName, resignReason: '' })
  resignDialogVisible.value = true
  if (resignFormRef.value) resignFormRef.value.clearValidate()
}

const handleResignSubmit = async () => {
  if (!resignFormRef.value) return
  await resignFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await recruitCandidateApi.resign(resignForm.id, resignForm.resignReason)
        if (res.code === 200) {
          ElMessage.success('离职登记成功')
          resignDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该候选人吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await recruitCandidateApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const getStageType = (stage) => {
  const types = {
    registered: 'info',
    interviewed: '',
    trial: 'warning',
    onboard: 'success',
    retained: 'success',
    resigned: 'danger',
    eliminated: 'info'
  }
  return types[stage] || 'info'
}

const getStageText = (stage) => {
  const texts = {
    registered: '已报名',
    interviewed: '已面试',
    trial: '试岗中',
    onboard: '已到岗',
    retained: '留任七天',
    resigned: '已离职',
    eliminated: '已淘汰'
  }
  return texts[stage] || stage
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('MM-DD HH:mm')
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadChannels()
  loadData()
})
</script>

<style scoped>
.recruit-candidate-page {
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
