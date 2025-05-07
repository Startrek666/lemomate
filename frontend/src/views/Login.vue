<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="card-header">
        <h2>登录 Lemomate</h2>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm('loginForm')" :loading="loading">登录</el-button>
          <el-button @click="resetForm('loginForm')">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 100, message: '长度在 6 到 100 个字符', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('login', this.loginForm)
            .then(() => {
              this.$message.success('登录成功')
              this.$router.push('/')
            })
            .catch(err => {
              this.$message.error(err.response?.data?.message || '登录失败，请检查用户名和密码')
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
}

.register-link {
  margin-top: 20px;
  text-align: center;
}
</style>
