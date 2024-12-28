import { createRouter, createWebHistory } from 'vue-router'
import LoginVue from '@/components/Login.vue'
import RegisterVue from '@/components/Register.vue'
import HomeVue from '@/components/Home.vue'
import HistoryVue from "@/components/History.vue";
import {ElMessage} from "element-plus";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      component: LoginVue,
      name: 'Login'
    },
    {
      path: '/register',
      component: RegisterVue
    },
    {
      path: '/home',
      component: HomeVue,
      name: 'Home'
    },
    {
      path: '/history',
      component: HistoryVue,
      name: 'History'
    }
  ]
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 获取用户的登录信息（比如 token）
  const token = localStorage.getItem("TELLER_SESSION_ID");

  // 如果访问的是登录页面或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    return next();
  }

  // 如果没有 token，则跳转到登录页面
  if (!token) {
    ElMessage.error('请先登录');
    return next('/'); // 重定向到登录页
  }

  // 如果有 token，继续访问目标页面
  next();
});

export default router;