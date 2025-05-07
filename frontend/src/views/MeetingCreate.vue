<template>
  <div class="meeting-create">
    <Navbar />

    <div class="container">
      <div class="page-title">创建会议</div>

      <el-card>
        <el-form :model="meetingForm" :rules="rules" ref="meetingForm" label-width="100px">
          <el-form-item label="会议标题" prop="title">
            <el-input v-model="meetingForm.title" placeholder="请输入会议标题"></el-input>
          </el-form-item>

          <el-form-item label="会议描述" prop="description">
            <el-input type="textarea" v-model="meetingForm.description" placeholder="请输入会议描述"></el-input>
          </el-form-item>

          <el-form-item label="会议类型" prop="meetingType">
            <el-radio-group v-model="meetingForm.meetingType">
              <el-radio label="now">立即开始</el-radio>
              <el-radio label="scheduled">预定会议</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime" v-if="meetingForm.meetingType === 'scheduled'">
            <el-date-picker
              v-model="meetingForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="yyyy-MM-dd HH:mm"
              value-format="yyyy-MM-ddTHH:mm:ss">
            </el-date-picker>
          </el-form-item>

          <el-form-item label="结束时间" prop="endTime" v-if="meetingForm.meetingType === 'scheduled'">
            <el-date-picker
              v-model="meetingForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="yyyy-MM-dd HH:mm"
              value-format="yyyy-MM-ddTHH:mm:ss">
            </el-date-picker>
          </el-form-item>

          <el-form-item label="参会限制" prop="teamOnly">
            <el-switch
              v-model="meetingForm.teamOnly"
              active-text="仅限团队成员"
              inactive-text="所有人可参加">
            </el-switch>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('meetingForm')" :loading="loading">创建会议</el-button>
            <el-button @click="resetForm('meetingForm')">重置</el-button>
          </el-form-item>
        </el-form>
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
  name: 'MeetingCreate',
  components: {
    Navbar
  },
  data() {
    return {
      meetingForm: {
        title: '',
        description: '',
        meetingType: 'now',
        startTime: '',
        endTime: '',
        teamOnly: true,
        teamId: null
      },
      rules: {
        title: [
          { required: true, message: '请输入会议标题', trigger: 'blur' },
          { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
        ],
        description: [
          { max: 500, message: '长度不能超过500个字符', trigger: 'blur' }
        ],
        meetingType: [
          { required: true, message: '请选择会议类型', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' }
        ]
      },
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userTeam'])
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (!this.userTeam) {
            this.$message.error('您不属于任何团队，无法创建会议')
            return
          }

          this.loading = true

          // 准备会议数据
          const meetingData = {
            title: this.meetingForm.title,
            description: this.meetingForm.description,
            teamId: this.userTeam.id,
            teamOnly: this.meetingForm.teamOnly
          }

          // 设置会议时间
          if (this.meetingForm.meetingType === 'now') {
            meetingData.startTime = moment().format('YYYY-MM-DDTHH:mm:ss')
          } else {
            // 转换为 ISO 格式
            meetingData.startTime = moment(this.meetingForm.startTime).format('YYYY-MM-DDTHH:mm:ss')
            if (this.meetingForm.endTime) {
              meetingData.endTime = moment(this.meetingForm.endTime).format('YYYY-MM-DDTHH:mm:ss')
            }
          }

          api.createMeeting(meetingData)
            .then(() => {
              this.$message.success('会议创建成功')
              this.$router.push('/meetings')
            })
            .catch(err => {
              this.$message.error(err.response?.data?.message || '创建会议失败')
            })
            .finally(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>

<style scoped>
</style>
