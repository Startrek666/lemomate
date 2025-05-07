<template>
  <div class="home">
    <Navbar />

    <div class="container">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="welcome-card">
            <div slot="header" class="welcome-header">
              <div class="logo-container">
                <img src="/images/logo.jpg" alt="Lemomate Logo" class="logo-image">
              </div>
              <h2>欢迎使用 Lemomate Meet</h2>
            </div>
            <div class="welcome-content">
              <p>Lemomate Meet是一个简单易用的会议系统，帮助您轻松创建和管理在线会议。</p>

              <div v-if="!userTeam" class="no-team-warning">
                <el-alert
                  title="您当前不属于任何团队"
                  type="warning"
                  description="请选择一个团队加入，或等待管理员将您添加到团队中。"
                  show-icon
                  :closable="false">
                </el-alert>
                <div class="team-selection" v-if="teams.length > 0">
                  <h3>可加入的团队：</h3>
                  <el-table :data="teams" style="width: 100%">
                    <el-table-column prop="teamName" label="团队名称"></el-table-column>
                    <el-table-column prop="memberCount" label="成员数量"></el-table-column>
                    <el-table-column label="操作" width="120">
                      <template slot-scope="scope">
                        <el-button
                          size="mini"
                          type="primary"
                          @click="applyToTeam(scope.row.id)">申请加入</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>

              <div v-else class="team-info">
                <p>您当前所在的团队：<strong>{{ userTeam.name }}</strong></p>

                <div class="quick-actions">
                  <h3>快速操作：</h3>
                  <el-button-group>
                    <el-button type="primary" style="margin-right: 10px" @click="$router.push('/meetings')">查看会议</el-button>
                    <el-button type="primary" style="margin-right: 10px" @click="$router.push('/team/members')">查看团队成员</el-button>
                    <el-button v-if="isTeamAdmin || isPlatformAdmin" type="success" @click="$router.push('/meetings/create')">创建会议</el-button>
                  </el-button-group>
                </div>
              </div>

              <div v-if="recentMeetings.length > 0" class="recent-meetings">
                <h3>近期会议：</h3>
                <el-table :data="recentMeetings" style="width: 100%">
                  <el-table-column prop="title" label="会议标题"></el-table-column>
                  <el-table-column label="开始时间">
                    <template slot-scope="scope">
                      {{ formatDateTime(scope.row.startTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="状态">
                    <template slot-scope="scope">
                      <el-tag :type="getMeetingStatusType(scope.row.status)">
                        {{ getMeetingStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template slot-scope="scope">
                      <el-button
                        size="mini"
                        type="primary"
                        @click="joinMeeting(scope.row.id)">加入会议</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import api from '@/services/api'
import { mapGetters } from 'vuex'
import moment from 'moment'

export default {
  name: 'Home',
  components: {
    Navbar
  },
  data() {
    return {
      recentMeetings: [],
      teams: []
    }
  },
  computed: {
    ...mapGetters(['user', 'userTeam']),
    isTeamAdmin() {
      return this.user.role === 'TEAM_ADMIN'
    },
    isPlatformAdmin() {
      return this.user.role === 'PLATFORM_ADMIN'
    }
  },
  methods: {
    fetchRecentMeetings() {
      if (this.userTeam) {
        api.getTeamMeetings()
          .then(response => {
            this.recentMeetings = response.data.slice(0, 5) // 只显示最近5个会议
          })
          .catch(err => {
            console.error('获取会议列表失败', err)
          })
      }
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
    applyToTeam(teamId) {
      api.applyToTeam(teamId)
        .then(response => {
          this.$message.success(response.data.message)
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '申请失败')
        })
    },
    joinMeeting(meetingId) {
      // 直接导航到会议加入页面
      this.$router.push(`/meetings/join/${meetingId}`)
    },
    formatDateTime(dateTime) {
      return moment(dateTime).format('YYYY-MM-DD HH:mm')
    },
    getMeetingStatusText(status) {
      switch (status) {
        case 'SCHEDULED': return '未开始'
        case 'ONGOING': return '进行中'
        case 'ENDED': return '已结束'
        default: return '未知'
      }
    },
    getMeetingStatusType(status) {
      switch (status) {
        case 'SCHEDULED': return 'info'
        case 'ONGOING': return 'success'
        case 'ENDED': return 'danger'
        default: return 'info'
      }
    }
  },
  created() {
    this.fetchTeams()
    this.fetchRecentMeetings()
  }
}
</script>

<style scoped>
.welcome-card {
  margin-bottom: 20px;
}

.welcome-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.logo-container {
  margin-bottom: 15px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.logo-image {
  max-width: 350px;
  width: 100%;
  height: auto;
  object-fit: contain;
}

.welcome-content {
  line-height: 1.6;
}

.no-team-warning {
  margin: 20px 0;
}

.team-selection {
  margin-top: 20px;
}

.team-info {
  margin: 20px 0;
}

.quick-actions {
  margin: 20px 0;
}

.recent-meetings {
  margin-top: 30px;
}

@media (max-width: 768px) {
  .logo-image {
    max-width: 280px;
  }
}
</style>
