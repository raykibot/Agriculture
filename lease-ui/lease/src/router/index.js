import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'     // 引入新建的组件
import ProfileView from '../views/ProfileView.vue'
import MachineryListView from '../views/MachineryListView.vue'
import MessagesView from '../views/MessagesView.vue'
import PublishView from '../views/PublishView.vue'
import ConfirmOrderView from '../views/ConfirmOrderView.vue'
import MachineryDetailView from '../views/MachineryDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    // 添加登录页路由
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    // 添加个人信息页路由
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView
    },
    {
      path: '/machinery-list',
      name: 'machinery-list',
      component: MachineryListView
    },
    {
      path: '/messages',
      name: 'messages',
      component: MessagesView
    },
    {
      path: '/publish',
      name: 'publish',
      component: PublishView,
      meta: { requiresAuth: true } 
    },
    {
      path: '/machinery/:id', // 注意这个 :id 是动态参数
      name: 'machineryDetail',
      component: MachineryDetailView
    },
    {
      path: '/confirm-order',
      name: 'confirmOrder',
      component: () => import('../views/ConfirmOrderView.vue')
    },
  ],
})

export default router
