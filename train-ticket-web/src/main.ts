import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

const pinia = createPinia()

pinia.use(piniaPluginPersistedstate)

app.use(pinia)

app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')
