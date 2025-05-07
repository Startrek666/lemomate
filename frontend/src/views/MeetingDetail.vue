<template>
  <div class="meeting-detail">
    <Navbar />

    <div class="container">
      <div class="page-title">会议详情</div>

      <el-card v-loading="loading">
        <div v-if="meeting">
          <div class="meeting-header">
            <h2>{{ meeting.title }}</h2>
            <el-tag :type="getMeetingStatusType(meeting.status)">
              {{ getMeetingStatusText(meeting.status) }}
            </el-tag>
          </div>

          <div class="meeting-info">
            <p v-if="meeting.description">{{ meeting.description }}</p>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="创建者">{{ meeting.creatorName }}</el-descriptions-item>
              <el-descriptions-item label="团队">{{ meeting.teamName }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ formatDateTime(meeting.startTime) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ meeting.endTime ? formatDateTime(meeting.endTime) : '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="参会限制">
                {{ meeting.teamOnly ? '仅限团队成员' : '所有人可参加' }}
              </el-descriptions-item>
              <el-descriptions-item label="房间ID">{{ meeting.roomName }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="meeting-share">
            <h3>会议链接</h3>
            <div class="share-link-container">
              <el-input
                v-model="shareLink"
                readonly
                placeholder="会议链接"
                class="share-link-input">
                <el-button slot="append" icon="el-icon-document-copy" @click="copyShareLink">复制</el-button>
              </el-input>
              <div class="share-tip">分享此链接给需要参加会议的人</div>
            </div>
          </div>

          <div class="meeting-status-control" v-if="isTeamAdmin || isPlatformAdmin">
            <h3>会议状态控制</h3>
            <div class="status-control-container">
              <el-radio-group v-model="meetingStatus" @change="updateStatus">
                <el-radio label="SCHEDULED">未开始</el-radio>
                <el-radio label="ONGOING">进行中</el-radio>
                <el-radio label="ENDED">已结束</el-radio>
              </el-radio-group>
              <div class="status-tip">您可以修改会议状态，当会议结束时请将状态设置为“已结束”</div>
            </div>
          </div>

          <div class="meeting-actions" v-if="meeting.status !== 'ENDED'">
            <el-button type="primary" @click="joinMeeting">加入会议</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import api from '@/services/api'
import moment from 'moment'
import { mapGetters } from 'vuex'

export default {
  name: 'MeetingDetail',
  components: {
    Navbar
  },
  data() {
    return {
      meeting: null,
      loading: false,
      shareLink: '',
      meetingStatus: '',
      updatingStatus: false
    }
  },
  computed: {
    baseUrl() {
      return window.location.origin
    },
    ...mapGetters(['user']),
    isTeamAdmin() {
      return this.user.role === 'TEAM_ADMIN'
    },
    isPlatformAdmin() {
      return this.user.role === 'PLATFORM_ADMIN'
    }
  },
  methods: {
    fetchMeeting() {
      this.loading = true
      api.getMeeting(this.$route.params.id)
        .then(response => {
          this.meeting = response.data
          // 生成可分享的会议链接
          this.shareLink = `${this.baseUrl}/meetings/join/${this.meeting.id}`
          // 设置当前会议状态
          this.meetingStatus = this.meeting.status
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '获取会议详情失败')
          this.$router.push('/meetings')
        })
        .finally(() => {
          this.loading = false
        })
    },
    joinMeeting() {
      // 直接导航到会议加入页面
      this.$router.push(`/meetings/join/${this.meeting.id}`)
    },
    copyShareLink() {
      // 复制链接到剪贴板
      const el = document.createElement('textarea')
      el.value = this.shareLink
      document.body.appendChild(el)
      el.select()
      document.execCommand('copy')
      document.body.removeChild(el)

      this.$message.success('会议链接已复制到剪贴板')
    },
    updateStatus(status) {
      if (this.updatingStatus) return

      this.updatingStatus = true

      api.updateMeetingStatus(this.meeting.id, status)
        .then(response => {
          this.meeting = response.data.data
          this.$message.success('会议状态已更新')
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '更新会议状态失败')
          // 恢复原状态
          this.meetingStatus = this.meeting.status
        })
        .finally(() => {
          this.updatingStatus = false
        })
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
    this.fetchMeeting()
  }
}
</script>

<style scoped>
.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.meeting-info {
  margin-bottom: 30px;
}

.meeting-share, .meeting-status-control {
  margin: 30px 0;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.share-link-container, .status-control-container {
  margin-top: 10px;
}

.share-link-input {
  width: 100%;
}

.share-tip, .status-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.meeting-actions {
  margin-top: 20px;
  text-align: center;
}
</style>
