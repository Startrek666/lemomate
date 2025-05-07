<template>
  <div class="admin-teams">
    <Navbar />
    
    <div class="container">
      <div class="page-header">
        <div class="page-title">团队管理</div>
        <div class="page-actions">
          <el-button type="primary" @click="showCreateTeamDialog">添加团队</el-button>
        </div>
      </div>
      
      <el-card>
        <el-table
          v-loading="loading"
          :data="teams"
          style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="teamName" label="团队名称"></el-table-column>
          <el-table-column prop="memberCount" label="成员数量" width="120"></el-table-column>
        </el-table>
      </el-card>
      
      <!-- 创建团队对话框 -->
      <el-dialog title="添加团队" :visible.sync="createTeamDialogVisible" width="500px">
        <el-form :model="teamForm" :rules="teamRules" ref="teamForm" label-width="100px">
          <el-form-item label="团队名称" prop="teamName">
            <el-input v-model="teamForm.teamName" placeholder="请输入团队名称"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="createTeamDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createTeam" :loading="createLoading">确定</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import Navbar from '@/components/Navbar.vue'
import api from '@/services/api'

export default {
  name: 'AdminTeams',
  components: {
    Navbar
  },
  data() {
    return {
      teams: [],
      loading: false,
      createTeamDialogVisible: false,
      createLoading: false,
      teamForm: {
        teamName: ''
      },
      teamRules: {
        teamName: [
          { required: true, message: '请输入团队名称', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    fetchTeams() {
      this.loading = true
      api.getAllTeams()
        .then(response => {
          this.teams = response.data
        })
        .catch(err => {
          this.$message.error('获取团队列表失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    showCreateTeamDialog() {
      this.createTeamDialogVisible = true
      this.$nextTick(() => {
        this.$refs.teamForm.resetFields()
      })
    },
    createTeam() {
      this.$refs.teamForm.validate((valid) => {
        if (valid) {
          this.createLoading = true
          api.createTeam(this.teamForm)
            .then(response => {
              this.$message.success(response.data.message || '团队创建成功')
              this.createTeamDialogVisible = false
              this.fetchTeams()
            })
            .catch(err => {
              this.$message.error(err.response?.data?.message || '创建团队失败')
            })
            .finally(() => {
              this.createLoading = false
            })
        } else {
          return false
        }
      })
    }
  },
  created() {
    this.fetchTeams()
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
