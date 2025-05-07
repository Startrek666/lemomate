<template>
  <div class="admin-users">
    <Navbar />

    <div class="container">
      <div class="page-header">
        <div class="page-title">用户管理</div>
        <div class="search-container">
          <el-input
            placeholder="搜索用户名、姓名或邮箱"
            v-model="searchQuery"
            clearable
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </div>
      </div>

      <el-card>
        <el-table
          v-loading="loading"
          :data="filteredUsers"
          style="width: 100%">
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="realName" label="姓名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column label="团队">
            <template slot-scope="scope">
              {{ scope.row.teamName || '无' }}
            </template>
          </el-table-column>
          <el-table-column label="角色" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.role" placeholder="选择角色" @change="updateUserRole(scope.row)">
                <el-option label="普通用户" value="USER"></el-option>
                <el-option label="团队管理员" value="TEAM_ADMIN"></el-option>
                <el-option label="平台管理员" value="PLATFORM_ADMIN"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="修改团队" width="250">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.teamId"
                placeholder="选择团队"
                clearable
                @change="updateUserTeam(scope.row)">
                <el-option
                  v-for="team in teams"
                  :key="team.id"
                  :label="team.teamName"
                  :value="team.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import api from '@/services/api'

export default {
  name: 'AdminUsers',
  components: {
    Navbar
  },
  data() {
    return {
      users: [],
      teams: [],
      loading: false,
      searchQuery: ''
    }
  },
  computed: {
    filteredUsers() {
      if (!this.searchQuery) {
        return this.users
      }

      const query = this.searchQuery.toLowerCase()
      return this.users.filter(user => {
        return user.username.toLowerCase().includes(query) ||
               user.realName.toLowerCase().includes(query) ||
               user.email.toLowerCase().includes(query) ||
               (user.teamName && user.teamName.toLowerCase().includes(query))
      })
    }
  },
  methods: {
    fetchUsers() {
      this.loading = true
      api.getAllUsers()
        .then(response => {
          this.users = response.data
        })
        .catch(err => {
          this.$message.error('获取用户列表失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    fetchTeams() {
      api.getAllTeams()
        .then(response => {
          this.teams = response.data
        })
        .catch(err => {
          console.error('获取团队列表失败', err)
        })
    },
    updateUserRole(user) {
      const userDto = { role: user.role }
      api.updateUserRole(user.id, userDto)
        .then(() => {
          this.$message.success('用户角色已更新')
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '更新用户角色失败')
          this.fetchUsers() // 刷新数据
        })
    },
    updateUserTeam(user) {
      const userDto = { teamId: user.teamId }
      api.updateUserTeam(user.id, userDto)
        .then(() => {
          this.$message.success('用户团队已更新')
          // 更新用户的团队名称显示
          if (user.teamId) {
            const team = this.teams.find(t => t.id === user.teamId)
            if (team) {
              user.teamName = team.teamName
            }
          } else {
            user.teamName = null
          }
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '更新用户团队失败')
          this.fetchUsers() // 刷新数据
        })
    },
    handleSearch() {
      // 搜索逻辑已在computed属性中实现
      // 这个方法主要用于响应输入事件
    }
  },
  created() {
    this.fetchUsers()
    this.fetchTeams()
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-container {
  width: 300px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-container {
    width: 100%;
    margin-top: 10px;
  }
}
</style>
