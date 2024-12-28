import './assets/main.css'
import axios from 'axios'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import * as echarts from 'echarts'; // 导入 ECharts

const app = createApp(App)
// 将 echarts 注册为全局属性
app.config.globalProperties.$echarts = echarts;
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
axios.defaults.baseURL = 'http://localhost:8000';
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')