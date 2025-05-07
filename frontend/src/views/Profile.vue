<template>
  <div class="profile-container">
    <Navbar />
    
    <div class="container">
      <h2 class="page-title">个人资料</h2>
      
      <el-card v-loading="loading">
        <div class="profile-content">
          <div class="avatar-section">
            <div class="avatar-container">
              <img v-if="user.avatarUrl" :src="user.avatarUrl" class="avatar-image" alt="用户头像">
              <div v-else class="avatar-placeholder">
                {{ user.realName ? user.realName.charAt(0).toUpperCase() : '?' }}
              </div>
              
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :http-request="uploadAvatar"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload">
                  <el-button size="small" type="primary">更换头像</el-button>
                </el-upload>
              </div>
            </div>
          </div>
          
          <div class="profile-form">
            <el-form :model="profileForm" :rules="rules" ref="profileForm" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled></el-input>
              </el-form-item>
              
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="profileForm.realName"></el-input>
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email"></el-input>
              </el-form-item>
              
              <el-form-item label="角色">
                <el-tag :type="getRoleTagType(profileForm.role)">
                  {{ getRoleText(profileForm.role) }}
                </el-tag>
              </el-form-item>
              
              <el-form-item label="团队" v-if="profileForm.teamName">
                <el-tag type="success">{{ profileForm.teamName }}</el-tag>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="submitForm('profileForm')" :loading="saving">保存</el-button>
                <el-button @click="resetForm('profileForm')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import { mapGetters } from 'vuex'
import api from '@/services/api'

export default {
  name: 'Profile',
  components: {
    Navbar
  },
  data() {
    return {
      loading: false,
      saving: false,
      profileForm: {
        username: '',
        realName: '',
        email: '',
        role: '',
        teamId: null,
        teamName: ''
      },
      rules: {
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['user'])
  },
  methods: {
    fetchUserProfile() {
      this.loading = true
      api.getUserProfile()
        .then(response => {
          const userData = response.data
          this.profileForm = {
            username: userData.username,
            realName: userData.realName,
            email: userData.email,
            role: userData.role,
            teamId: userData.teamId,
            teamName: userData.teamName
          }
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '获取用户资料失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.saving = true
          
          const userData = {
            realName: this.profileForm.realName,
            email: this.profileForm.email
          }
          
          this.$store.dispatch('updateUserProfile', userData)
            .then(() => {
              this.$message.success('个人资料已更新')
            })
            .catch(err => {
              this.$message.error(err.response?.data?.message || '更新个人资料失败')
            })
            .finally(() => {
              this.saving = false
            })
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
      this.fetchUserProfile()
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        this.$message.error('上传头像图片只能是图片格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
      }
      
      return isImage && isLt2M
    },
    uploadAvatar(options) {
      this.loading = true
      this.$store.dispatch('uploadAvatar', options.file)
        .then(() => {
          this.$message.success('头像上传成功')
        })
        .catch(err => {
          this.$message.error(err.response?.data?.message || '头像上传失败')
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
        case 'USER': return 'info'
        case 'TEAM_ADMIN': return 'warning'
        case 'PLATFORM_ADMIN': return 'danger'
        default: return 'info'
      }
    }
  },
  created() {
    this.fetchUserProfile()
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px 0;
}

.profile-content {
  display: flex;
  flex-wrap: wrap;
}

.avatar-section {
  flex: 0 0 200px;
  margin-right: 30px;
  margin-bottom: 20px;
}

.profile-form {
  flex: 1;
  min-width: 300px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-image {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 15px;
  border: 1px solid #dcdfe6;
}

.avatar-placeholder {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 60px;
  margin-bottom: 15px;
}

.avatar-upload {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  
  .avatar-section {
    margin-right: 0;
    margin-bottom: 30px;
    width: 100%;
    display: flex;
    justify-content: center;
  }
}
</style>
