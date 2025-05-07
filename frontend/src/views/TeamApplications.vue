<template>
  <div class="team-applications">
    <Navbar />
    
    <div class="container">
      <div class="page-title">团队申请审核</div>
      
      <el-card v-if="!userTeam">
        <el-alert
          title="您不属于任何团队"
          type="warning"
          description="请先加入一个团队，才能查看团队申请。"
          show-icon
          :closable="false">
        </el-alert>
      </el-card>
      
      <el-card v-else>
        <el-table
          v-loading="loading"
          :data="applications"
          style="width: 100%"
          empty-text="暂无待审核的申请">
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="realName" label="姓名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column label="申请时间">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="success"
                @click="approveApplication(scope.row.id)">通过</el-button>
              <el-button
                size="mini"
                type="danger"
                @click="rejectApplication(scope.row.id)">拒绝</el-button>
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
import moment from 'moment'

export default {
  name: 'TeamApplications',
  components: {
    Navbar
  },
  data() {
    return {
      applications: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userTeam'])
  },
  methods: {
    fetchApplications() {
      if (!this.userTeam) return
      
      this.loading = true
      api.getTeamApplications()
        .then(response => {
          this.applications = response.data
        })
        .catch(err => {
          this.$message.error('获取团队申请失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    approveApplication(id) {
      this.$confirm('确定通过该用户的团队申请吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        api.approveApplication(id)
          .then(response => {
            this.$message.success(response.data.message || '已通过申请')
            this.fetchApplications()
          })
          .catch(err => {
            this.$message.error(err.response?.data?.message || '操作失败')
          })
      }).catch(() => {
        // 取消操作
      })
    },
    rejectApplication(id) {
      this.$confirm('确定拒绝该用户的团队申请吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        api.rejectApplication(id)
          .then(response => {
            this.$message.success(response.data.message || '已拒绝申请')
            this.fetchApplications()
          })
          .catch(err => {
            this.$message.error(err.response?.data?.message || '操作失败')
          })
      }).catch(() => {
        // 取消操作
      })
    },
    formatDateTime(dateTime) {
      return moment(dateTime).format('YYYY-MM-DD HH:mm')
    }
  },
  created() {
    this.fetchApplications()
  }
}
</script>

<style scoped>
</style>
