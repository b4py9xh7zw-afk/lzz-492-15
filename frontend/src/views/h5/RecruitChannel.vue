<template>
  <div class="recruit-channel-page">
    <!-- 质量回算摘要卡片 -->
    <div class="mb-3 mx-2 rounded-lg bg-gradient-to-r from-blue-500 to-indigo-500 p-4 text-white shadow-sm">
      <div class="flex items-center justify-between mb-2">
        <span class="text-sm font-medium opacity-90">渠道质量回算（按真实到岗 × 稳定率结算）</span>
        <van-button
          size="mini"
          round
          plain
          color="#ffffff"
          :loading="recalcing"
          @click="handleRecalc"
        >回算</van-button>
      </div>
      <div class="flex justify-around text-center">
        <div>
          <div class="text-lg font-bold">{{ totalStats.onboard }}</div>
          <div class="text-xs opacity-80">真实到岗</div>
        </div>
        <div>
          <div class="text-lg font-bold">{{ totalStats.retained }}</div>
          <div class="text-xs opacity-80">留任7天</div>
        </div>
        <div>
          <div class="text-lg font-bold">{{ totalStats.avgStability }}%</div>
          <div class="text-xs opacity-80">平均稳定率</div>
        </div>
        <div>
          <div class="text-lg font-bold">¥{{ totalStats.settle }}</div>
          <div class="text-xs opacity-80">回算结算</div>
        </div>
      </div>
    </div>

    <van-search
      v-model="searchForm.channelName"
      placeholder="请输入渠道名称搜索"
      @search="handleSearch"
      @clear="handleSearch"
      class="mb-2"
    />

    <div class="fixed bottom-20 right-4 z-50">
      <van-button
        type="primary"
        round
        icon="plus"
        size="large"
        @click="handleAdd"
        class="shadow-lg"
      >
        新增
      </van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <div
          v-for="item in list"
          :key="item.id"
          class="channel-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-semibold text-gray-800 flex-1">{{ item.channelName }}</h3>
              <van-tag
                :type="item.status === 'active' ? 'success' : 'danger'"
                class="ml-2 rounded-full"
              >
                {{ item.status === 'active' ? '合作中' : '已暂停' }}
              </van-tag>
            </div>
            <div class="flex items-center gap-2 mb-3">
              <van-tag size="small" class="rounded-full">{{ getChannelTypeText(item.channelType) }}</van-tag>
              <span class="text-xs text-gray-500">单价 ¥{{ formatMoney(item.settlePrice) }}/人</span>
            </div>

            <!-- 质量回算数据 -->
            <div v-if="qualityMap[item.id]" class="rounded-lg bg-gray-50 p-3 mb-3">
              <div class="grid grid-cols-4 gap-1 text-center text-xs">
                <div>
                  <div class="font-semibold text-gray-800">{{ qualityMap[item.id].registeredCount }}</div>
                  <div class="text-gray-400">报名</div>
                </div>
                <div>
                  <div class="font-semibold text-gray-800">{{ qualityMap[item.id].onboardCount }}</div>
                  <div class="text-gray-400">到岗</div>
                </div>
                <div>
                  <div class="font-semibold" :class="qualityMap[item.id].stabilityRate >= 60 ? 'text-green-600' : 'text-red-500'">
                    {{ qualityMap[item.id].stabilityRate }}%
                  </div>
                  <div class="text-gray-400">稳定率</div>
                </div>
                <div>
                  <div class="font-semibold text-gray-800">¥{{ formatMoney(qualityMap[item.id].settleAmount) }}</div>
                  <div class="text-gray-400">结算</div>
                </div>
              </div>
              <van-progress
                :percentage="Number(qualityMap[item.id].stabilityRate)"
                :color="qualityMap[item.id].stabilityRate >= 60 ? '#07c160' : '#ee0a24'"
                stroke-width="6"
                class="mt-2"
              />
              <div v-if="qualityMap[item.id].suggestPause && item.status !== 'paused'" class="mt-2 text-xs text-red-500">
                ⚠ 稳定率过低，建议暂停该渠道
              </div>
            </div>

            <!-- 暂停原因展示 -->
            <div v-if="item.status === 'paused' && item.pauseReason" class="rounded-lg bg-red-50 p-2 mb-3 text-xs text-red-500">
              暂停原因：{{ item.pauseReason }}
            </div>

            <div class="flex items-center justify-end gap-2">
              <van-button size="small" type="primary" plain @click="handleEdit(item)" class="rounded-full">编辑</van-button>
              <van-button
                v-if="item.status === 'active'"
                size="small"
                type="warning"
                plain
                @click="handlePause(item)"
                class="rounded-full"
              >暂停</van-button>
              <van-button
                v-else
                size="small"
                type="success"
                plain
                @click="handleResume(item)"
                class="rounded-full"
              >恢复</van-button>
              <van-button size="small" type="danger" plain @click="handleDelete(item)" class="rounded-full">删除</van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑渠道弹窗 -->
    <van-popup
      v-model:show="popupVisible"
      position="bottom"
      :style="{ height: '75%' }"
      round
      closeable
      close-icon-position="top-right"
    >
      <div class="popup-content p-4">
        <h3 class="text-lg font-bold text-center mb-4">{{ popupTitle }}</h3>
        <van-form @submit="handleSubmit" ref="formRef">
          <van-cell-group inset>
            <van-field
              v-model="formData.channelName"
              name="channelName"
              label="渠道名称"
              placeholder="请输入渠道名称"
              :rules="[{ required: true, message: '请输入渠道名称' }]"
            />
            <van-field
              v-model="channelTypeDisplay"
              name="channelType"
              label="渠道类型"
              placeholder="请选择渠道类型"
              is-link
              readonly
              @click="showTypePicker = true"
              :rules="[{ required: true, message: '请选择渠道类型' }]"
            />
            <van-field
              v-model="formData.settlePrice"
              name="settlePrice"
              label="结算单价"
              type="number"
              placeholder="元/每个真实到岗且稳定的人"
              :rules="[{ required: true, message: '请输入结算单价' }]"
            />
            <van-field
              v-model="formData.contactPerson"
              name="contactPerson"
              label="联系人"
              placeholder="请输入联系人"
            />
            <van-field
              v-model="formData.contactPhone"
              name="contactPhone"
              label="联系电话"
              placeholder="请输入联系电话"
            />
            <van-field
              v-model="formData.remark"
              name="remark"
              label="备注"
              type="textarea"
              rows="2"
              placeholder="请输入备注"
            />
          </van-cell-group>
          <div class="p-4">
            <van-button round block type="primary" native-type="submit" :loading="submitting" class="mb-3">
              {{ formData.id ? '更新' : '新增' }}
            </van-button>
            <van-button round block @click="popupVisible = false">取消</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 渠道类型选择器 -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 暂停原因弹窗 -->
    <van-dialog
      v-model:show="pauseDialogVisible"
      title="暂停渠道"
      show-cancel-button
      :before-close="onPauseConfirm"
    >
      <div class="p-4">
        <van-field
          v-model="pauseReason"
          type="textarea"
          rows="3"
          placeholder="请填写暂停原因（必填），例如：稳定率连续低于40%"
          class="rounded-lg bg-gray-50"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import { recruitChannelApi } from '@/api/recruit'

const loading = ref(false)
const submitting = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const recalcing = ref(false)
const popupVisible = ref(false)
const popupTitle = ref('新增渠道')
const formRef = ref(null)
const showTypePicker = ref(false)

const list = ref([])
const qualityMap = ref({})

// 暂停
const pauseDialogVisible = ref(false)
const pauseReason = ref('')
const pausingChannel = ref(null)

const searchForm = reactive({
  channelName: ''
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
  settlePrice: null,
  contactPerson: '',
  contactPhone: '',
  remark: ''
})

const typeColumns = [
  { text: '门店海报', value: 'store_poster' },
  { text: '短视频投放', value: 'short_video' },
  { text: '劳务中介', value: 'labor_agency' },
  { text: '熟人推荐', value: 'referral' }
]

const channelTypeDisplay = computed(() => {
  const item = typeColumns.find(t => t.value === formData.channelType)
  return item ? item.text : ''
})

// 汇总统计
const totalStats = computed(() => {
  const qualities = Object.values(qualityMap.value)
  const onboard = qualities.reduce((s, q) => s + (q.onboardCount || 0), 0)
  const retained = qualities.reduce((s, q) => s + (q.retainedCount || 0), 0)
  const settle = qualities.reduce((s, q) => s + Number(q.settleAmount || 0), 0)
  const avgStability = onboard > 0 ? Math.round(retained * 100 / onboard) : 0
  return {
    onboard,
    retained,
    avgStability,
    settle: settle.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
})

const loadData = async () => {
  try {
    const res = await recruitChannelApi.page({
      current: pagination.current,
      size: pagination.size,
      channelName: searchForm.channelName || undefined
    })
    if (res.code === 200) {
      if (pagination.current === 1) {
        list.value = res.data.records
      } else {
        list.value.push(...res.data.records)
      }
      pagination.total = res.data.total
      if (list.value.length >= res.data.total) {
        finished.value = true
      } else {
        pagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    showToast('加载失败')
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载质量回算数据
const loadQuality = async () => {
  try {
    const res = await recruitChannelApi.quality()
    if (res.code === 200) {
      const map = {}
      res.data.forEach(q => { map[q.channelId] = q })
      qualityMap.value = map
    }
  } catch (error) {
    console.error(error)
  }
}

// 执行回算并存档
const handleRecalc = async () => {
  recalcing.value = true
  try {
    const res = await recruitChannelApi.recalc()
    if (res.code === 200) {
      const map = {}
      res.data.forEach(q => { map[q.channelId] = q })
      qualityMap.value = map
      showSuccessToast('回算完成')
    }
  } catch (error) {
    console.error(error)
  } finally {
    recalcing.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  finished.value = false
  list.value = []
  loadData()
}

const onRefresh = () => {
  handleSearch()
  loadQuality()
}

const handleAdd = () => {
  popupTitle.value = '新增渠道'
  Object.assign(formData, {
    id: null,
    channelName: '',
    channelType: '',
    settlePrice: null,
    contactPerson: '',
    contactPhone: '',
    remark: ''
  })
  popupVisible.value = true
}

const handleEdit = (item) => {
  popupTitle.value = '编辑渠道'
  Object.assign(formData, {
    id: item.id,
    channelName: item.channelName,
    channelType: item.channelType,
    settlePrice: item.settlePrice,
    contactPerson: item.contactPerson || '',
    contactPhone: item.contactPhone || '',
    remark: item.remark || ''
  })
  popupVisible.value = true
}

const onTypeConfirm = ({ selectedOptions }) => {
  formData.channelType = selectedOptions[0].value
  showTypePicker.value = false
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    let res
    if (formData.id) {
      res = await recruitChannelApi.update(formData.id, formData)
    } else {
      res = await recruitChannelApi.save(formData)
    }
    if (res.code === 200) {
      showSuccessToast(formData.id ? '更新成功' : '新增成功')
      popupVisible.value = false
      handleSearch()
      loadQuality()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 暂停渠道
const handlePause = (item) => {
  pausingChannel.value = item
  pauseReason.value = ''
  pauseDialogVisible.value = true
}

const onPauseConfirm = async (action) => {
  if (action !== 'confirm') return true
  if (!pauseReason.value || !pauseReason.value.trim()) {
    showToast('请填写暂停原因')
    return false
  }
  try {
    const res = await recruitChannelApi.pause(pausingChannel.value.id, pauseReason.value.trim())
    if (res.code === 200) {
      showSuccessToast('渠道已暂停')
      handleSearch()
      loadQuality()
      return true
    }
    return false
  } catch (error) {
    console.error(error)
    return false
  }
}

// 恢复渠道
const handleResume = async (item) => {
  try {
    await showConfirmDialog({ title: '提示', message: `确定要恢复渠道「${item.channelName}」吗？` })
    const res = await recruitChannelApi.resume(item.id)
    if (res.code === 200) {
      showSuccessToast('渠道已恢复')
      handleSearch()
      loadQuality()
    }
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (item) => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定要删除该渠道吗？' })
    const res = await recruitChannelApi.delete(item.id)
    if (res.code === 200) {
      showSuccessToast('删除成功')
      handleSearch()
      loadQuality()
    }
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
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

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(() => {
  loadQuality()
})
</script>

<style scoped>
.recruit-channel-page {
  padding-bottom: 20px;
}

.channel-card {
  transition: transform 0.2s;
}

.channel-card:active {
  transform: scale(0.98);
}
</style>
