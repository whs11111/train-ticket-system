<template>
  <div class="detail" v-if="isTransfer ? firstOrder.orderId && secondOrder.orderId : order.orderId">
    <div class="top-bar">
      <el-button class="back-btn" @click="goBack"> ← </el-button>

      <h2 class="title">
        订单详情
        <span v-if="!isTransfer && order.status === '改签票'">（改签）</span>
      </h2>
    </div>

    <template v-if="isTransfer">
      <div class="ticket-section">
        <div class="section-title">第一程</div>

        <div class="ticket-info">
          <div class="train-row">
            <div class="station">
              <div class="time">
                {{ formatTime(firstOrder.departureTime) }}
              </div>

              <div class="station-name">
                {{ firstOrder.startStation }}
              </div>
            </div>

            <div class="middle">
              <div class="train-no">
                {{ firstOrder.trainNo }}
              </div>

              <div class="arrow">━━━━━━━━━━▶</div>

              <div class="duration">
                {{ firstDuration }}
              </div>
            </div>

            <div class="station">
              <div class="time">
                {{ formatTime(firstOrder.arrivalTime) }}
              </div>

              <div class="station-name">
                {{ firstOrder.endStation }}
              </div>
            </div>
          </div>
        </div>

        <div class="order-info">
          <div class="row">
            <span>乘车日期</span>
            <span>{{ firstOrder.travelDate }}</span>
          </div>

          <div class="row">
            <span>乘车人</span>
            <span>{{ firstOrder.name }}</span>
          </div>

          <div class="row">
            <span>座位</span>
            <span>
              {{ firstOrder.seatType }}
              0{{ firstOrder.coachNo }}车 {{ firstOrder.seatNo }}号
            </span>
          </div>

          <div class="row">
            <span>票价</span>
            <span class="price"> ￥{{ Math.round(Number(firstOrder.price || 0)) }} </span>
          </div>

          <div class="row">
            <span>支付方式</span>
            <span>{{ firstOrder.payMethod }}</span>
          </div>

          <div class="row">
            <span>支付时间</span>
            <span>{{ formatDateTime(firstOrder.payTime) }}</span>
          </div>

          <div class="row">
            <span>状态</span>
            <span class="status">{{ firstOrder.status }}</span>
          </div>
        </div>
      </div>

      <div class="ticket-section second-section">
        <div class="section-title">第二程</div>

        <div class="ticket-info">
          <div class="train-row">
            <div class="station">
              <div class="time">
                {{ formatTime(secondOrder.departureTime) }}
              </div>

              <div class="station-name">
                {{ secondOrder.startStation }}
              </div>
            </div>

            <div class="middle">
              <div class="train-no">
                {{ secondOrder.trainNo }}
              </div>

              <div class="arrow">━━━━━━━━━━▶</div>

              <div class="duration">
                {{ secondDuration }}
              </div>
            </div>

            <div class="station">
              <div class="time">
                {{ formatTime(secondOrder.arrivalTime) }}
              </div>

              <div class="station-name">
                {{ secondOrder.endStation }}
              </div>
            </div>
          </div>
        </div>

        <div class="order-info">
          <div class="row">
            <span>乘车日期</span>
            <span>{{ secondOrder.travelDate }}</span>
          </div>

          <div class="row">
            <span>乘车人</span>
            <span>{{ secondOrder.name }}</span>
          </div>

          <div class="row">
            <span>座位</span>
            <span>
              {{ secondOrder.seatType }}
              0{{ secondOrder.coachNo }}车 {{ secondOrder.seatNo }}号
            </span>
          </div>

          <div class="row">
            <span>票价</span>
            <span class="price"> ￥{{ Math.round(Number(secondOrder.price || 0)) }} </span>
          </div>

          <div class="row">
            <span>支付方式</span>
            <span>{{ secondOrder.payMethod }}</span>
          </div>

          <div class="row">
            <span>支付时间</span>
            <span>{{ formatDateTime(secondOrder.payTime) }}</span>
          </div>

          <div class="row">
            <span>状态</span>
            <span class="status">{{ secondOrder.status }}</span>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="ticket-info">
        <div class="train-row">
          <div class="station">
            <div class="time">
              {{ formatTime(order.departureTime) }}
            </div>

            <div class="station-name">
              {{ order.startStation }}
            </div>
          </div>

          <div class="middle">
            <div class="train-no">
              {{ order.trainNo }}
            </div>

            <div class="arrow">━━━━━━━━━━▶</div>

            <div class="duration">
              {{ duration }}
            </div>
          </div>

          <div class="station">
            <div class="time">
              {{ formatTime(order.arrivalTime) }}
            </div>

            <div class="station-name">
              {{ order.endStation }}
            </div>
          </div>
        </div>
      </div>

      <div class="order-info">
        <div class="row">
          <span>乘车日期</span>
          <span>{{ order.travelDate }}</span>
        </div>

        <div class="row">
          <span>乘车人</span>
          <span>{{ order.name }}</span>
        </div>

        <div class="row">
          <span>座位</span>
          <span>
            {{ order.seatType }}
            0{{ order.coachNo }}车 {{ order.seatNo }}号
          </span>
        </div>

        <div class="row">
          <span>票价</span>
          <span class="price"> ￥{{ Math.round(Number(order.price || 0)) }} </span>
        </div>

        <div class="row" v-if="order.status !== '改签票'">
          <span>支付方式</span>
          <span>{{ order.payMethod }}</span>
        </div>

        <div class="row" v-if="order.status !== '改签票'">
          <span>支付时间</span>
          <span>{{ formatDateTime(order.payTime) }}</span>
        </div>

        <div class="row">
          <span>状态</span>
          <span class="status">{{ order.status }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/request'

const route = useRoute()
const router = useRouter()

const outTradeNo = route.query.out_trade_no as string

const transferTradeParts = outTradeNo?.startsWith('TRANSFER_') ? outTradeNo.split('_') : []

let orderId = Number(route.query.orderId || 0)

if (!orderId && outTradeNo) {
  if (outTradeNo.startsWith('CHANGE_')) {
    const parts = outTradeNo.split('_')
    orderId = Number(parts[1])
  } else {
    orderId = Number(outTradeNo)
  }
}

const firstOrderId = Number(route.query.firstOrderId || transferTradeParts[1] || 0)
const secondOrderId = Number(route.query.secondOrderId || transferTradeParts[2] || 0)

const isTransfer = route.query.transfer === 'true' || transferTradeParts.length === 3

const order = ref<any>({})
const firstOrder = ref<any>({})
const secondOrder = ref<any>({})

const from = route.query.from || (route.query.out_trade_no ? 'payment' : '')

onMounted(async () => {
  try {
    if (isTransfer) {
      const firstRes = await request.get(`/order/detail/${firstOrderId}`)
      const secondRes = await request.get(`/order/detail/${secondOrderId}`)

      firstOrder.value = firstRes.data
      secondOrder.value = secondRes.data
    } else {
      const res = await request.get(`/order/detail/${orderId}`)
      order.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
})

const formatTime = (time: string) => {
  if (!time) return ''

  return time.substring(0, 5)
}

const formatDateTime = (time: string) => {
  if (!time) return ''

  return time.replace('T', ' ').substring(0, 19)
}

const calculateDuration = (departure: string, arrival: string) => {
  if (!departure || !arrival) return ''

  const start = departure.split(':')
  const end = arrival.split(':')

  const s = Number(start[0]) * 60 + Number(start[1])
  const e = Number(end[0]) * 60 + Number(end[1])

  let diff = e - s

  if (diff < 0) {
    diff += 24 * 60
  }

  return `${Math.floor(diff / 60)}小时${diff % 60}分`
}

const duration = computed(() => {
  return calculateDuration(order.value.departureTime, order.value.arrivalTime)
})

const firstDuration = computed(() => {
  return calculateDuration(firstOrder.value.departureTime, firstOrder.value.arrivalTime)
})

const secondDuration = computed(() => {
  return calculateDuration(secondOrder.value.departureTime, secondOrder.value.arrivalTime)
})

const transferTotalPrice = computed(() => {
  const firstPrice = Number(firstOrder.value.price || 0)
  const secondPrice = Number(secondOrder.value.price || 0)

  return Math.round(firstPrice + secondPrice)
})

const goBack = () => {
  if (from === 'payment') {
    router.push('/home')
  } else {
    router.push('/myOrder')
  }
}
</script>

<style scoped>
.detail {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  padding: 40px 0;
  box-sizing: border-box;
}

.top-bar {
  width: 900px;
  margin: 0 auto 35px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.back-btn {
  position: absolute;
  left: 0;
  font-size: 22px;
  width: 45px;
  height: 45px;
}

.title {
  margin: 0;
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

.ticket-section {
  width: 900px;
  margin: 0 auto;
}

.second-section {
  margin-top: 30px;
}

.section-title {
  margin-bottom: 12px;
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #1f4e8c;
}

.ticket-info {
  width: 900px;
  margin: 0 auto;
  border: 1px solid #dcecff;
  border-radius: 8px;
  padding: 30px;
  background: #fbfdff;
  box-sizing: border-box;
}

.train-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.station {
  width: 180px;
  text-align: center;
}

.time {
  font-size: 43px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.station-name {
  margin-top: 8px;
  font-size: 20px;
  font-weight: bold;
}

.middle {
  width: 230px;
  margin-top: -30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.train-no {
  font-size: 18px;
  margin-bottom: -16px;
  font-weight: bold;
  color: #303133;
}

.arrow {
  font-size: 20px;
  margin: 5px 0;
  color: black;
}

.duration {
  color: #707070;
  margin-top: -14px;
  font-size: 13px;
}

.order-info {
  width: 900px;
  margin: 20px auto 0;
  border: 1px solid #dcecff;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  box-sizing: border-box;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 18px 25px;
  border-bottom: 1px solid #eee;
}

.row:last-child {
  border-bottom: none;
}

.price {
  color: #ff742e;
  font-weight: bold;
}

.status {
  color: #67c23a;
}
</style>
