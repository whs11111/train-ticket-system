<template>
  <div class="order-page">
    <div class="top-bar">
      <el-button @click="goBack">←</el-button>

      <h2 class="title">我的订单</h2>
    </div>

    <div
      v-for="order in orders"
      :key="order.orderId"
      class="order-card"
      :class="{ refunded: order.status === '已退票' }"
    >
      <div
        class="order-content"
        :class="{ disabled: order.status === '已退票' }"
        @click="order.status !== '已退票' && detail(order.orderId)"
      >
        <div class="train-info">
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

            <div class="arrow">━━━━━━━━━━━━━━━━━▶</div>

            <div class="travel-date">
              {{ formatDateWeek(order.travelDate) }}
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

        <div class="info-line">
          <div class="left-info">
            <span>
              {{ order.status }}
            </span>

            <span class="tagtype">
              {{ order.ticketType }}
            </span>
          </div>

          <div class="right-info">
            <span class="tag"> 线上支付 </span>

            <span class="tag" v-if="order.status !== '改签票' && order.status !== '已退票'">
              {{ order.payMethod }}
            </span>
          </div>
        </div>

        <div class="seat-line">
          <div class="seat-info">
            {{ order.seatType }}
            0{{ order.coachNo }}车 {{ order.seatNo }}号
          </div>

          <div class="price">￥{{ order.price }}</div>
        </div>
      </div>

      <div class="operation-bar" v-if="order.status !== '已退票'">
        <div
          class="operation-item"
          :class="{ disabled: order.status === '改签票' }"
          @click.stop="changeTicket(order)"
        >
          改签
        </div>

        <div class="divider"></div>

        <div class="operation-item" @click.stop="refundTicket(order)">退票</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'

import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const userStore = useUserStore()

const orders = ref<any[]>([])

onMounted(async () => {
  const res = await request.get('/order/findOrderList', {
    params: {
      userId: userStore.user.userId,
    },
  })

  orders.value = res.data.sort((a: any, b: any) => {
    if (a.status === '已退票' && b.status !== '已退票') {
      return 1
    }

    if (a.status !== '已退票' && b.status === '已退票') {
      return -1
    }

    return 0
  })

  const savedScrollTop = sessionStorage.getItem('myOrderScrollTop')

  if (savedScrollTop !== null) {
    await nextTick()
    window.scrollTo({
      top: Number(savedScrollTop),
      behavior: 'auto',
    })
    sessionStorage.removeItem('myOrderScrollTop')
  }
})

const formatTime = (time: string) => {
  if (!time) {
    return ''
  }

  return time.substring(0, 5)
}

const rememberScrollPosition = () => {
  sessionStorage.setItem('myOrderScrollTop', String(window.scrollY))
}

const detail = (orderId: number) => {
  rememberScrollPosition()

  router.push({
    path: '/orderDetail',
    query: {
      orderId,
      from: 'order',
    },
  })
}

const goBack = () => {
  router.push('/home')
}

const formatDateWeek = (date: string) => {
  if (!date) {
    return ''
  }

  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

  const d = new Date(date)

  return `${date}（${week[d.getDay()]}）`
}

const refundTicket = async (order: any) => {
  rememberScrollPosition()

  router.push({
    path: '/refund',
    query: {
      orderId: order.orderId,
    },
  })
}

const changeTicket = (order: any) => {
  if (order.status === '改签票') {
    ElMessage.warning('改签票不能再次改签')

    return
  }

  rememberScrollPosition()

  router.push({
    path: '/changeTicket',
    query: {
      oldOrderId: order.orderId,

      startStation: order.startStation,

      endStation: order.endStation,

      travelDate: order.travelDate,

      oldPrice: order.price,
    },
  })
}
</script>

<style scoped>
.order-page {
  width: 650px;
  margin: 40px auto;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.order-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: linear-gradient(180deg, #f3f8ff 0%, #ffffff 100%);
  z-index: -1;
}
.top-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.top-bar .el-button {
  position: absolute;
  left: 0;
}

.title {
  text-align: center;
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  letter-spacing: 1px;
  margin: 0;
  position: relative;
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
.order-card {
  border: 1px solid #eaeaea;
  border-radius: 12px;
  margin-bottom: 25px;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.order-content {
  padding: 25px;
  cursor: pointer;
}

.order-content:hover {
  background: #fafafa;
}

.refunded .order-content {
  cursor: default;
}

.refunded .order-content:hover {
  background: #fff;
}

.train-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.station {
  width: 160px;
  text-align: left;
}

.station:last-child {
  text-align: right;
}

.time {
  font-size: 32px;
  font-weight: bold;
}

.station-name {
  margin-top: 3px;
  font-size: 18px;
}

.middle {
  width: 180px;
  margin-top: -28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.train-no {
  font-size: 22px;
  margin-bottom: -12px;
  font-weight: bold;
}

.arrow {
  font-size: 15px;
  font-weight: bold;
}

.travel-date {
  color: black;
  margin-top: -7px;
  font-size: 15px;
}

.info-line {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-group {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 3px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}

.tagtype {
  padding: 3px 10px;
  border: 1px solid #2692ff;
  border-radius: 4px;
  background: #fff;
  color: #2692ff;
  font-size: 12px;
  line-height: 18px;
}

.seat-line {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seat-info {
  font-size: 16px;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color: #ff742e;
}

.operation-bar {
  height: 52px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: stretch;
}

.operation-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #0783ff;
  font-size: 15px;
  user-select: none;
}

.operation-item:hover {
  background: #fafafa;
}

.operation-bar span {
  flex: 1;
  text-align: center;
  cursor: pointer;
  color: #0783ff;
  font-size: 15px;
  user-select: none;
}

.operation-bar span:hover {
  background: #fafafa;
}

.divider {
  width: 1px;
  height: 52px;
  background: #eee;
}

.left-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.right-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.disabled {
  color: #92c6fb;
  cursor: not-allowed;
}

.disabled:hover {
  background: white;
}

.operation-item.disabled {
  color: #92c6fb;
  cursor: not-allowed;
}

.operation-item.disabled:hover {
  background: white;
}

.refunded {
  background: #fff;
  border-color: #ececec;
}

.refunded .time {
  color: #a7a7a7 !important;
}
.refunded .station-name,
.refunded .train-no,
.refunded .seat-info {
  color: #ababab !important;
}

.refunded .travel-date,
.refunded .arrow {
  color: #a7a7a7 !important;
}

.refunded .left-info span,
.refunded .right-info span {
  color: #9f9f9f !important;
}

.refunded .price {
  color: #9f9f9f !important;
}

.refunded .tag,
.refunded .tagtype {
  background: #fff !important;
  color: #a5a5a5 !important;
  border: 1px solid #dddddd !important;
}

.refunded .order-content {
  cursor: default;
  pointer-events: none;
}

.refunded .order-content:hover {
  background: #fff;
}
</style>
