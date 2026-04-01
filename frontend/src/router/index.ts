/**
 * 路由配置
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue')
      },
      {
        path: 'notes',
        name: 'Notes',
        component: () => import('@/views/notes/NotesView.vue')
      },
      {
        path: 'notes/:id',
        name: 'NoteEdit',
        component: () => import('@/views/notes/NoteEditView.vue')
      },
      {
        path: 'ai/chat',
        name: 'AIChat',
        component: () => import('@/views/ai/AIChatView.vue')
      },
      {
        path: 'spaces',
        name: 'Spaces',
        component: () => import('@/views/space/SpaceListView.vue')
      },
      {
        path: 'spaces/:id',
        name: 'SpaceDetail',
        component: () => import('@/views/space/SpaceDetailView.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/SettingsView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();

  // 需要认证的路由
  if (to.meta.requiresAuth) {
    if (authStore.isLoggedIn) {
      next();
    } else {
      next('/login');
    }
  }
  // 游客路由（登录、注册）
  else if (to.meta.guest) {
    if (authStore.isLoggedIn) {
      next('/');
    } else {
      next();
    }
  }
  // 其他路由
  else {
    next();
  }
});

export default router;
