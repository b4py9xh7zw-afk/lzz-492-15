<template>
  <div class="recruit-channel-page">
    <!-- 渠道质量回算看板 -->
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <span class="text-xl font-bold text-gray-800">渠道质量回算</span>
            <span class="ml-3 text-sm text-gray-500">结算口径：真实到岗 × 结算单价 × 稳定率（不按报名人数结算）</span>
          </div>
          <div class="flex gap-2">
            <el-button
              type="primary"
              @click="handleRecalc"
              :loading="recalcing"
              class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <el-icon><Refresh /></el-icon>
              执行回算并存档
            </el-button>
            <el-button
              @click="historyVisible = true; loadHistory()"
              class="rounded-lg"
            >
              <el-icon><Clock /></el-icon>
              回算历史
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="qualityList"
        v-loading="qualityLoading"
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="channelName" label="渠道" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ row.channelName }}</span>
              <el-tag v-if="row.status === 'paused'" type="danger" size="small" class="rounded-full">已暂停</el-tag>
            </div>
            <div class="text-xs text-gray-400 mt-1">{{ getChannelTypeText(row.channelType) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="转化漏斗（报名→面试→试岗→到岗→留任7天）" min-width="280">
          <template #default="{ row }">
            <div class="funnel-bar flex items-center gap-1 text-xs">
              <div class="funnel-step bg-blue-100 text-blue-700" :style="{ flexGrow: Math.max(row.registeredCount, 1) }">
                报名 {{ row.registeredCount }}
              </div>
              <el-icon class="text-gray-300"><ArrowRight /></el-icon>
              <div class="funnel-step bg-cyan-100 text-cyan-700" :style="{ flexGrow: Math.max(row.interviewCount, 1) }">
                面试 {{ row.interviewCount }}
              </div>
              <el-icon class="text-gray-300"><ArrowRight /></el-icon>
              <div class="funnel-step bg-teal-100 text-teal-700" :style="{ flexGrow: Math.max(row.trialCount, 1) }">
                试岗 {{ row.trialCount }}
              </div>
              <el-icon class="text-gray-300"><ArrowRight /></el-icon>
              <div class="funnel-step bg-green-100 text-green-700" :style="{ flexGrow: Math.max(row.onboardCount, 1) }">
                到岗 {{ row.onboardCount }}
              </div>
              <el-icon class="text-gray-300"><ArrowRight /></el-icon>
              <div class="funnel-step bg-emerald-200 text-emerald-800" :style="{ flexGrow: Math.max(row.retainedCount, 1) }">
                留任 {{ row.retainedCount }}
              </div>
            </div>
            <div class="text-xs text-gray-400 mt-1" v-if="row.resignedCount > 0">离职 {{ row.resignedCount }} 人</div>
          </template>
        </el-table-column>
        <el-table-column label="到岗率" width="110" align="center">
          <template #default="{ row }">
            <span class="font-semibold" :class="row.arrivalRate >= 50 ? 'text-green-600' : 'text-orange-500'">
              {{ row.arrivalRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column label="稳定率" width="110" align="center">
          <template #default="{ row }">
            <span class="font-semibold" :class="row.stabilityRate >= 60 ? 'text-green-600' : 'text-red-500'">
              {{ row.stabilityRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column label="质量评级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getQualityType(row.qualityLevel)" class="rounded-full px-3">
              {{ getQualityText(row.qualityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回算结算金额" width="130" align="right">
          <template #default="{ row }">
            <span class="font-bold text-gray-800">¥{{ formatMoney(row.settleAmount) }}</span>
            <div class="text-xs text-gray-400">单价 ¥{{ formatMoney(row.settlePrice) }}/人</div>
          </template>
        </el-table-column>
        <el-table-column label="运营建议" min-width="140">
          <template #default="{ row }">
            <el-alert
              v-if="row.suggestPause && row.status !== 'paused'"
              type="error"
              :closable="false"
              class="rounded-lg py-1"
            >
              <span class="text-xs">稳定率过低，建议暂停</span>
            </el-alert>
            <span v-else-if="row.status === 'paused'" class="text-xs text-gray-400">
              已暂停：{{ row.pauseReason || '-' }}
            </span>
            <span v-else class="text-xs text-gray-400">正常合作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 渠道管理 -->
    <el-card class="shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">渠道管理</span>
          <el-button
            type="primary"
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增渠道
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="渠道名称">
          <el-input
            v-model="searchForm.channelName"
            placeholder="请输入渠道名称"
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="渠道类型">
          <el-select
            v-model="searchForm.channelType"
            placeholder="请选择渠道类型"
            clearable
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="门店海报" value="store_poster" />
            <el-option label="短视频投放" value="short_video" />
            <el-option label="劳务中介" value="labor_agency" />
            <el-option label="熟人推荐" value="referral" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="合作中" value="active" />
            <el-option label="已暂停" value="paused" />
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
        <el-table-column prop="channelName" label="渠道名称" min-width="150" />
        <el-table-column prop="channelType" label="渠道类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getChannelTypeTagType(row.channelType)" class="rounded-full px-3">
              {{ getChannelTypeText(row.channelType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settlePrice" label="结算单价" width="110" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.settlePrice) }}/人</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" class="rounded-full px-3">
              {{ row.status === 'active' ? '合作中' : '已暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="暂停原因" min-width="160">
          <template #default="{ row }">
            <span v-if="row.status === 'paused'" class="text-red-500 text-sm">{{ row.pauseReason || '-' }}</span>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'active'"
              type="warning"
              size="small"
              link
              @click="handlePause(row)"
            >暂停</el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="handleResume(row)"
            >恢复</el-button>
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

    <!-- 新增/编辑渠道对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="formData.channelName" placeholder="请输入渠道名称" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-select v-model="formData.channelType" placeholder="请选择渠道类型" class="w-full rounded-lg">
            <el-option label="门店海报" value="store_poster" />
            <el-option label="短视频投放" value="short_video" />
            <el-option label="劳务中介" value="labor_agency" />
            <el-option label="熟人推荐" value="referral" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算单价" prop="settlePrice">
          <el-input-number
            v-model="formData.settlePrice"
            :min="0"
            :precision="2"
            :step="50"
            class="w-full"
            controls-position="right"
          />
          <div class="text-xs text-gray-400 mt-1">单位：元/每个真实到岗且稳定留任的人</div>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="formData.contactPerson" placeholder="请输入联系人" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" class="rounded-lg" />
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

    <!-- 暂停渠道对话框 -->
    <el-dialog
      v-model="pauseDialogVisible"
      title="暂停渠道"
      width="480px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-alert type="warning" :closable="false" class="rounded-lg mb-4">
        暂停后该渠道将无法继续报名，请填写暂停原因以便后续复盘。
      </el-alert>
      <el-form :model="pauseForm" :rules="pauseRules" ref="pauseFormRef" label-width="90px">
        <el-form-item label="渠道名称">
          <span class="font-medium">{{ pauseForm.channelName }}</span>
        </el-form-item>
        <el-form-item label="暂停原因" prop="pauseReason">
          <el-input
            v-model="pauseForm.pauseReason"
            type="textarea"
            :rows="4"
            placeholder="例如：连续两周稳定率低于40%，到岗人员流失严重"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="pauseDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button type="warning" @click="handlePauseSubmit" :loading="submitting" class="rounded-lg">确认暂停</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回算历史对话框 -->
    <el-dialog
      v-model="historyVisible"
      title="回算历史快照"
      width="900px"
      class="rounded-lg"
    >
      <el-table
        :data="historyList"
        v-loading="historyLoading"
        border
        size="small"
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="statDate" label="回算日期" width="110" />
        <el-table-column prop="channelId" label="渠道" min-width="140">
          <template #default="{ row }">{{ channelNameMap[row.channelId] || row.channelId }}</template>
        </el-table-column>
        <el-table-column prop="registeredCount" label="报名" width="70" align="center" />
        <el-table-column prop="onboardCount" label="到岗" width="70" align="center" />
        <el-table-column prop="retainedCount" label="留任7天" width="80" align="center" />
        <el-table-column prop="resignedCount" label="离职" width="70" align="center" />
        <el-table-column prop="arrivalRate" label="到岗率" width="90" align="center">
          <template #default="{ row }">{{ row.arrivalRate }}%</template>
        </el-table-column>
        <el-table-column prop="stabilityRate" label="稳定率" width="90" align="center">
          <template #default="{ row }">{{ row.stabilityRate }}%</template>
        </el-table-column>
        <el-table-column prop="settleAmount" label="结算金额" width="110" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.settleAmount) }}</template>
        </el-table-column>
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="historyPagination.current"
          v-model:page-size="historyPagination.size"
          :total="historyPagination.total"
          layout="total, prev, pager, next"
          @current-change="loadHistory"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Clock, ArrowRight } from '@element-plus/icons-vue'
import { recruitChannelApi } from '@/api/recruit'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增渠道')
const formRef = ref(null)

// 质量回算看板
const qualityLoading = ref(false)
const recalcing = ref(false)
const qualityList = ref([])
const channelNameMap = ref({})

// 暂停
const pauseDialogVisible = ref(false)
const pauseFormRef = ref(null)
const pauseForm = reactive({ id: null, channelName: '', pauseReason: '' })
const pauseRules = {
  pauseReason: [{ required: true, message: '请填写暂停原因', trigger: 'blur' }]
}

// 回算历史
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])
const historyPagination = reactive({ current: 1, size: 10, total: 0 })

const searchForm = reactive({
  channelName: '',
  channelType: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  channelName: '',
  channelType: '',
  settlePrice: 300,
  contactPerson: '',
  contactPhone: '',
  remark: ''
})

const formRules = {
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  channelType: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  settlePrice: [{ required: true, message: '请输入结算单价', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await recruitChannelApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
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

// 加载质量回算看板（实时回算）
const loadQuality = async () => {
  qualityLoading.value = true
  try {
    const res = await recruitChannelApi.quality()
    if (res.code === 200) {
      qualityList.value = res.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('质量回算失败')
  } finally {
    qualityLoading.value = false
  }
}

// 执行回算并保存快照
const handleRecalc = async () => {
  recalcing.value = true
  try {
    const res = await recruitChannelApi.recalc()
    if (res.code === 200) {
      qualityList.value = res.data
      ElMessage.success('回算完成，快照已存档')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('回算失败')
  } finally {
    recalcing.value = false
  }
}

// 加载回算历史
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res = await recruitChannelApi.recalcHistory({
      current: historyPagination.current,
      size: historyPagination.size
    })
    if (res.code === 200) {
      historyList.value = res.data.records
      historyPagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载回算历史失败')
  } finally {
    historyLoading.value = false
  }
}

// 加载渠道名称映射（历史快照展示用）
const loadChannelNames = async () => {
  try {
    const res = await recruitChannelApi.list()
    if (res.code === 200) {
      const map = {}
      res.data.forEach(c => { map[c.id] = c.channelName })
      channelNameMap.value = map
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, { channelName: '', channelType: '', status: '' })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增渠道'
  Object.assign(formData, {
    id: null,
    channelName: '',
    channelType: '',
    settlePrice: 300,
    contactPerson: '',
    contactPhone: '',
    remark: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑渠道'
  try {
    const res = await recruitChannelApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        channelName: res.data.channelName,
        channelType: res.data.channelType,
        settlePrice: res.data.settlePrice,
        contactPerson: res.data.contactPerson || '',
        contactPhone: res.data.contactPhone || '',
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
          res = await recruitChannelApi.update(formData.id, formData)
        } else {
          res = await recruitChannelApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          refreshAll()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '新增失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 暂停渠道
const handlePause = (row) => {
  Object.assign(pauseForm, { id: row.id, channelName: row.channelName, pauseReason: '' })
  pauseDialogVisible.value = true
  if (pauseFormRef.value) pauseFormRef.value.clearValidate()
}

const handlePauseSubmit = async () => {
  if (!pauseFormRef.value) return
  await pauseFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await recruitChannelApi.pause(pauseForm.id, pauseForm.pauseReason)
        if (res.code === 200) {
          ElMessage.success('渠道已暂停')
          pauseDialogVisible.value = false
          refreshAll()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 恢复渠道
const handleResume = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要恢复渠道「${row.channelName}」吗？`, '提示', { type: 'warning' })
    const res = await recruitChannelApi.resume(row.id)
    if (res.code === 200) {
      ElMessage.success('渠道已恢复')
      refreshAll()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该渠道吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await recruitChannelApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      refreshAll()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const refreshAll = () => {
  loadData()
  loadQuality()
  loadChannelNames()
}

const getChannelTypeText = (type) => {
  const texts = {
    store_poster: '门店海报',
    short_video: '短视频投放',
    labor_agency: '劳务中介',
    referral: '熟人推荐'
  }
  return texts[type] || type
}

const getChannelTypeTagType = (type) => {
  const types = {
    store_poster: '',
    short_video: 'warning',
    labor_agency: 'danger',
    referral: 'success'
  }
  return types[type] || ''
}

const getQualityType = (level) => {
  const types = { excellent: 'success', good: '', medium: 'warning', poor: 'danger' }
  return types[level] || 'info'
}

const getQualityText = (level) => {
  const texts = { excellent: '优', good: '良', medium: '中', poor: '差' }
  return texts[level] || level
}

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
  refreshAll()
})
</script>

<style scoped>
.recruit-channel-page {
  max-width: 1400px;
  margin: 0 auto;
}

.funnel-step {
  padding: 4px 8px;
  border-radius: 6px;
  text-align: center;
  white-space: nowrap;
  min-width: 52px;
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
