<template>
  <div class="home">
    <div class="welcome-user">
      <span class="welcome-icon">👤</span>
      <span>欢迎，{{ userName }}</span>
    </div>

    <h1 class="title">欢迎来到12704火车票购票系统！</h1>

    <div class="nav-bar">
      <div class="nav-item active" @click="goTicket">车票查询</div>
      <div class="nav-item" @click="goOrder">我的订单</div>
      <div class="nav-item" @click="goUser">个人中心</div>
      <div class="nav-item logout" @click="logout">退出登录</div>
    </div>

    <div class="search-box">
      <div class="place-input-wrapper" @click.stop>
        <el-input
          v-model="startPlace"
          placeholder="请输入出发城市或车站"
          size="large"
          @input="handleStartInput"
          @focus="searchStartPlace"
        />

        <div v-if="startSuggestions.length > 0" class="place-suggestion-box">
          <div
            v-for="item in startSuggestions"
            :key="item.type + '-' + item.name"
            class="place-suggestion-item"
            @mousedown.prevent="selectStartPlace(item)"
          >
            <div class="suggestion-main">
              <span class="suggestion-name">
                {{ item.name }}
              </span>

              <span
                class="suggestion-label"
                :class="item.type === 'city' ? 'city-label' : 'station-label'"
              >
                {{ item.type === 'city' ? '城市' : '车站' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <el-button @click="exchangeCity" size="large">⇄</el-button>

      <div class="place-input-wrapper" @click.stop>
        <el-input
          v-model="endPlace"
          placeholder="请输入到达城市或车站"
          size="large"
          @input="handleEndInput"
          @focus="searchEndPlace"
        />

        <div v-if="endSuggestions.length > 0" class="place-suggestion-box">
          <div
            v-for="item in endSuggestions"
            :key="item.type + '-' + item.name"
            class="place-suggestion-item"
            @mousedown.prevent="selectEndPlace(item)"
          >
            <div class="suggestion-main">
              <span class="suggestion-name">
                {{ item.name }}
              </span>

              <span
                class="suggestion-label"
                :class="item.type === 'city' ? 'city-label' : 'station-label'"
              >
                {{ item.type === 'city' ? '城市' : '车站' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <el-date-picker
        v-model="date"
        type="date"
        placeholder="请选择日期"
        value-format="YYYY-MM-DD"
        size="large"
        :disabled-date="disabledDate"
      />

      <el-button type="primary" @click="searchTicket" size="large">查询车票</el-button>
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
            <div class="route-index">
              {{ index + 1 }}
            </div>

            <div class="route-station">
              <span class="route-station-content">
                {{ station.stationName }}
              </span>
            </div>

            <div class="route-time">
              {{ formatTime(station.arrivalTime) }}
            </div>

            <div class="route-time">
              {{ formatTime(station.departureTime) }}
            </div>

            <div class="route-stop-time">
              {{ getStopDuration(station, index) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import request from '../api/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const userName = computed(() => {
  const user: any = userStore.user

  return user?.username || user?.userName || user?.name || user?.nickname || '用户'
})

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

const startSuggestions = ref<any[]>([])
const endSuggestions = ref<any[]>([])

const startPlaceType = ref<'city' | 'station' | ''>(savedParams.startPlaceType || '')
const endPlaceType = ref<'city' | 'station' | ''>(savedParams.endPlaceType || '')

const searchStartPlace = async () => {
  if (!startPlace.value.trim()) {
    startSuggestions.value = []
    startPlaceType.value = ''
    return
  }

  try {
    const res = await request.get('/station/search', {
      params: {
        keyword: startPlace.value.trim(),
      },
    })

    startSuggestions.value = res.data || []
  } catch (e) {
    startSuggestions.value = []
  }
}

const searchEndPlace = async () => {
  if (!endPlace.value.trim()) {
    endSuggestions.value = []
    endPlaceType.value = ''
    return
  }

  try {
    const res = await request.get('/station/search', {
      params: {
        keyword: endPlace.value.trim(),
      },
    })

    endSuggestions.value = res.data || []
  } catch (e) {
    endSuggestions.value = []
  }
}

const handleStartInput = () => {
  startPlaceType.value = ''
  searchStartPlace()
}

const handleEndInput = () => {
  endPlaceType.value = ''
  searchEndPlace()
}

const selectStartPlace = (item: any) => {
  startPlace.value = item.name
  startPlaceType.value = item.type
  startSuggestions.value = []
}

const selectEndPlace = (item: any) => {
  endPlace.value = item.name
  endPlaceType.value = item.type
  endSuggestions.value = []
}

const handlePlaceOutsideClick = () => {
  startSuggestions.value = []
  endSuggestions.value = []
}

const routeVisible = ref(false)
const routeLoading = ref(false)

const currentRouteTrainNo = ref('')

const routeStations = ref<any[]>([])

const routePosition = ref({
  x: Math.max((window.innerWidth - 700) / 2, 100),
  y: 180,
})

const isDragging = ref(false)

const dragOffset = ref({
  x: 0,
  y: 0,
})

const currentTicketStartStation = ref('')
const currentTicketEndStation = ref('')

const disabledDate = (time: Date) => {
  const today = new Date()

  today.setHours(0, 0, 0, 0)

  return time < today
}

const normalizeStationName = (value: string | null | undefined) => {
  if (!value) {
    return ''
  }

  return String(value).trim().replace(/\s+/g, '').replace(/站$/, '')
}

const isSameStation = (
  stationName: string | null | undefined,
  searchName: string | null | undefined,
) => {
  const station = normalizeStationName(stationName)
  const search = normalizeStationName(searchName)

  if (!station || !search) {
    return false
  }

  return station === search
}

const getRouteStartIndex = () => {
  if (!routeStations.value.length) {
    return -1
  }

  return routeStations.value.findIndex((station) =>
    isSameStation(station.stationName, currentTicketStartStation.value),
  )
}

const getRouteEndIndex = () => {
  if (!routeStations.value.length) {
    return -1
  }

  return routeStations.value.findIndex((station) =>
    isSameStation(station.stationName, currentTicketEndStation.value),
  )
}

const getRouteStationClass = (station: any, index: number) => {
  const startIndex = getRouteStartIndex()
  const endIndex = getRouteEndIndex()

  const classes: string[] = []

  const actualStartIndex = startIndex >= 0 ? startIndex : 0
  const actualEndIndex = endIndex >= 0 ? endIndex : routeStations.value.length - 1

  const minIndex = Math.min(actualStartIndex, actualEndIndex)
  const maxIndex = Math.max(actualStartIndex, actualEndIndex)

  if (index === actualStartIndex && actualStartIndex <= actualEndIndex) {
    classes.push('route-start-row')
    return classes
  }

  if (index === actualEndIndex && actualStartIndex <= actualEndIndex) {
    classes.push('route-end-row')
    return classes
  }

  if (index < minIndex) {
    classes.push('route-passed-row')
  } else if (index > maxIndex) {
    classes.push('route-after-row')
  } else {
    classes.push('route-middle-row')
  }

  return classes
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

  if (Array.isArray(ticket.stations)) {
    routeStations.value = ticket.stations
    routeLoading.value = false
    return
  }

  if (Array.isArray(ticket.routeStations)) {
    routeStations.value = ticket.routeStations
    routeLoading.value = false
    return
  }

  if (Array.isArray(ticket.stationList)) {
    routeStations.value = ticket.stationList
    routeLoading.value = false
    return
  }

  try {
    const res = await request.post('/ticket/route', {
      scheduleId: ticket.scheduleId,
      date: date.value,
    })

    routeStations.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
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
  if (!isDragging.value) {
    return
  }

  const windowWidth = 700
  const windowHeight = 500

  let x = event.clientX - dragOffset.value.x
  let y = event.clientY - dragOffset.value.y

  const maxX = Math.max(window.innerWidth - windowWidth, 0)
  const maxY = Math.max(window.innerHeight - windowHeight, 0)

  if (x < 0) {
    x = 0
  }

  if (y < 0) {
    y = 0
  }

  if (x > maxX) {
    x = maxX
  }

  if (y > maxY) {
    y = maxY
  }

  routePosition.value = {
    x,
    y,
  }
}

const stopDrag = () => {
  isDragging.value = false

  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('click', handlePlaceOutsideClick)
})

onMounted(() => {
  document.addEventListener('click', handlePlaceOutsideClick)

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
  const tempPlace = startPlace.value
  const tempType = startPlaceType.value

  startPlace.value = endPlace.value
  startPlaceType.value = endPlaceType.value

  endPlace.value = tempPlace
  endPlaceType.value = tempType

  startSuggestions.value = []
  endSuggestions.value = []
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
      startPlaceType: startPlaceType.value,
      endPlaceType: endPlaceType.value,
      date: date.value,
    })

    tickets.value = res.data || []

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
      startPlaceType: startPlaceType.value,
      endPlaceType: endPlaceType.value,
      date: date.value,
    })

    transferTickets.value = res.data || []

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

  startSuggestions.value = []
  endSuggestions.value = []

  sessionStorage.setItem(
    'searchParams',
    JSON.stringify({
      startCity: startPlace.value,
      endCity: endPlace.value,
      startPlaceType: startPlaceType.value,
      endPlaceType: endPlaceType.value,
      date: date.value,
    }),
  )

  await loadTickets()
}

const formatTime = (time: string | null | undefined) => {
  if (!time) {
    return ''
  }

  const value = String(time)

  if (value.length >= 5) {
    return value.substring(0, 5)
  }

  return value
}

const formatStopDuration = (duration: number | string | null | undefined) => {
  if (duration === null || duration === undefined || duration === '') {
    return '-'
  }

  const minutes = Number(duration)

  if (Number.isNaN(minutes)) {
    return String(duration)
  }

  if (minutes <= 0) {
    return '—'
  }

  if (minutes < 60) {
    return `${minutes}分`
  }

  const hour = Math.floor(minutes / 60)
  const minute = minutes % 60

  if (minute === 0) {
    return `${hour}小时`
  }

  return `${hour}小时${minute}分`
}

const calculateStopDuration = (
  arrival: string | null | undefined,
  departure: string | null | undefined,
) => {
  if (!arrival || !departure) {
    return 0
  }

  const arrivalValue = String(arrival).substring(0, 8)
  const departureValue = String(departure).substring(0, 8)

  const arrivalParts = arrivalValue.split(':').map(Number)
  const departureParts = departureValue.split(':').map(Number)

  const arrivalHour = arrivalParts[0] || 0
  const arrivalMinute = arrivalParts[1] || 0
  const arrivalSecond = arrivalParts[2] || 0

  const departureHour = departureParts[0] || 0
  const departureMinute = departureParts[1] || 0
  const departureSecond = departureParts[2] || 0

  const arrivalSeconds = arrivalHour * 3600 + arrivalMinute * 60 + arrivalSecond

  let departureSeconds = departureHour * 3600 + departureMinute * 60 + departureSecond

  if (departureSeconds < arrivalSeconds) {
    departureSeconds += 24 * 3600
  }

  return Math.floor((departureSeconds - arrivalSeconds) / 60)
}

const getStopDuration = (station: any, index: number) => {
  if (
    station.stopDuration !== null &&
    station.stopDuration !== undefined &&
    station.stopDuration !== ''
  ) {
    return formatStopDuration(station.stopDuration)
  }

  if (index === 0 || index === routeStations.value.length - 1) {
    return '—'
  }

  const minutes = calculateStopDuration(station.arrivalTime, station.departureTime)

  return formatStopDuration(minutes)
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
  position: relative;
  min-height: 100vh;
  padding: 35px 50px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 35%);
}

.welcome-user {
  position: absolute;
  top: 60px;
  right: 60px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e4edf8;
  border-radius: 22px;
  box-shadow: 0 4px 15px rgba(31, 78, 140, 0.08);
  color: #1f4e8c;
  font-size: 15px;
  font-weight: 600;
  user-select: none;
  white-space: nowrap;
}
.welcome-icon {
  font-size: 20px;
  line-height: 1;
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

.place-input-wrapper {
  position: relative;
  width: 260px;
}

.place-input-wrapper .el-input {
  width: 260px;
}

.place-suggestion-box {
  position: absolute;
  top: 52px;
  left: 0;
  width: 260px;
  max-height: 300px;
  overflow-y: auto;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid #e4edf8;
  border-radius: 10px;
  box-shadow: 0 8px 22px rgba(31, 78, 140, 0.12);
  z-index: 9999;
  padding: 5px 0;
}

.place-suggestion-item {
  position: relative;
  min-height: 38px;
  padding: 9px 14px;
  box-sizing: border-box;
  cursor: pointer;
  border-bottom: 1px solid #f2f5f9;
  transition: background 0.18s ease;
}

.place-suggestion-item:last-child {
  border-bottom: none;
}

.place-suggestion-item:hover {
  background: #f5f9ff;
}

.suggestion-main {
  display: flex;
  align-items: center;
  gap: 7px;
  line-height: 22px;
}

.suggestion-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.suggestion-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 19px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 19px;
  font-weight: 500;
  box-sizing: border-box;
}

.city-label {
  color: #409eff;
  background: #ecf5ff;
}

.station-label {
  color: #67c23a;
  background: #f0f9eb;
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

.train-number-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.train-number-link {
  color: #1f4e8c;
  font-size: 19px;
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 3px;
  cursor: pointer;
  transition: 0.2s;
}

.train-number-link:hover {
  color: #409eff;
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
  grid-template-columns:
    70px
    1fr
    120px
    120px
    120px;
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
.route-end-row .route-index {
  color: #ffffff !important;
  font-weight: 700;
}

.route-start-row .route-station,
.route-end-row .route-station {
  color: #ffffff !important;
  font-size: 15px;
  font-weight: 700;
}

.route-start-row .route-time,
.route-end-row .route-time {
  color: #ffffff !important;
  font-weight: 600;
}

.route-start-row .route-stop-time,
.route-end-row .route-stop-time {
  color: #ffffff !important;
  font-weight: 600;
}

.route-start-row .route-station-content,
.route-end-row .route-station-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: auto;
  min-height: 32px;
  padding: 0;
  box-sizing: border-box;
  background: transparent;
  color: #ffffff;
  border-radius: 0;
  font-size: 15px;
  font-weight: 700;
  box-shadow: none;
}

.route-passed-row {
  background: #f5f5f5;
  color: #a8abb2;
}

.route-passed-row:hover {
  background: #eeeeee;
}

.route-passed-row .route-index {
  color: #b5b7ba;
}

.route-passed-row .route-station {
  color: #a8abb2;
}

.route-passed-row .route-time {
  color: #b5b7ba;
}

.route-passed-row .route-stop-time {
  color: #b5b7ba;
}

.route-after-row {
  background: #f5f5f5;
  color: #a8abb2;
}

.route-after-row:hover {
  background: #eeeeee;
}

.route-after-row .route-index {
  color: #b5b7ba;
}

.route-after-row .route-station {
  color: #b5b7ba;
}

.route-after-row .route-time {
  color: #b5b7ba;
}

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

.route-middle-row .route-time {
  color: #606266;
}

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
  padding: 0;
  box-sizing: border-box;
}

.route-time {
  color: #606266;
}

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
