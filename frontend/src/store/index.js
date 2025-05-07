import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'

Vue.use(Vuex)

// 设置axios默认值
axios.defaults.baseURL = 'http://localhost:8085/api'

export default new Vuex.Store({
  state: {
    status: '',
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user')) || {},
    teams: []
  },
  getters: {
    isLoggedIn: state => !!state.token,
    authStatus: state => state.status,
    user: state => state.user,
    userRole: state => state.user.role || '',
    userTeam: state => state.user.teamId ? { id: state.user.teamId, name: state.user.teamName } : null,
    teams: state => state.teams
  },
  mutations: {
    auth_request(state) {
      state.status = 'loading'
    },
    auth_success(state, { token, user }) {
      state.status = 'success'
      state.token = token
      state.user = user
    },
    auth_error(state) {
      state.status = 'error'
    },
    logout(state) {
      state.status = ''
      state.token = ''
      state.user = {}
    },
    set_teams(state, teams) {
      state.teams = teams
    },
    update_user(state, userData) {
      state.user = { ...state.user, ...userData }
      localStorage.setItem('user', JSON.stringify(state.user))
    }
  },
  actions: {
    login({ commit }, user) {
      return new Promise((resolve, reject) => {
        commit('auth_request')
        axios.post('/auth/login', user)
          .then(resp => {
            const token = resp.data.token
            const user = {
              id: resp.data.id,
              username: resp.data.username,
              realName: resp.data.realName,
              email: resp.data.email,
              role: resp.data.role,
              teamId: resp.data.teamId,
              teamName: resp.data.teamName
            }
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            axios.defaults.headers.common['Authorization'] = 'Bearer ' + token
            commit('auth_success', { token, user })
            resolve(resp)
          })
          .catch(err => {
            commit('auth_error')
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            reject(err)
          })
      })
    },
    register({ commit }, user) {
      return new Promise((resolve, reject) => {
        commit('auth_request')
        axios.post('/auth/register', user)
          .then(resp => {
            resolve(resp)
          })
          .catch(err => {
            commit('auth_error')
            reject(err)
          })
      })
    },
    logout({ commit }) {
      return new Promise((resolve) => {
        commit('logout')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        delete axios.defaults.headers.common['Authorization']
        resolve()
      })
    },
    fetchTeams({ commit }) {
      return new Promise((resolve, reject) => {
        axios.get('/teams/list')
          .then(resp => {
            commit('set_teams', resp.data)
            resolve(resp)
          })
          .catch(err => {
            reject(err)
          })
      })
    },
    updateUserProfile({ commit }, userData) {
      return new Promise((resolve, reject) => {
        axios.put('/users/profile', userData)
          .then(resp => {
            commit('update_user', resp.data.data)
            resolve(resp)
          })
          .catch(err => {
            reject(err)
          })
      })
    },
    uploadAvatar({ commit }, file) {
      return new Promise((resolve, reject) => {
        const formData = new FormData()
        formData.append('file', file)

        axios.post('/users/avatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
          .then(resp => {
            commit('update_user', { avatarUrl: resp.data.data.avatarUrl })
            resolve(resp)
          })
          .catch(err => {
            reject(err)
          })
      })
    }
  },
  modules: {
  }
})
