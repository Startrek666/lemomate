import axios from 'axios'

// 根据环境设置API URL
const API_URL = process.env.NODE_ENV === 'production'
  ? '/api'
  : 'http://localhost:8085/api'

// 添加请求拦截器
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 未授权，清除token并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default {
  // 认证相关
  login(credentials) {
    return axios.post(`${API_URL}/auth/login`, credentials)
  },
  register(user) {
    return axios.post(`${API_URL}/auth/register`, user)
  },

  // 用户相关
  getUserProfile() {
    return axios.get(`${API_URL}/users/profile`)
  },
  getAllUsers() {
    return axios.get(`${API_URL}/users/list`)
  },
  updateUserRole(userId, role) {
    return axios.put(`${API_URL}/users/${userId}/role`, role)
  },
  updateUserTeam(userId, teamId) {
    return axios.put(`${API_URL}/users/${userId}/team`, teamId)
  },
  updateUserProfile(userData) {
    return axios.put(`${API_URL}/users/profile`, userData)
  },
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post(`${API_URL}/users/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 团队相关
  getAllTeams() {
    return axios.get(`${API_URL}/teams/list`)
  },
  createTeam(team) {
    return axios.post(`${API_URL}/teams/create`, team)
  },
  getTeamMembers() {
    return axios.get(`${API_URL}/teams/members`)
  },
  getTeamApplications() {
    return axios.get(`${API_URL}/teams/applications`)
  },
  approveApplication(id) {
    return axios.post(`${API_URL}/teams/applications/${id}/approve`)
  },
  rejectApplication(id) {
    return axios.post(`${API_URL}/teams/applications/${id}/reject`)
  },
  applyToTeam(teamId) {
    return axios.post(`${API_URL}/teams/apply/${teamId}`)
  },

  // 会议相关
  createMeeting(meeting) {
    return axios.post(`${API_URL}/meetings/create`, meeting)
  },
  getTeamMeetings() {
    return axios.get(`${API_URL}/meetings/list`)
  },
  getMeeting(id) {
    return axios.get(`${API_URL}/meetings/${id}`)
  },
  joinMeeting(id) {
    return axios.get(`${API_URL}/meetings/join/${id}`)
  },
  updateMeetingStatus(id, status) {
    return axios.put(`${API_URL}/meetings/status/${id}`, { status })
  }
}
