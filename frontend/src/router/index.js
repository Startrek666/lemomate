import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MeetingCreate from '../views/MeetingCreate.vue'
import MeetingList from '../views/MeetingList.vue'
import MeetingDetail from '../views/MeetingDetail.vue'
import MeetingJoin from '../views/MeetingJoin.vue'
import TeamMembers from '../views/TeamMembers.vue'
import TeamApplications from '../views/TeamApplications.vue'
import Profile from '../views/Profile.vue'
import AdminUsers from '../views/admin/Users.vue'
import AdminTeams from '../views/admin/Teams.vue'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guest: true }
  },
  {
    path: '/meetings/create',
    name: 'MeetingCreate',
    component: MeetingCreate,
    meta: { requiresAuth: true, requiresTeamAdmin: true }
  },
  {
    path: '/meetings',
    name: 'MeetingList',
    component: MeetingList,
    meta: { requiresAuth: true }
  },
  {
    path: '/meetings/:id',
    name: 'MeetingDetail',
    component: MeetingDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/meetings/join/:id',
    name: 'MeetingJoin',
    component: MeetingJoin
  },
  {
    path: '/team/members',
    name: 'TeamMembers',
    component: TeamMembers,
    meta: { requiresAuth: true }
  },
  {
    path: '/team/applications',
    name: 'TeamApplications',
    component: TeamApplications,
    meta: { requiresAuth: true, requiresTeamAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers,
    meta: { requiresAuth: true, requiresPlatformAdmin: true }
  },
  {
    path: '/admin/teams',
    name: 'AdminTeams',
    component: AdminTeams,
    meta: { requiresAuth: true, requiresPlatformAdmin: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = store.getters.isLoggedIn
  const userRole = store.getters.userRole

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      next({ name: 'Login' })
    } else {
      if (to.matched.some(record => record.meta.requiresTeamAdmin)) {
        if (userRole === 'TEAM_ADMIN' || userRole === 'PLATFORM_ADMIN') {
          next()
        } else {
          next({ name: 'Home' })
        }
      } else if (to.matched.some(record => record.meta.requiresPlatformAdmin)) {
        if (userRole === 'PLATFORM_ADMIN') {
          next()
        } else {
          next({ name: 'Home' })
        }
      } else {
        next()
      }
    }
  } else if (to.matched.some(record => record.meta.guest)) {
    if (isLoggedIn) {
      next({ name: 'Home' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
