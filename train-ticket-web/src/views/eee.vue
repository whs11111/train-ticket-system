<template>
  <div class="home">
    <h1 class="title">欢迎来到12704火车票购票系统！</h1>

    <div class="nav-bar">
      <div class="nav-item active" @click="goTicket">车票查询</div>

      <div class="nav-item" @click="goOrder">我的订单</div>

      <div class="nav-item" @click="goUser">个人中心</div>

      <div class="nav-item logout" @click="logout">退出登录</div>
    </div>

    <div class="search-box">
      <el-input v-model="startPlace" placeholder="请输入出发城市或车站" size="large" />

      <el-button @click="exchangeCity" size="large"> ⇄ </el-button>

      <el-input v-model="endPlace" placeholder="请输入到达城市或车站" size="large" />

      <el-date-picker
        v-model="date"
        type="date"
        placeholder="请选择日期"
        value-format="YYYY-MM-DD"
        size="large"
      />

      <el-button type="primary" @click="searchTicket" size="large"> 查询车票 </el-button>
    </div>

    <div class="search-mode-box">
      <div
        class="search-mode-item"
        :class="{ active: searchMode === 'direct' }"
        @click="selectMode('direct')"
      >
        <span class="mode-title">直达</span>
      </div>

      <div class="mode-divider"></div>

      <div
        class="search-mode-item"
        :class="{ active: searchMode === 'transfer' }"
        @click="selectMode('transfer')"
      >
        <span class="mode-title">换乘</span>
      </div>
    </div>

    <div v-if="searchMode === 'direct'" class="ticket-list">
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
            <div>
              {{ ticket.trainNo }}
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

            <el-button type="primary" size="small" @click.stop="book(ticket, item)">
              预订
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="transfer-list">
      <div class="transfer-header">
        <div>出发</div>
        <div>第一程</div>
        <div>换乘</div>
        <div>第二程</div>
        <div>到达</div>
        <div>总历时</div>
      </div>

      <div
        v-for="(ticket, index) in transferTickets"
        :key="index"
        class="transfer-item"
        @click="bookTransfer(ticket)"
      >
        <div class="transfer-main">
          <div class="transfer-time-block">
            <div class="transfer-time">
              {{ formatTime(ticket.totalDepartureTime) }}
            </div>

            <div class="transfer-station">
              {{ ticket.startStation }}
            </div>
          </div>

          <div class="transfer-train-block">
            <div class="train-number">
              {{ ticket.firstTrainNo }}
            </div>

            <div class="train-line">
              <i>━━━━━━━━━▶</i>
            </div>

            <div class="transfer-duration">{{ formatTime(ticket.firstArrivalTime) }} 到达</div>
          </div>

          <div class="transfer-station-block">
            <div class="transfer-station-name">
              {{ ticket.transferStation }}
            </div>

            <div class="transfer-wait">
              换乘
              {{ calculateTransferTime(ticket.firstArrivalTime, ticket.secondDepartureTime) }}
            </div>
          </div>

          <div class="transfer-train-block">
            <div class="train-number">
              {{ ticket.secondTrainNo }}
            </div>

            <div class="train-line">
              <i>━━━━━━━━━━▶</i>
            </div>

            <div class="transfer-duration">{{ formatTime(ticket.secondDepartureTime) }} 出发</div>
          </div>

          <div class="transfer-time-block">
            <div class="transfer-time">
              {{ formatTime(ticket.totalArrivalTime) }}
            </div>

            <div class="transfer-station">
              {{ ticket.endStation }}
            </div>
          </div>

          <div class="transfer-total-duration">
            {{ ticket.totalDuration }}
          </div>
        </div>

        <div class="transfer-seat-box">
          <div class="seat-section">
            <div class="seat-section-title">
              <span class="section-train">
                {{ ticket.firstTrainNo }}
              </span>

              <span>第一程</span>
            </div>

            <div class="seat-list">
              <div v-for="seat in ticket.firstSeats" :key="seat.seatTypeId" class="seat-item">
                <span class="seat-name">
                  {{ seat.seatType }}
                </span>

                <span class="seat-remain"> {{ seat.remain }}张 </span>

                <span class="seat-price"> {{ seat.price }} 元 </span>
              </div>
            </div>
          </div>

          <div class="seat-divider"></div>

          <div class="seat-section">
            <div class="seat-section-title">
              <span class="section-train">
                {{ ticket.secondTrainNo }}
              </span>

              <span>第二程</span>
            </div>

            <div class="seat-list">
              <div v-for="seat in ticket.secondSeats" :key="seat.seatTypeId" class="seat-item">
                <span class="seat-name">
                  {{ seat.seatType }}
                </span>

                <span class="seat-remain"> {{ seat.remain }}张 </span>

                <span class="seat-price"> {{ seat.price }} 元 </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="transferTickets.length === 0 && !transferLoading" class="transfer-empty">
        <div class="empty-icon">⇄</div>

        <div class="empty-title">暂无换乘车次</div>

        <div class="empty-text">当前条件下没有找到合适的换乘方案</div>
      </div>

      <div v-if="transferLoading" class="transfer-empty">
        <div class="empty-icon loading-icon">...</div>

        <div class="empty-title">正在查询换乘车次</div>

        <div class="empty-text">请稍候</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()

const userStore = useUserStore()

const startPlace = ref('')
const endPlace = ref('')
const date = ref('')

const searchMode = ref<'direct' | 'transfer'>('direct')

const tickets = ref<any[]>(JSON.parse(sessionStorage.getItem('tickets') || '[]'))

const transferTickets = ref<any[]>(JSON.parse(sessionStorage.getItem('transferTickets') || '[]'))

const expandedRows = ref<number[]>(JSON.parse(sessionStorage.getItem('expandedRows') || '[]'))

const transferLoading = ref(false)

const savedParams = JSON.parse(sessionStorage.getItem('searchParams') || '{}')

startPlace.value = savedParams.startCity || ''
endPlace.value = savedParams.endCity || ''
date.value = savedParams.date || ''

onMounted(() => {
  const savedMode = sessionStorage.getItem('searchMode')

  if (savedMode === 'transfer') {
    searchMode.value = 'transfer'
    loadTransferTickets()
  } else {
    searchMode.value = 'direct'
    loadTickets()
  }
})

const logout = async () => {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', {
    type: 'warning',
  })

  userStore.setUser({})

  sessionStorage.clear()

  router.push('/')
}

const goTicket = () => {
  router.push('/home')
}

const goOrder = () => {
  router.push('/myOrder')
}

const goUser = () => {
  router.push('/profile')
}

const exchangeCity = () => {
  const temp = startPlace.value

  startPlace.value = endPlace.value
  endPlace.value = temp
}

const selectMode = async (mode: 'direct' | 'transfer') => {
  searchMode.value = mode

  expandedRows.value = []

  sessionStorage.setItem('searchMode', mode)

  if (mode === 'direct') {
    loadTickets()
  } else {
    loadTransferTickets()
  }
}

const handleRowClick = (row: any) => {
  const index = expandedRows.value.indexOf(row.scheduleId)

  if (index > -1) {
    expandedRows.value.splice(index, 1)
  } else {
    expandedRows.value.push(row.scheduleId)
  }

  sessionStorage.setItem('expandedRows', JSON.stringify(expandedRows.value))
}

const loadTickets = async () => {
  if (!startPlace.value || !endPlace.value || !date.value) {
    return
  }

  try {
    const res = await request.post('/ticket/search', {
      startPlace: startPlace.value,
      endPlace: endPlace.value,
      date: date.value,
    })

    tickets.value = res.data

    sessionStorage.setItem('tickets', JSON.stringify(tickets.value))
  } catch (e) {
    tickets.value = []

    sessionStorage.setItem('tickets', JSON.stringify([]))
  }
}

const loadTransferTickets = async () => {
  if (!startPlace.value || !endPlace.value || !date.value) {
    transferTickets.value = []
    return
  }

  transferLoading.value = true

  try {
    const res = await request.post('/ticket/searchTransfer', {
      startPlace: startPlace.value,
      endPlace: endPlace.value,
      date: date.value,
    })

    transferTickets.value = res.data

    sessionStorage.setItem('transferTickets', JSON.stringify(transferTickets.value))
  } catch (e) {
    transferTickets.value = []

    sessionStorage.setItem('transferTickets', JSON.stringify([]))

    ElMessage.error('换乘车次查询失败')
  } finally {
    transferLoading.value = false
  }
}

const searchTicket = async () => {
  searchMode.value = 'direct'

  sessionStorage.setItem('searchMode', 'direct')

  expandedRows.value = []

  sessionStorage.removeItem('expandedRows')

  sessionStorage.setItem(
    'searchParams',
    JSON.stringify({
      startCity: startPlace.value,
      endCity: endPlace.value,
      date: date.value,
    }),
  )

  await loadTickets()
}

const formatTime = (time: string) => {
  if (!time) {
    return ''
  }

  return time.substring(0, 5)
}

const calculateTransferTime = (arrival: string, departure: string) => {
  if (!arrival || !departure) {
    return ''
  }

  const arrivalTime = arrival.substring(0, 5)

  const departureTime = departure.substring(0, 5)

  const arrivalParts = arrivalTime.split(':').map(Number)

  const departureParts = departureTime.split(':').map(Number)

  const arrivalHour = arrivalParts[0] ?? 0

  const arrivalMinute = arrivalParts[1] ?? 0

  const departureHour = departureParts[0] ?? 0

  const departureMinute = departureParts[1] ?? 0

  const arrivalMinutes = arrivalHour * 60 + arrivalMinute

  let departureMinutes = departureHour * 60 + departureMinute

  if (departureMinutes < arrivalMinutes) {
    departureMinutes += 24 * 60
  }

  const minutes = departureMinutes - arrivalMinutes

  const hour = Math.floor(minutes / 60)

  const minute = minutes % 60

  if (hour > 0) {
    return `${hour}时${minute}分`
  }

  return `${minute}分`
}

const book = (ticket: any, price: any) => {
  router.push({
    path: '/order',
    query: {
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

const bookTransfer = (ticket: any) => {
  router.push({
    path: '/transferOrder',
    query: {
      date: date.value,

      firstScheduleId: ticket.firstScheduleId,

      firstTrainNo: ticket.firstTrainNo,

      firstStartStation: ticket.startStation,

      firstEndStation: ticket.transferStation,

      firstDepartureTime: ticket.totalDepartureTime,

      firstArrivalTime: ticket.firstArrivalTime,

      firstSeats: JSON.stringify(ticket.firstSeats || []),

      secondScheduleId: ticket.secondScheduleId,

      secondTrainNo: ticket.secondTrainNo,

      secondStartStation: ticket.transferStation,

      secondEndStation: ticket.endStation,

      secondDepartureTime: ticket.secondDepartureTime,

      secondArrivalTime: ticket.totalArrivalTime,

      secondSeats: JSON.stringify(ticket.secondSeats || []),
    },
  })
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 35px 50px;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 35%);
}

.title {
  text-align: center;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #1f4e8c;
  margin: 10px 0 25px;
}

.nav-bar {
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 70px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 35px;
}

.nav-item {
  height: 60px;
  line-height: 60px;
  font-size: 18px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: 0.3s;
}

.nav-item:hover {
  color: #409eff;
}

.nav-item.active {
  color: #409eff;
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 10%;
  width: 80%;
  height: 3px;
  background: #409eff;
  border-radius: 5px;
}

.search-box {
  width: 85%;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 25px;
  background: white;
  border-radius: 18px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
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

.search-box .el-button {
  height: 45px;
  padding: 0 28px;
  border-radius: 10px;
  font-size: 16px;
}

.search-mode-box {
  width: 48%;
  height: 58px;
  margin: 0 auto 35px;
  display: flex;
  align-items: stretch;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e4edf8;
  box-shadow: 0 5px 18px rgba(31, 78, 140, 0.07);
  overflow: hidden;
}

.search-mode-item {
  flex: 1;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  background: #ffffff;
  transition: all 0.25s ease;
}

.search-mode-item:hover {
  background: #f6faff;
}

.search-mode-item.active {
  background: #eaf4ff;
}

.search-mode-item.active::before {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 0;
  height: 3px;
  background: #409eff;
  border-radius: 3px 3px 0 0;
}

.mode-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  line-height: 22px;
  transition: 0.25s;
}

.search-mode-item.active .mode-title {
  color: #1f4e8c;
}

.mode-divider {
  width: 1px;
  height: 28px;
  align-self: center;
  background: #e8edf3;
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

.train-row > div:first-child {
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

.logout {
  color: #f56c6c;
}

.logout:hover {
  color: #ff7875;
}

.transfer-list {
  width: 92%;
  margin: auto;
}

.transfer-header {
  display: grid;
  grid-template-columns:
    1.1fr
    1.3fr
    1.1fr
    1.3fr
    1.1fr
    0.8fr;
  align-items: center;
  text-align: center;
  height: 55px;
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  border-radius: 12px 12px 0 0;
}

.transfer-item {
  background: white;
  margin-bottom: 15px;
  border-radius: 14px;
  box-shadow: 0 5px 18px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: 0.3s;
  cursor: pointer;
}

.transfer-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.transfer-main {
  min-height: 105px;
  display: grid;
  grid-template-columns:
    1.1fr
    1.3fr
    1.1fr
    1.3fr
    1.1fr
    0.8fr;
  align-items: center;
  text-align: center;
  padding: 15px 10px;
  box-sizing: border-box;
}

.transfer-time-block {
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.transfer-time {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 30px;
}

.transfer-station {
  margin-top: 7px;
  font-size: 17px;
  color: #606266;
  line-height: 20px;
}

.transfer-train-block {
  margin-top: -16px;
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.train-number {
  font-size: 18px;
  font-weight: 600;
  color: #1f4e8c;
  line-height: 22px;
  margin-bottom: -10px;
}

.train-line {
  height: 22px;
  margin: 3px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.train-line i {
  font-size: 15px;
  font-style: normal;
  font-weight: 500;
  color: #409eff;
  letter-spacing: -1px;
  white-space: nowrap;
}

.transfer-duration {
  margin-top: -8px;
  font-size: 13px;
  color: #909399;
  line-height: 18px;
}

.transfer-station-block {
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.transfer-station-name {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  line-height: 28px;
  white-space: nowrap;
}

.transfer-wait {
  margin-top: 7px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
  white-space: nowrap;
}

.transfer-total-duration {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  line-height: 22px;
  white-space: nowrap;
}

.transfer-seat-box {
  width: 92%;
  margin: 0 auto 18px;
  padding: 15px 20px;
  display: grid;
  grid-template-columns: 1fr 1px 1fr;
  gap: 20px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  box-sizing: border-box;
}

.seat-section {
  min-width: 0;
}

.seat-section-title {
  height: 35px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #909399;
}

.section-train {
  color: #1f4e8c;
  font-size: 15px;
  font-weight: 600;
}

.seat-list {
  width: 100%;
}

.seat-item {
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 35px;
  border-top: 1px solid #ebeef5;
}

.seat-name {
  width: 80px;
  text-align: center;
  font-weight: 600;
  color: #303133;
}

.seat-item .seat-remain {
  width: 60px;
}

.seat-item .seat-price {
  width: 80px;
  font-size: 15px;
}

.seat-divider {
  width: 1px;
  background: #e4e7ed;
}

.transfer-empty {
  width: 92%;
  min-height: 260px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #edf2f7;
  box-shadow: 0 5px 18px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.empty-icon {
  width: 52px;
  height: 52px;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #eaf4ff;
  color: #409eff;
  font-size: 26px;
  font-weight: 500;
}

.loading-icon {
  font-size: 22px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
  color: #a8abb2;
}
</style>
