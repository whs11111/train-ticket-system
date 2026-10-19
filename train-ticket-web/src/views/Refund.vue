<template>
  <div class="payment">
    <div class="payment-card">
      <div class="header-box">
        <el-button class="back-btn" @click="goBack"> ← </el-button>

        <div class="title">退票</div>
      </div>

      <div class="ticket-info">
        <div class="train-row">
          <div class="station start">
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

            <div class="arrow">━━━━━━━━━━━▶</div>

            <div class="duration">
              {{ duration }}
            </div>
          </div>

          <div class="station end">
            <div class="time">
              {{ formatTime(order.arrivalTime) }}
            </div>

            <div class="station-name">
              {{ order.endStation }}
            </div>
          </div>
        </div>

        <div class="date">
          <span class="label">乘车日期：</span>
          <span>{{ order.travelDate }}</span>
        </div>
      </div>

      <div class="order-info">
        <div class="row">
          <span>乘车人</span>

          <span>
            {{ order.name }}
          </span>
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

          <span class="price"> ￥{{ order.price }} </span>
        </div>

        <div class="row" v-if="order.status !== '改签票'">
          <span>支付方式</span>

          <span>
            {{ order.payMethod }}
          </span>
        </div>

        <div class="row" v-if="order.status !== '改签票'">
          <span>支付时间</span>

          <span>
            {{ formatDateTime(order.payTime) }}
          </span>
        </div>
      </div>

      <div class="total-price">
        <span>退款金额：</span>

        <span class="money"> ￥{{ order.price }} </span>
      </div>

      <div class="pay-btn">
        <el-button class="cancel-button" size="large" @click="goBack"> 取消退款 </el-button>

        <el-button class="pay-button" size="large" @click="refund"> 确认退款 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()

const router = useRouter()

const orderId = Number(route.query.orderId)

const order = ref<any>({})

onMounted(async () => {
  const res = await request.get(`/order/detail/${orderId}`)

  order.value = res.data
})

const formatTime = (time: string) => {
  if (!time) {
    return ''
  }

  return time.substring(0, 5)
}

const formatDateTime = (time: string) => {
  if (!time) {
    return ''
  }

  return time.replace('T', ' ').substring(0, 19)
}

const duration = computed(() => {
  if (!order.value.departureTime || !order.value.arrivalTime) {
    return ''
  }

  const start = order.value.departureTime.split(':')

  const end = order.value.arrivalTime.split(':')

  const s = Number(start[0]) * 60 + Number(start[1])

  const e = Number(end[0]) * 60 + Number(end[1])

  const diff = e - s

  return `${Math.floor(diff / 60)}小时${diff % 60}分`
})

const refund = async () => {
  try {
    await request.post(`payment/refund/${orderId}`)

    ElMessage.success('退票成功')

    router.push('/myOrder')
  } catch (e) {
    ElMessage.error('退票失败')
  }
}

const goBack = () => {
  ElMessage.success('已取消退款')
  router.back()
}
</script>

<style scoped>
.payment {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  padding: 40px 0;
  box-sizing: border-box;
}

.payment-card {
  width: 900px;
  margin: auto;
  background: #fff;
  border-radius: 8px;
  padding: 25px 30px 40px;
  box-sizing: border-box;
}

.header-box {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 35px;
}

.back-btn {
  position: absolute;
  left: 0;
  font-size: 22px;
  width: 45px;
  height: 45px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  text-align: center;
  letter-spacing: 2px;
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

.ticket-info {
  border: 1px solid #dcecff;
  border-radius: 8px;
  padding: 25px 30px;
  background: #fbfdff;
}

.train-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.station {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 37px;
}

.start,
.end {
  text-align: center;
}

.middle {
  width: 190px;
  margin: 0 55px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time {
  font-size: 47px;
  font-weight: bold;
  line-height: 1;
  color: #303133;
}

.station-name {
  margin-top: 8px;
  font-size: 22px;
  font-weight: bold;
}

.train-no {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: -11px;
  color: black;
}

.arrow {
  width: 270px;
  text-align: center;
  color: black;
  font-weight: bold;
  font-size: 20px;
  line-height: 20px;
  margin: 2px 0;
}

.duration {
  margin-top: -5px;
  color: #7d7e80;
  font-size: 15px;
}

.date {
  margin-top: 10px;
  text-align: center;
  color: #585858;
  font-size: 17px;
}

.label {
  color: #585858;
}

.order-info {
  margin-top: 30px;
  border: 1px solid #dcecff;
  border-radius: 8px;
  overflow: hidden;
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
  color: #ff5500;
  font-size: 22px;
  font-weight: bold;
}

.total-price {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 18px;
}

.money {
  margin-left: 10px;
  font-size: 25px;
  font-weight: bold;
  color: #ff5500;
}

.pay-btn {
  margin-top: 40px;
  text-align: center;
}

.pay-btn .el-button {
  margin: 0 15px;
}

.pay-button {
  width: 220px;
  height: 46px;
  font-size: 18px;
  background: #ff742e;
  border-color: #ff742e;
  color: #fff;
}

.pay-button:hover,
.pay-button:focus {
  background: #ffad33;
  border-color: #ffad33;
  color: #fff;
}

.pay-button:active {
  background: #e68900;
  border-color: #e68900;
  color: #fff;
}

.cancel-button {
  width: 220px;
  height: 46px;
  font-size: 18px;
  background: #909399;
  border-color: #909399;
  color: #fff;
}

.cancel-button:hover,
.cancel-button:focus {
  background: #b1b3b8;
  border-color: #b1b3b8;
  color: #fff;
}

.cancel-button:active {
  background: #73767a;
  border-color: #73767a;
  color: #fff;
}
</style>
