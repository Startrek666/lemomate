<template>
  <el-menu
    :default-active="activeIndex"
    class="navbar"
    mode="horizontal"
    background-color="#545c64"
    text-color="#fff"
    active-text-color="#ffd04b"
    router
  >
    <el-menu-item index="/">Lemomate</el-menu-item>

    <template v-if="isLoggedIn">
      <el-menu-item index="/meetings">会议列表</el-menu-item>
      <el-menu-item index="/team/members">团队成员</el-menu-item>

      <el-submenu index="user-menu" style="float: right;">
        <template slot="title">
          <div class="user-avatar-menu">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" class="avatar-small" alt="头像">
            <span v-else class="avatar-placeholder-small">{{ user.realName ? user.realName.charAt(0).toUpperCase() : '?' }}</span>
            <span class="username">{{ user.realName }}</span>
          </div>
        </template>
        <el-menu-item index="/profile">个人资料</el-menu-item>
        <el-menu-item v-if="isTeamAdmin || isPlatformAdmin" index="/meetings/create">创建会议</el-menu-item>
        <el-menu-item v-if="isTeamAdmin || isPlatformAdmin" index="/team/applications">团队申请</el-menu-item>
        <el-menu-item v-if="isPlatformAdmin" index="/admin/users">用户管理</el-menu-item>
        <el-menu-item v-if="isPlatformAdmin" index="/admin/teams">团队管理</el-menu-item>
        <el-menu-item @click="logout">退出登录</el-menu-item>
      </el-submenu>
    </template>

    <template v-else>
      <el-menu-item index="/login" style="float: right;">登录</el-menu-item>
      <el-menu-item index="/register" style="float: right;">注册</el-menu-item>
    </template>
  </el-menu>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Navbar',
  data() {
    return {
      activeIndex: '/'
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'user']),
    isTeamAdmin() {
      return this.user.role === 'TEAM_ADMIN'
    },
    isPlatformAdmin() {
      return this.user.role === 'PLATFORM_ADMIN'
    }
  },
  methods: {
    logout() {
      this.$store.dispatch('logout')
        .then(() => {
          this.$router.push('/login')
        })
    }
  },
  created() {
    this.activeIndex = this.$route.path
  }
}
</script>

<style scoped>
.navbar {
  margin-bottom: 20px;
}

.user-avatar-menu {
  display: flex;
  align-items: center;
}

.avatar-small {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
}

.avatar-placeholder-small {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  margin-right: 8px;
}

.username {
  margin-left: 5px;
}
</style>
