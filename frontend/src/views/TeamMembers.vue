<template>
  <div class="team-members">
    <Navbar />
    
    <div class="container">
      <div class="page-title">团队成员</div>
      
      <el-card v-if="!userTeam">
        <el-alert
          title="您不属于任何团队"
          type="warning"
          description="请先加入一个团队，才能查看团队成员。"
          show-icon
          :closable="false">
        </el-alert>
      </el-card>
      
      <el-card v-else>
        <div class="team-info">
          <h3>{{ userTeam.name }}</h3>
          <p>成员数量：{{ members.length }}</p>
        </div>
        
        <el-table
          v-loading="loading"
          :data="members"
          style="width: 100%"
          empty-text="暂无团队成员">
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="realName" label="姓名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column label="角色" width="120">
            <template slot-scope="scope">
              <el-tag :type="getRoleTagType(scope.row.role)">
                {{ getRoleText(scope.row.role) }}
              </el-tag>
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
import { mapGetters } from 'vuex'

export default {
  name: 'TeamMembers',
  components: {
    Navbar
  },
  data() {
    return {
      members: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userTeam'])
  },
  methods: {
    fetchTeamMembers() {
      if (!this.userTeam) return
      
      this.loading = true
      api.getTeamMembers()
        .then(response => {
          this.members = response.data
        })
        .catch(err => {
          this.$message.error('获取团队成员失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    getRoleText(role) {
      switch (role) {
        case 'USER': return '普通用户'
        case 'TEAM_ADMIN': return '团队管理员'
        case 'PLATFORM_ADMIN': return '平台管理员'
        default: return '未知'
      }
    },
    getRoleTagType(role) {
      switch (role) {
        case 'USER': return ''
        case 'TEAM_ADMIN': return 'success'
        case 'PLATFORM_ADMIN': return 'danger'
        default: return 'info'
      }
    }
  },
  created() {
    this.fetchTeamMembers()
  }
}
</script>

<style scoped>
.team-info {
  margin-bottom: 20px;
}
</style>
