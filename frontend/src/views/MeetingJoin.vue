<template>
  <div class="meeting-join">
    <Navbar />

    <div class="container">
      <el-card v-loading="loading">
        <div v-if="meeting" class="meeting-info">
          <div class="meeting-header">
            <h2>{{ meeting.title }}</h2>
            <el-tag :type="getMeetingStatusType(meeting.status)">
              {{ getMeetingStatusText(meeting.status) }}
            </el-tag>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建者">{{ meeting.creatorName }}</el-descriptions-item>
            <el-descriptions-item label="团队">{{ meeting.teamName }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(meeting.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ meeting.endTime ? formatDateTime(meeting.endTime) : '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="参会限制">
              {{ meeting.teamOnly ? '仅限团队成员' : '所有人可参加' }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="meeting.description" class="meeting-description">
            <h3>会议描述</h3>
            <p>{{ meeting.description }}</p>
          </div>

          <div class="meeting-share" v-if="!error">
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

          <div class="meeting-actions" v-if="!error">
            <el-button type="primary" size="large" @click="joinMeeting" :loading="joiningMeeting">
              加入会议
            </el-button>
          </div>
        </div>

        <div v-if="error" class="error-message">
          <el-alert
            :title="error"
            type="error"
            description="请确认您有权限参加此会议，或联系会议创建者获取帮助。"
            show-icon
            :closable="false">
          </el-alert>

          <div class="error-actions">
            <el-button @click="$router.push('/meetings')">返回会议列表</el-button>
            <el-button v-if="!isLoggedIn" type="primary" @click="$router.push('/login')">登录</el-button>
          </div>
        </div>
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
  name: 'MeetingJoin',
  components: {
    Navbar
  },
  data() {
    return {
      meeting: null,
      loading: true,
      joiningMeeting: false,
      error: null,
      shareLink: ''
    }
  },
  computed: {
    baseUrl() {
      return window.location.origin
    },
    ...mapGetters(['isLoggedIn', 'user', 'userTeam'])
  },
  methods: {
    fetchMeeting() {
      this.loading = true
      this.error = null

      const meetingId = this.$route.params.id

      api.getMeeting(meetingId)
        .then(response => {
          this.meeting = response.data

          // 生成可分享的会议链接
          this.shareLink = `${this.baseUrl}/meetings/join/${this.meeting.id}`

          // 检查会议状态
          if (this.meeting.status === 'ENDED') {
            this.error = '会议已结束'
            return
          }

          // 检查团队限制
          if (this.meeting.teamOnly && (!this.userTeam || this.userTeam.id !== this.meeting.teamId)) {
            this.error = '该会议仅限团队成员参加'
          }
        })
        .catch(err => {
          this.error = err.response?.data?.message || '获取会议信息失败'
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    joinMeeting() {
      if (this.joiningMeeting) return

      this.joiningMeeting = true
      const meetingId = this.$route.params.id

      api.joinMeeting(meetingId)
        .then(response => {
          if (response.data.success && response.data.data.meetingUrl) {
            window.location.href = response.data.data.meetingUrl
          } else {
            this.error = '获取会议链接失败'
          }
        })
        .catch(err => {
          this.error = err.response?.data?.message || '加入会议失败'
        })
        .finally(() => {
          this.joiningMeeting = false
        })
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
    if (!this.isLoggedIn) {
      this.error = '请先登录'
      this.loading = false
      return
    }

    this.fetchMeeting()
  }
}
</script>

<style scoped>
.meeting-join {
  padding: 20px 0;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.meeting-description {
  margin: 20px 0;
}

.meeting-share {
  margin: 30px 0;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.share-link-container {
  margin-top: 10px;
}

.share-link-input {
  width: 100%;
}

.share-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.meeting-actions {
  margin-top: 30px;
  text-align: center;
}

.error-message {
  margin: 20px 0;
}

.error-actions {
  margin-top: 20px;
  text-align: center;
}
</style>
