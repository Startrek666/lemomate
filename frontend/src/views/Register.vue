<template>
  <div class="register-container">
    <el-card class="register-card">
      <div slot="header" class="card-header">
        <h2>注册 Lemomate</h2>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        
        <el-form-item label="选择团队" prop="teamId">
          <el-select v-model="registerForm.teamId" placeholder="请选择团队" style="width: 100%;">
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.teamName"
              :value="team.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm('registerForm')" :loading="loading">注册</el-button>
          <el-button @click="resetForm('registerForm')">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '@/services/api'

export default {
  name: 'Register',
  data() {
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    
    return {
      registerForm: {
        username: '',
        realName: '',
        email: '',
        password: '',
        confirmPassword: '',
        teamId: null
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 100, message: '长度在 6 到 100 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' }
        ]
      },
      teams: [],
      loading: false
    }
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.loading = true
          
          // 移除确认密码字段
          const userData = { ...this.registerForm }
          delete userData.confirmPassword
          
          api.register(userData)
            .then(response => {
              this.$message.success(response.data.message || '注册成功')
              this.$router.push('/login')
            })
            .catch(err => {
              this.$message.error(err.response?.data?.message || '注册失败')
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
    },
    fetchTeams() {
      api.getAllTeams()
        .then(response => {
          this.teams = response.data
        })
        .catch(err => {
          console.error('获取团队列表失败', err)
        })
    }
  },
  created() {
    this.fetchTeams()
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px 0;
}

.register-card {
  width: 500px;
}

.card-header {
  text-align: center;
}

.login-link {
  margin-top: 20px;
  text-align: center;
}
</style>
