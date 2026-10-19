<template>
  <div class="change-page">
    <div class="change-card">
      <div class="header-box">
        <el-button class="back-btn" @click="goBack">←</el-button>

        <div class="title">改签</div>
      </div>

      <div class="search-box">
        <el-input v-model="startCity" disabled size="large" />

        <span class="exchange">→</span>

        <el-input v-model="endCity" disabled size="large" />

        <el-date-picker
          v-model="date"
          type="date"
          placeholder="请选择改签日期"
          value-format="YYYY-MM-DD"
          size="large"
        />

        <el-button type="primary" size="large" @click="searchTicket"> 查询车票 </el-button>
      </div>

      <div class="ticket-list">
        <div class="header-row">
          <div>车次</div>
          <div>出发站</div>
          <div>到达站</div>
          <div>出发时间</div>
          <div>到达时间</div>
          <div>历时</div>
        </div>

        <div v-for="ticket in tickets" :key="ticket.scheduleId" class="ticket-item">
          <div class="ticket-main" @click="handleRowClick(ticket)">
            <div class="train-row">
              <div class="train-number-cell">
                <span class="train-number-link" @click.stop="showRoute(ticket)">
                  {{ ticket.trainNo }}
                </span>
              </div>

              <div class="station">
                <span class="station-type" :class="ticket.startType">
                  {{ ticket.startType }}
                </span>

                {{ ticket.startStation }}
              </div>

              <div class="station">
                <span class="station-type" :class="ticket.endType">
                  {{ ticket.endType }}
                </span>

                {{ ticket.endStation }}
              </div>

              <div>
                {{ formatTime(ticket.departureTime) }}
              </div>

              <div>
                {{ formatTime(ticket.arrivalTime) }}
              </div>

              <div>
                {{ ticket.duration }}
              </div>
            </div>

            <div v-if="!expandedRows.includes(ticket.scheduleId)" class="seat-summary">
              <span v-for="item in ticket.prices" :key="item.seatTypeId">
                {{ item.seatType }} {{ item.remain }}张
              </span>
            </div>
          </div>

          <div v-if="expandedRows.includes(ticket.scheduleId)" class="price-box">
            <div v-for="item in ticket.prices" :key="item.seatTypeId" class="price-item">
              <span class="seat-type">
                {{ item.seatType }}
              </span>

              <span class="seat-remain"> {{ item.remain }}张 </span>

              <span class="seat-price"> {{ item.price }} 元 </span>

              <el-button type="primary" size="small" @click.stop="change(ticket, item)">
                改签
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="routeVisible"
        class="route-window"
        :style="{
          left: routePosition.x + 'px',
          top: routePosition.y + 'px',
        }"
      >
        <div class="route-window-header" @mousedown="startDrag">
          <div class="route-title">{{ currentRouteTrainNo }} 线路</div>

          <div class="route-close" @mousedown.stop @click="closeRoute">×</div>
        </div>

        <div class="route-window-body">
          <div v-if="routeLoading" class="route-loading">正在加载线路...</div>

          <div v-else-if="routeStations.length === 0" class="route-empty">暂无线路信息</div>

          <div v-else class="route-table">
            <div class="route-table-header">
              <div>站序</div>
              <div>车站</div>
              <div>到达</div>
              <div>开车</div>
              <div>停靠时间</div>
            </div>

            <div
              v-for="(station, index) in routeStations"
              :key="index"
              class="route-table-row"
              :class="getRouteStationClass(station, index)"
            >
              <div class="route-index">{{ index + 1 }}</div>
              <div class="route-station">
                <span class="route-station-content">{{ station.stationName }}</span>
              </div>
              <div class="route-time">{{ formatTime(station.arrivalTime) }}</div>
              <div class="route-time">{{ formatTime(station.departureTime) }}</div>
              <div class="route-stop-time">{{ getStopDuration(station, index) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const route = useRoute()

const router = useRouter()

const oldOrderId = Number(route.query.oldOrderId)

const oldPrice = Number(route.query.oldPrice)

const startCity = ref(route.query.startStation as string)

const endCity = ref(route.query.endStation as string)

const date = ref(route.query.travelDate as string)

const tickets = ref<any[]>([])

const expandedRows = ref<number[]>([])

const routeVisible = ref(false)
const routeLoading = ref(false)
const currentRouteTrainNo = ref('')
const routeStations = ref<any[]>([])
const currentTicketStartStation = ref('')
const currentTicketEndStation = ref('')
const routePosition = ref({
  x: Math.max((window.innerWidth - 700) / 2, 100),
  y: 180,
})
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

const normalizeStationName = (value: string | null | undefined) => {
  if (!value) return ''
  return String(value).trim().replace(/\s+/g, '').replace(/站$/, '')
}

const isSameStation = (
  stationName: string | null | undefined,
  targetName: string | null | undefined,
) => {
  const station = normalizeStationName(stationName)
  const target = normalizeStationName(targetName)
  return !!station && !!target && station === target
}

const getRouteStartIndex = () =>
  routeStations.value.findIndex((station) =>
    isSameStation(station.stationName, currentTicketStartStation.value),
  )

const getRouteEndIndex = () =>
  routeStations.value.findIndex((station) =>
    isSameStation(station.stationName, currentTicketEndStation.value),
  )

const getRouteStationClass = (_station: any, index: number) => {
  const startIndex = getRouteStartIndex()
  const endIndex = getRouteEndIndex()
  const actualStartIndex = startIndex >= 0 ? startIndex : 0
  const actualEndIndex = endIndex >= 0 ? endIndex : routeStations.value.length - 1
  const minIndex = Math.min(actualStartIndex, actualEndIndex)
  const maxIndex = Math.max(actualStartIndex, actualEndIndex)

  if (index === actualStartIndex && actualStartIndex <= actualEndIndex) return ['route-start-row']
  if (index === actualEndIndex && actualStartIndex <= actualEndIndex) return ['route-end-row']
  if (index < minIndex) return ['route-passed-row']
  if (index > maxIndex) return ['route-after-row']
  return ['route-middle-row']
}

const showRoute = async (ticket: any) => {
  currentRouteTrainNo.value = ticket.trainNo
  currentTicketStartStation.value = ticket.startStation || ''
  currentTicketEndStation.value = ticket.endStation || ''
  routeStations.value = []
  routeVisible.value = true
  routePosition.value = {
    x: Math.max((window.innerWidth - 700) / 2, 100),
    y: 180,
  }
  routeLoading.value = true

  try {
    const res = await request.post('/ticket/route', {
      scheduleId: ticket.scheduleId,
      date: date.value,
    })
    routeStations.value = Array.isArray(res.data) ? res.data : []
  } catch (_error) {
    routeStations.value = []
    ElMessage.error('线路信息加载失败')
  } finally {
    routeLoading.value = false
  }
}

const closeRoute = () => {
  routeVisible.value = false
}

const startDrag = (event: MouseEvent) => {
  isDragging.value = true
  dragOffset.value = {
    x: event.clientX - routePosition.value.x,
    y: event.clientY - routePosition.value.y,
  }
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

const handleDrag = (event: MouseEvent) => {
  if (!isDragging.value) return

  const maxX = Math.max(window.innerWidth - 700, 0)
  const maxY = Math.max(window.innerHeight - 500, 0)
  const x = Math.min(Math.max(event.clientX - dragOffset.value.x, 0), maxX)
  const y = Math.min(Math.max(event.clientY - dragOffset.value.y, 0), maxY)
  routePosition.value = { x, y }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onBeforeUnmount(stopDrag)

const goBack = () => {
  router.back()
}

const handleRowClick = (row: any) => {
  const index = expandedRows.value.indexOf(row.scheduleId)

  if (index > -1) {
    expandedRows.value.splice(index, 1)
  } else {
    expandedRows.value.push(row.scheduleId)
  }
}

const loadTickets = async () => {
  if (!startCity.value || !endCity.value || !date.value) {
    return
  }

  const res = await request.post('/ticket/searchChange', {
    startStation: startCity.value,
    endStation: endCity.value,
    date: date.value,
  })

  tickets.value = res.data
}

const searchTicket = async () => {
  await loadTickets()

  expandedRows.value = []
}

const change = (ticket: any, price: any) => {
  router.push({
    path: '/order',
    query: {
      type: 'change',
      oldOrderId,
      oldPrice,
      scheduleId: ticket.scheduleId,
      trainNo: ticket.trainNo,
      startStation: ticket.startStation,
      endStation: ticket.endStation,
      departureTime: ticket.departureTime,
      arrivalTime: ticket.arrivalTime,
      seatTypeId: price.seatTypeId,
      date: date.value,
      prices: JSON.stringify(ticket.prices),
    },
  })
}

const formatTime = (time: string) => {
  if (!time) {
    return ''
  }

  return time.substring(0, 5)
}

const formatStopDuration = (duration: number | string | null | undefined) => {
  if (duration === null || duration === undefined || duration === '') return '-'

  const minutes = Number(duration)
  if (Number.isNaN(minutes)) return String(duration)
  if (minutes <= 0) return '—'
  if (minutes < 60) return `${minutes}分`

  const hour = Math.floor(minutes / 60)
  const minute = minutes % 60
  return minute === 0 ? `${hour}小时` : `${hour}小时${minute}分`
}

const calculateStopDuration = (
  arrival: string | null | undefined,
  departure: string | null | undefined,
) => {
  if (!arrival || !departure) return 0

  const arrivalParts = String(arrival).substring(0, 8).split(':').map(Number)
  const departureParts = String(departure).substring(0, 8).split(':').map(Number)
  const arrivalSeconds =
    (arrivalParts[0] || 0) * 3600 + (arrivalParts[1] || 0) * 60 + (arrivalParts[2] || 0)
  let departureSeconds =
    (departureParts[0] || 0) * 3600 +
    (departureParts[1] || 0) * 60 +
    (departureParts[2] || 0)

  if (departureSeconds < arrivalSeconds) departureSeconds += 24 * 3600
  return Math.floor((departureSeconds - arrivalSeconds) / 60)
}

const getStopDuration = (station: any, index: number) => {
  if (station.stopDuration !== null && station.stopDuration !== undefined && station.stopDuration !== '') {
    return formatStopDuration(station.stopDuration)
  }
  if (index === 0 || index === routeStations.value.length - 1) return '—'
  return formatStopDuration(calculateStopDuration(station.arrivalTime, station.departureTime))
}
</script>

<style scoped>
.change-page {
  width: 100%;
  min-height: 100vh;
  padding: 35px 50px;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 35%);
  box-sizing: border-box;
}

.change-card {
  width: 1200px;
  margin: 0 auto;
}

.header-box {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 10px 0 25px;
}

.back-btn {
  position: absolute;
  left: 0;
  width: 45px;
  height: 45px;
  font-size: 22px;
}

.title {
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #1f4e8c;
  text-align: center;
  margin: 0;
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

.search-box {
  width: 85%;
  margin: 0 auto 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 25px;
  background: white;
  border-radius: 18px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}

.search-box .el-input {
  width: 260px;
}

.search-box .el-input__inner {
  font-size: 16px;
}

.search-box .el-input__wrapper,
.search-box .el-date-editor {
  height: 45px;
  border-radius: 10px;
}

.search-box .el-date-editor {
  width: 230px;
}

.search-box .el-button {
  height: 45px;
  padding: 0 28px;
  border-radius: 10px;
  font-size: 16px;
}

.exchange {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  flex-shrink: 0;
}

.ticket-list {
  width: 92%;
  margin: auto;
}

.header-row,
.train-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  align-items: center;
  text-align: center;
}

.header-row {
  height: 55px;
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  border-radius: 12px 12px 0 0;
}

.ticket-item {
  background: white;
  margin-bottom: 15px;
  border-radius: 14px;
  box-shadow: 0 5px 18px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: 0.3s;
}

.ticket-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.train-row {
  height: 80px;
  font-size: 16px;
  color: #303133;
}

.train-row > div:first-child,
.train-number-link {
  font-size: 19px;
  font-weight: 700;
  color: #1f4e8c;
}

.train-row .station {
  font-size: 19px;
  font-weight: 600;
}

.train-row > div:nth-child(4),
.train-row > div:nth-child(5),
.train-row > div:nth-child(6) {
  font-size: 19px;
  font-weight: 600;
}

.train-number-link {
  text-decoration: underline;
  text-underline-offset: 3px;
  cursor: pointer;
  transition: 0.2s;
}

.train-number-link:hover {
  color: #409eff;
}

.ticket-main {
  cursor: pointer;
}

.ticket-main:hover {
  background: #f8fbff;
}

.station {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.station-type {
  font-size: 11px;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  color: white;
  border-radius: 4px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  font-weight: 600;
}

.始 {
  background: #e6a23c;
}

.终 {
  background: #67c23a;
}

.过 {
  background: #409eff;
}

.seat-summary {
  text-align: center;
  font-size: 13px;
  color: #909399;
  padding-bottom: 15px;
}

.seat-summary span {
  margin: 0 25px;
}

.price-box {
  width: 75%;
  margin: 10px auto 20px;
  padding: 18px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  box-sizing: border-box;
}

.price-item {
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 90px;
  border-bottom: 1px solid #ebeef5;
}

.price-item:last-child {
  border: none;
}

.seat-type {
  width: 120px;
  text-align: center;
  font-weight: 600;
}

.seat-remain {
  width: 100px;
  text-align: center;
  color: #67c23a;
}

.seat-price {
  width: 120px;
  text-align: center;
  color: #ff742e;
  font-size: 17px;
  font-weight: 600;
}

.price-item .el-button {
  border-radius: 8px;
}

.route-window {
  position: fixed;
  z-index: 9999;
  width: 700px;
  max-height: 500px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid #dcdfe6;
  overflow: hidden;
}

.route-window-header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  background: #f5f9ff;
  border-bottom: 1px solid #e4e7ed;
  cursor: move;
  user-select: none;
}

.route-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f4e8c;
}

.route-close {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  line-height: 30px;
}

.route-close:hover {
  background: #ebeef5;
  color: #303133;
}

.route-window-body {
  max-height: 448px;
  overflow-y: auto;
  padding: 15px;
  box-sizing: border-box;
}

.route-table {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.route-table-header,
.route-table-row {
  display: grid;
  grid-template-columns: 70px 1fr 120px 120px 120px;
  align-items: center;
  text-align: center;
}

.route-table-header {
  height: 45px;
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  font-size: 14px;
}

.route-table-row {
  min-height: 54px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
  color: #303133;
  background: #ffffff;
  transition: all 0.2s;
}

.route-table-row:hover {
  background: #f8fbff;
}

.route-start-row,
.route-end-row {
  background: #409eff !important;
  color: #ffffff !important;
  font-weight: 600;
}

.route-start-row:hover,
.route-end-row:hover {
  background: #337ecc !important;
}

.route-start-row .route-index,
.route-end-row .route-index,
.route-start-row .route-station,
.route-end-row .route-station,
.route-start-row .route-time,
.route-end-row .route-time,
.route-start-row .route-stop-time,
.route-end-row .route-stop-time {
  color: #ffffff !important;
  font-weight: 600;
}

.route-passed-row,
.route-after-row {
  background: #f5f5f5;
  color: #a8abb2;
}

.route-passed-row:hover,
.route-after-row:hover {
  background: #eeeeee;
}

.route-passed-row .route-index,
.route-passed-row .route-station,
.route-passed-row .route-time,
.route-passed-row .route-stop-time,
.route-after-row .route-index,
.route-after-row .route-station,
.route-after-row .route-time,
.route-after-row .route-stop-time {
  color: #b5b7ba;
}

.route-middle-row {
  background: #ffffff;
  color: #303133;
}

.route-middle-row .route-index {
  color: #909399;
}

.route-middle-row .route-station {
  color: #303133;
}

.route-middle-row .route-time,
.route-middle-row .route-stop-time {
  color: #606266;
}

.route-index {
  color: #909399;
}

.route-station {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.route-station-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
}

.route-time,
.route-stop-time {
  color: #606266;
}

.route-loading,
.route-empty {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}
</style>
