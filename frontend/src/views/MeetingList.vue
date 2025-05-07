<template>
  <div class="meeting-list">
    <Navbar />

    <div class="container">
      <div class="page-header">
        <div class="page-title">会议列表</div>
        <div class="page-actions" v-if="isTeamAdmin || isPlatformAdmin">
          <el-button type="primary" @click="$router.push('/meetings/create')">创建会议</el-button>
        </div>
      </div>

      <el-card v-if="!userTeam">
        <el-alert
          title="您不属于任何团队"
          type="warning"
          description="请先加入一个团队，才能查看会议列表。"
          show-icon
          :closable="false">
        </el-alert>
      </el-card>

      <el-card v-else>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="即将开始" name="upcoming">
            <el-table
              v-loading="loading"
              :data="upcomingMeetings"
              style="width: 100%"
              empty-text="暂无即将开始的会议">
              <el-table-column prop="title" label="会议标题"></el-table-column>
              <el-table-column label="开始时间">
                <template slot-scope="scope">
                  {{ formatDateTime(scope.row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column label="创建者" width="120">
                <template slot-scope="scope">
                  {{ scope.row.creatorName }}
                </template>
              </el-table-column>
              <el-table-column label="参会限制" width="120">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.teamOnly ? 'warning' : 'success'">
                    {{ scope.row.teamOnly ? '仅限团队成员' : '所有人可参加' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="primary"
                    @click="joinMeeting(scope.row.id)">加入会议</el-button>
                  <el-button
                    size="mini"
                    @click="viewMeetingDetails(scope.row.id)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="进行中" name="ongoing">
            <el-table
              v-loading="loading"
              :data="ongoingMeetings"
              style="width: 100%"
              empty-text="暂无进行中的会议">
              <el-table-column prop="title" label="会议标题"></el-table-column>
              <el-table-column label="开始时间">
                <template slot-scope="scope">
                  {{ formatDateTime(scope.row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column label="创建者" width="120">
                <template slot-scope="scope">
                  {{ scope.row.creatorName }}
                </template>
              </el-table-column>
              <el-table-column label="参会限制" width="120">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.teamOnly ? 'warning' : 'success'">
                    {{ scope.row.teamOnly ? '仅限团队成员' : '所有人可参加' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="success"
                    @click="joinMeeting(scope.row.id)">加入会议</el-button>
                  <el-button
                    size="mini"
                    @click="viewMeetingDetails(scope.row.id)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="已结束" name="ended">
            <el-table
              v-loading="loading"
              :data="endedMeetings"
              style="width: 100%"
              empty-text="暂无已结束的会议">
              <el-table-column prop="title" label="会议标题"></el-table-column>
              <el-table-column label="开始时间">
                <template slot-scope="scope">
                  {{ formatDateTime(scope.row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column label="结束时间">
                <template slot-scope="scope">
                  {{ scope.row.endTime ? formatDateTime(scope.row.endTime) : '未设置' }}
                </template>
              </el-table-column>
              <el-table-column label="创建者" width="120">
                <template slot-scope="scope">
                  {{ scope.row.creatorName }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    @click="viewMeetingDetails(scope.row.id)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
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
  name: 'MeetingList',
  components: {
    Navbar
  },
  data() {
    return {
      meetings: [],
      activeTab: 'ongoing',
      loading: false
    }
  },
  computed: {
    ...mapGetters(['user', 'userTeam']),
    isTeamAdmin() {
      return this.user.role === 'TEAM_ADMIN'
    },
    isPlatformAdmin() {
      return this.user.role === 'PLATFORM_ADMIN'
    },
    upcomingMeetings() {
      return this.meetings.filter(meeting => meeting.status === 'SCHEDULED')
    },
    ongoingMeetings() {
      return this.meetings.filter(meeting => meeting.status === 'ONGOING')
    },
    endedMeetings() {
      return this.meetings.filter(meeting => meeting.status === 'ENDED')
    }
  },
  methods: {
    fetchMeetings() {
      if (!this.userTeam) return

      this.loading = true
      api.getTeamMeetings()
        .then(response => {
          this.meetings = response.data
        })
        .catch(err => {
          this.$message.error('获取会议列表失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    joinMeeting(meetingId) {
      // 直接导航到会议加入页面
      this.$router.push(`/meetings/join/${meetingId}`)
    },
    viewMeetingDetails(meetingId) {
      this.$router.push(`/meetings/${meetingId}`)
    },
    formatDateTime(dateTime) {
      return moment(dateTime).format('YYYY-MM-DD HH:mm')
    }
  },
  created() {
    this.fetchMeetings()
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
</style>
