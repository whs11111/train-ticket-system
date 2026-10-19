<template>
  <div class="page">
    <div class="page-card">
      <div class="top-bar">
        <el-button class="back-btn" @click="$router.push('/adminHome')">←</el-button>
        <div class="title">车次管理</div>
      </div>

      <div class="table-box">
        <el-table
          :data="trains"
          border
          stripe
          style="width: 100%"
          header-align="center"
          align="center"
        >
          <el-table-column prop="trainId" label="ID" width="70" />

          <el-table-column label="车次编号" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.trainNo" />
            </template>
          </el-table-column>

          <el-table-column label="列车类型" width="150">
            <template #default="scope">
              <el-select v-model="scope.row.trainType">
                <el-option label="高铁" value="高铁" />
                <el-option label="动车" value="动车" />
                <el-option label="火车" value="火车" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column prop="startStationName" label="始发站" width="120" />

          <el-table-column prop="endStationName" label="终点站" width="120" />

          <el-table-column label="开行日期" width="220">
            <template #default="scope">
              <div class="schedule-box" @click="openSchedule(scope.row)">
                <el-tag v-for="item in scope.row.scheduleList" :key="item.runDate" type="primary">
                  {{ item.runDate }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="350">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openLine(scope.row)">
                线路票价管理
              </el-button>

              <el-button size="small" type="primary" @click="openCoach(scope.row)">
                查看席位
              </el-button>

              <el-button size="small" type="primary" @click="update(scope.row)"> 保存 </el-button>

              <el-button size="small" type="danger" @click="remove(scope.row.trainId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="add-btn">
        <el-button type="primary" @click="add">新增车次</el-button>
      </div>

      <el-dialog
        v-model="dialogVisible"
        title="新增车次"
        width="560px"
        :close-on-click-modal="false"
      >
        <el-form label-width="100px">
          <el-form-item label="车次编号">
            <el-input v-model="form.trainNo" />
          </el-form-item>

          <el-form-item label="列车类型">
            <el-select v-model="form.trainType">
              <el-option label="高铁" value="高铁" />
              <el-option label="动车" value="动车" />
              <el-option label="火车" value="火车" />
            </el-select>
          </el-form-item>

          <div class="seat-title">
            <span>席位配置</span>
            <small>每节车厢固定 50 个座位</small>
          </div>

          <el-form-item label="商务座车厢">
            <el-input-number
              v-model="form.businessCount"
              :min="0"
              :max="20"
              :precision="0"
              controls-position="right"
            />
            <span class="unit">节</span>
          </el-form-item>

          <el-form-item label="一等座车厢">
            <el-input-number
              v-model="form.firstCount"
              :min="0"
              :max="20"
              :precision="0"
              controls-position="right"
            />
            <span class="unit">节</span>
          </el-form-item>

          <el-form-item label="二等座车厢">
            <el-input-number
              v-model="form.secondCount"
              :min="0"
              :max="20"
              :precision="0"
              controls-position="right"
            />
            <span class="unit">节</span>
          </el-form-item>

          <div class="coach-total">
            共 {{ totalCoachCount }} 节车厢，共 {{ totalSeatCount }} 个座位
          </div>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">确定新增</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="scheduleDialogVisible" title="开行日期" width="700px">
        <el-calendar v-model="calendarDate">
          <template #date-cell="{ data }">
            <div
              class="calendar-day"
              :class="{ active: isSchedule(data.day) }"
              @click="clickDay(data.day)"
            >
              {{ data.day.substring(8) }}
            </div>
          </template>
        </el-calendar>

        <template #footer>
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSchedule"> 保存开行日期 </el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="lineDialogVisible" title="线路票价管理" width="1135px">
        <el-table :data="trainStations" border stripe align="center" header-align="center">
          <el-table-column prop="stopOrder" label="站序" width="70" />

          <el-table-column label="车站" width="150">
            <template #default="scope">
              <el-select
                v-model="scope.row.stationId"
                filterable
                clearable
                :filter-method="filterStation"
                @visible-change="handleStationVisibleChange"
              >
                <el-option
                  v-for="item in filteredStations"
                  :key="item.stationId"
                  :label="item.stationName"
                  :value="item.stationId"
                />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="到达时间" width="170">
            <template #default="scope">
              <el-input
                v-model="scope.row.arrivalTime"
                class="time-input"
                maxlength="5"
                @input="formatTime(scope.row, 'arrivalTime')"
                @blur="validateTime(scope.row, 'arrivalTime')"
                :disabled="scope.row.stopOrder === 1"
              />
            </template>
          </el-table-column>

          <el-table-column label="出发时间" width="170">
            <template #default="scope">
              <el-input
                v-model="scope.row.departureTime"
                class="time-input"
                maxlength="5"
                @input="formatTime(scope.row, 'departureTime')"
                @blur="validateTime(scope.row, 'departureTime')"
                :disabled="scope.row.stopOrder === trainStations.length"
              />
            </template>
          </el-table-column>

          <el-table-column label="商务座票价" width="150">
            <template #default="scope">
              <el-input
                v-if="scope.$index < trainStations.length - 1"
                v-model="scope.row.businessPrice"
                placeholder="请输入"
                @input="formatPrice(scope.row, 'businessPrice')"
                @blur="validatePrice(scope.row, 'businessPrice')"
              >
                <template #suffix>元</template>
              </el-input>

              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="一等座票价" width="150">
            <template #default="scope">
              <el-input
                v-if="scope.$index < trainStations.length - 1"
                v-model="scope.row.firstPrice"
                placeholder="请输入"
                @input="formatPrice(scope.row, 'firstPrice')"
                @blur="validatePrice(scope.row, 'firstPrice')"
              >
                <template #suffix>元</template>
              </el-input>

              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="二等座票价" width="150">
            <template #default="scope">
              <el-input
                v-if="scope.$index < trainStations.length - 1"
                v-model="scope.row.secondPrice"
                placeholder="请输入"
                @input="formatPrice(scope.row, 'secondPrice')"
                @blur="validatePrice(scope.row, 'secondPrice')"
              >
                <template #suffix>元</template>
              </el-input>

              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="90">
            <template #default="scope">
              <el-button size="small" type="danger" @click="deleteStation(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="price-tip">
          <span>提示：</span>
          每一行票价表示“本站 → 下一站”的票价，票价必须为非负整数。保存后自动累加并更新该车次所有区间的票价。
        </div>

        <el-button class="add-station-btn" type="primary" @click="addStation"> 添加车站 </el-button>

        <template #footer>
          <el-button @click="lineDialogVisible = false">取消</el-button>

          <el-button type="success" @click="saveLine"> 保存线路和票价 </el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="coachDialogVisible" title="席位设置" width="520px">
        <div class="coach-setting">
          <div class="coach-train">车次：{{ currentCoachTrain.trainNo }}</div>

          <div class="view-item">
            <span>商务座车厢</span>
            <strong>{{ coachInfo.businessCount }} 节</strong>
          </div>

          <div class="view-item">
            <span>一等座车厢</span>
            <strong>{{ coachInfo.firstCount }} 节</strong>
          </div>

          <div class="view-item">
            <span>二等座车厢</span>
            <strong>{{ coachInfo.secondCount }} 节</strong>
          </div>

          <div class="coach-total">
            共 {{ coachInfo.totalCoachCount }} 节车厢，共 {{ coachInfo.totalSeatCount }} 个座位
          </div>

          <div class="coach-tip">
            每节车厢固定 50 个座位，席位配置在新增车次时确定，后续不能修改。
          </div>
        </div>

        <template #footer>
          <el-button @click="coachDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'

import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pinyin } from 'pinyin-pro'

const trains = ref<any[]>([])
const allStations = ref<any[]>([])
const filteredStations = ref<any[]>([])
const trainStations = ref<any[]>([])

const currentTrainId = ref<number>()
const currentTrain = ref<any>(null)
const currentCoachTrain = ref<any>({})

const dialogVisible = ref(false)
const lineDialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const coachDialogVisible = ref(false)

const calendarDate = ref(new Date())
const schedules = ref<any[]>([])
const stationKeyword = ref('')

const currentStationRow = ref<any>(null)

const coachInfo = ref({
  businessCount: 0,
  firstCount: 0,
  secondCount: 0,
  totalCoachCount: 0,
  totalSeatCount: 0,
})

const form = ref({
  trainNo: '',
  trainType: '',
  businessCount: 0,
  firstCount: 0,
  secondCount: 0,
})

const totalCoachCount = computed(() => {
  return form.value.businessCount + form.value.firstCount + form.value.secondCount
})

const totalSeatCount = computed(() => {
  return totalCoachCount.value * 50
})

const load = async () => {
  const res = await request.get('/train/list')

  trains.value = res.data

  trains.value.forEach((train: any) => {
    train.scheduleList?.sort((a: any, b: any) => a.runDate.localeCompare(b.runDate))
  })
}

const loadStations = async () => {
  const res = await request.get('/station/list')

  allStations.value = res.data
  filteredStations.value = res.data
}

onMounted(() => {
  load()
  loadStations()
})

const add = () => {
  form.value = {
    trainNo: '',
    trainType: '',
    businessCount: 0,
    firstCount: 0,
    secondCount: 0,
  }

  dialogVisible.value = true
}

const submit = async () => {
  if (!/^[A-Z]?\d+$/.test(form.value.trainNo)) {
    ElMessage.error('车次编号格式错误')
    return
  }

  if (!form.value.trainType) {
    ElMessage.error('请选择列车类型')
    return
  }

  if (totalCoachCount.value === 0) {
    ElMessage.error('至少设置一种座位车厢')
    return
  }

  try {
    await request.post('/train/add', {
      trainNo: form.value.trainNo,
      trainType: form.value.trainType,
      businessCount: form.value.businessCount,
      firstCount: form.value.firstCount,
      secondCount: form.value.secondCount,
    })

    ElMessage.success('新增成功')
    dialogVisible.value = false

    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '车次编号已存在或席位生成失败')
  }
}

const update = async (row: any) => {
  try {
    await request.put('/train/update', row)

    ElMessage.success('修改成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '修改失败')
  }
}

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该车次？', '提示', {
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await request.delete(`/train/delete/${id}`)

    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '该车次已有售票记录，不能删除')
  }
}

const openLine = async (row: any) => {
  currentTrainId.value = row.trainId

  try {
    const stationRes = await request.get(`/train/${row.trainId}/stations`)

    const stationList = stationRes.data

    const priceRes = await request.get('/price/manage')

    const priceList = priceRes.data.filter((item: any) => item.trainId === row.trainId)

    trainStations.value = stationList.map((item: any, index: number) => {
      const nextStation = stationList[index + 1]

      let price: any = null

      if (nextStation) {
        price = priceList.find(
          (priceItem: any) =>
            priceItem.startStationId === item.stationId &&
            priceItem.endStationId === nextStation.stationId,
        )
      }

      return {
        ...item,

        arrivalTime: normalizeTime(item.arrivalTime),
        departureTime: normalizeTime(item.departureTime),

        businessPrice:
          price?.businessPrice !== null && price?.businessPrice !== undefined
            ? String(price.businessPrice)
            : '',

        firstPrice:
          price?.firstPrice !== null && price?.firstPrice !== undefined
            ? String(price.firstPrice)
            : '',

        secondPrice:
          price?.secondPrice !== null && price?.secondPrice !== undefined
            ? String(price.secondPrice)
            : '',
      }
    })

    filteredStations.value = allStations.value
    stationKeyword.value = ''
    currentStationRow.value = null

    lineDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '线路加载失败')
  }
}

const addStation = () => {
  trainStations.value.push({
    stationId: null,
    arrivalTime: '',
    departureTime: '',

    businessPrice: '',
    firstPrice: '',
    secondPrice: '',

    stopOrder: trainStations.value.length + 1,
  })
}

const deleteStation = (index: number) => {
  trainStations.value.splice(index, 1)

  trainStations.value.forEach((item, index) => {
    item.stopOrder = index + 1
  })
}

const normalizeTime = (value: any) => {
  if (!value) {
    return ''
  }

  const text = String(value)

  if (/^\d{2}:\d{2}:\d{2}$/.test(text)) {
    return text.substring(0, 5)
  }

  if (/^\d{2}:\d{2}$/.test(text)) {
    return text
  }

  const digits = text.replace(/\D/g, '')

  if (digits.length >= 4) {
    return digits.substring(0, 2) + ':' + digits.substring(2, 4)
  }

  return text
}

const formatTime = (row: any, field: 'arrivalTime' | 'departureTime') => {
  let value = row[field]

  if (value === null || value === undefined) {
    row[field] = ''
    return
  }

  value = String(value)

  let digits = value.replace(/\D/g, '')

  if (digits.length > 4) {
    digits = digits.substring(0, 4)
  }

  if (digits.length <= 2) {
    row[field] = digits
    return
  }

  row[field] = digits.substring(0, 2) + ':' + digits.substring(2, 4)
}

const validateTime = (row: any, field: 'arrivalTime' | 'departureTime') => {
  const value = row[field]

  if (!value) {
    return
  }

  const text = String(value)

  if (!/^\d{2}:\d{2}$/.test(text)) {
    ElMessage.error('请输入正确的时间')
    row[field] = ''
    return
  }

  const hour = Number(text.substring(0, 2))

  const minute = Number(text.substring(3, 5))

  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
    ElMessage.error('请输入正确的时间')
    row[field] = ''
  }
}

const isValidTime = (value: string) => {
  if (!/^\d{2}:\d{2}$/.test(value)) {
    return false
  }

  const hour = Number(value.substring(0, 2))

  const minute = Number(value.substring(3, 5))

  return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59
}

const formatPrice = (row: any, field: 'businessPrice' | 'firstPrice' | 'secondPrice') => {
  let value = row[field]

  if (value === null || value === undefined) {
    row[field] = ''
    return
  }

  value = String(value)

  if (/^\d+$/.test(value)) {
    value = value.replace(/^0+(?=\d)/, '')
  }

  row[field] = value
}

let priceValidationMessage: ReturnType<typeof ElMessage> | undefined

const validatePrice = (row: any, field: 'businessPrice' | 'firstPrice' | 'secondPrice') => {
  const text = String(row[field] ?? '').trim()

  if (!/^\d+$/.test(text) || !Number.isSafeInteger(Number(text))) {
    if (!priceValidationMessage) {
      priceValidationMessage = ElMessage({
        type: 'error',
        message: '票价必须为非负整数',
        onClose: () => {
          priceValidationMessage = undefined
        },
      })
    }
    return false
  }

  return true
}

const saveLine = async () => {
  for (const item of trainStations.value) {
    if (item.arrivalTime && !isValidTime(item.arrivalTime)) {
      ElMessage.error(`第 ${item.stopOrder} 站到达时间格式错误`)
      return
    }

    if (item.departureTime && !isValidTime(item.departureTime)) {
      ElMessage.error(`第 ${item.stopOrder} 站出发时间格式错误`)
      return
    }
  }

  if (trainStations.value.length < 2) {
    ElMessage.error('至少需要两个车站')
    return
  }

  for (let i = 0; i < trainStations.value.length; i++) {
    if (!trainStations.value[i].stationId) {
      ElMessage.error(`第 ${i + 1} 站请选择车站`)
      return
    }
  }

  const stationIds = trainStations.value.map((item: any) => item.stationId)

  if (new Set(stationIds).size !== stationIds.length) {
    ElMessage.error('线路中不能存在重复车站')
    return
  }

  for (let i = 0; i < trainStations.value.length - 1; i++) {
    const row = trainStations.value[i]

    if (!validatePrice(row, 'businessPrice')) {
      return
    }

    if (!validatePrice(row, 'firstPrice')) {
      return
    }

    if (!validatePrice(row, 'secondPrice')) {
      return
    }
  }

  try {
    await request.put(`/train/${currentTrainId.value}/stations`, trainStations.value)

    const segmentPrices = trainStations.value.slice(0, -1).map((start: any, i: number) => ({
      trainId: currentTrainId.value,
      startStationId: start.stationId,
      endStationId: trainStations.value[i + 1].stationId,
      businessPrice: Number(start.businessPrice),
      firstPrice: Number(start.firstPrice),
      secondPrice: Number(start.secondPrice),
    }))

    await request.post('/price/saveBatch', segmentPrices)

    ElMessage.success('线路和票价保存成功')

    lineDialogVisible.value = false

    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '线路或票价保存失败')
  }
}

const openSchedule = async (row: any) => {
  currentTrainId.value = row.trainId
  currentTrain.value = row

  const res = await request.get(`/schedule/${row.trainId}`)

  schedules.value = res.data
  scheduleDialogVisible.value = true
}

const isSchedule = (day: string) => {
  return schedules.value.some((item) => item.runDate === day)
}

const clickDay = (day: string) => {
  const index = schedules.value.findIndex((item) => item.runDate === day)

  if (index >= 0) {
    schedules.value.splice(index, 1)
  } else {
    schedules.value.push({
      runDate: day,
    })
  }

  schedules.value.sort((a, b) => a.runDate.localeCompare(b.runDate))
}

const saveSchedule = async () => {
  try {
    await request.put(`/schedule/${currentTrainId.value}`, schedules.value)

    currentTrain.value.scheduleList = schedules.value.map((item: any) => ({
      runDate: item.runDate,
    }))

    ElMessage.success('开行日期保存成功')

    scheduleDialogVisible.value = false
  } catch {
    ElMessage.error('该开行日期已生成座位，不能删除')
  }
}

const openCoach = async (row: any) => {
  currentCoachTrain.value = row

  try {
    const res = await request.get('/coach/config', {
      params: {
        trainId: row.trainId,
      },
    })

    coachInfo.value = {
      businessCount: res.data.businessCount || 0,

      firstCount: res.data.firstCount || 0,

      secondCount: res.data.secondCount || 0,

      totalCoachCount:
        res.data.totalCoachCount ||
        (res.data.businessCount || 0) + (res.data.firstCount || 0) + (res.data.secondCount || 0),

      totalSeatCount:
        res.data.totalSeatCount ||
        ((res.data.businessCount || 0) + (res.data.firstCount || 0) + (res.data.secondCount || 0)) *
          50,
    }
  } catch {
    coachInfo.value = {
      businessCount: 0,
      firstCount: 0,
      secondCount: 0,
      totalCoachCount: 0,
      totalSeatCount: 0,
    }
  }

  coachDialogVisible.value = true
}

const filterStation = (query: string) => {
  const keyword = String(query || '')
    .trim()
    .toLowerCase()

  stationKeyword.value = keyword

  if (!keyword) {
    filteredStations.value = allStations.value
    return
  }

  filteredStations.value = allStations.value.filter((station: any) => {
    const stationName = String(station.stationName || '').toLowerCase()

    const initials = pinyin(station.stationName || '', {
      pattern: 'first',
      toneType: 'none',
      type: 'array',
    })
      .join('')
      .toLowerCase()

    return stationName.includes(keyword) || initials.startsWith(keyword)
  })

  currentStationRow.value = getCurrentStationRow()
}

const getCurrentStationRow = () => {
  const active = document.activeElement as HTMLElement | null

  if (!active) {
    return null
  }

  const select = active.closest('.el-select')

  if (!select) {
    return null
  }

  const selects = Array.from(document.querySelectorAll('.el-dialog .el-table .el-select'))

  const index = selects.indexOf(select)

  if (index < 0 || !trainStations.value[index]) {
    return null
  }

  return trainStations.value[index]
}

const selectStationBySpace = () => {
  if (filteredStations.value.length !== 1) {
    return
  }

  const station = filteredStations.value[0]

  let row = currentStationRow.value

  if (!row) {
    row = getCurrentStationRow()
  }

  if (!row) {
    return
  }

  row.stationId = station.stationId

  stationKeyword.value = ''
  filteredStations.value = allStations.value

  nextTick(() => {
    const input = document.querySelector(
      '.el-select-dropdown__wrap input',
    ) as HTMLInputElement | null

    if (input) {
      input.value = ''
    }
  })
}

const handleStationVisibleChange = (visible: boolean) => {
  if (!visible) {
    currentStationRow.value = null
    return
  }

  nextTick(() => {
    bindStationSearchInput()
  })
}

const bindStationSearchInput = () => {
  const inputs = document.querySelectorAll('.el-select-dropdown input')

  inputs.forEach((element) => {
    const input = element as HTMLInputElement

    if (input.dataset.stationSpaceBound === 'true') {
      return
    }

    input.dataset.stationSpaceBound = 'true'

    input.addEventListener('keydown', handleStationSearchKeydown, true)

    input.addEventListener('input', handleStationSearchInput)

    input.addEventListener('focus', handleStationSearchFocus)
  })
}

const handleStationSearchFocus = () => {
  nextTick(() => {
    currentStationRow.value = getCurrentStationRow()
  })
}

const handleStationSearchInput = (event: Event) => {
  const input = event.target as HTMLInputElement

  const value = input.value

  filterStation(value)

  currentStationRow.value = getCurrentStationRow()
}

const handleStationSearchKeydown = (event: KeyboardEvent) => {
  if (event.key !== ' ' && event.code !== 'Space') {
    return
  }

  event.preventDefault()
  event.stopPropagation()
  event.stopImmediatePropagation()

  currentStationRow.value = getCurrentStationRow()

  selectStationBySpace()
}

const removeStationListeners = () => {
  const inputs = document.querySelectorAll('.el-select-dropdown input')

  inputs.forEach((element) => {
    const input = element as HTMLInputElement

    input.removeEventListener('keydown', handleStationSearchKeydown, true)

    input.removeEventListener('input', handleStationSearchInput)

    input.removeEventListener('focus', handleStationSearchFocus)

    delete input.dataset.stationSpaceBound
  })
}

onBeforeUnmount(() => {
  removeStationListeners()
})
</script>

<style scoped>
.page {
  width: 100%;
  min-height: 100vh;
  padding: 40px 0;
  box-sizing: border-box;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
}

.page-card {
  width: 1220px;
  margin: 0 auto;
  padding: 25px 30px 40px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 8px;
}

.top-bar {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 35px;
}

.back-btn {
  position: absolute;
  left: 0;
  width: 45px;
  height: 45px;
  font-size: 22px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  text-align: center;
  letter-spacing: 2px;
}

.title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  margin: 12px auto 0;
  background: #409eff;
  border-radius: 5px;
}

.table-box {
  width: 100%;
  overflow: hidden;
  border: 1px solid #dcecff;
  border-radius: 10px;
}

:deep(.el-table) {
  --el-table-header-bg-color: #eaf3ff;
  --el-table-row-hover-bg-color: #f8fbff;
}

:deep(.el-table th.el-table__cell) {
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  height: 55px;
}

:deep(.el-table td.el-table__cell) {
  height: 58px;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}

:deep(.el-input-number) {
  width: 180px;
}

.add-btn {
  margin-top: 20px;
}

.add-btn .el-button {
  height: 40px;
  padding: 0 22px;
  font-size: 15px;
}

.schedule-box {
  min-height: 35px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  cursor: pointer;
  align-items: center;
  justify-content: flex-start;
}

.schedule-box .el-tag {
  cursor: pointer;
  border-radius: 5px;
}

.calendar-day {
  width: 100%;
  height: 100%;
  min-height: 72px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 6px;
  font-size: 20px;
  font-weight: 500;
  transition: 0.2s;
}

.calendar-day:hover {
  background: #ecf5ff;
}

.calendar-day.active {
  background: #409eff;
  color: #fff;
}

:deep(.el-calendar-day) {
  padding: 0 !important;
  height: 100%;
}

.add-station-btn {
  margin-top: 15px;
}

.time-input {
  width: 140px;
}

:deep(.time-input .el-input__wrapper) {
  height: 36px;
  padding: 0 11px;
}

:deep(.time-input input) {
  text-align: center;
  font-size: 15px;
  letter-spacing: 1px;
}

.seat-title {
  margin: 25px 0 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  color: #1f4e8c;
  font-size: 17px;
  font-weight: 600;
}

.seat-title small {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
  font-weight: normal;
}

.unit {
  margin-left: 12px;
  color: #606266;
}

.coach-total {
  margin-top: 5px;
  padding: 12px;
  text-align: center;
  color: #1f4e8c;
  background: #eaf3ff;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
}

.coach-setting {
  padding: 10px 30px 20px;
}

.coach-train {
  margin-bottom: 25px;
  font-size: 18px;
  font-weight: 600;
  color: #1f4e8c;
  text-align: center;
}

.view-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 15px 18px;
  background: #f8fbff;
  border: 1px solid #e4effc;
  border-radius: 8px;
  color: #606266;
}

.view-item strong {
  color: #1f4e8c;
  font-size: 16px;
}

.coach-tip {
  margin-top: 15px;
  padding: 12px;
  text-align: center;
  color: #909399;
  background: #f5f9ff;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.price-tip {
  margin-top: 15px;
  padding: 12px 15px;
  background: #f5f9ff;
  border: 1px solid #e4effc;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.price-tip span {
  color: #1f4e8c;
  font-weight: 600;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  color: #1f4e8c;
}

:deep(.el-dialog__title) {
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>
