import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user-home',
    name: 'UserHome',
    component: () => import('../views/UserHome.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  
  if (to.meta.requiresAuth && !userInfo) {
    next('/login')
  } else if (to.path === '/login' && userInfo) {
    try {
      const parsedUserInfo = JSON.parse(userInfo)
      if (parsedUserInfo.roleType === 1) {
        next('/home')
      } else {
        next('/user-home')
      }
    } catch (error) {
      next('/home')
    }
  } else {
    next()
  }
})

export default router
