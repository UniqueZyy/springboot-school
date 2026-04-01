<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, createUser } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const isRegisterMode = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  code: '',
  roleType: 3 // 默认学生角色
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await login(loginForm.value)
    
    if (response.code === 200 && response.data) {
      userStore.setUserInfo(response.data)
      router.push('/home')
    } else {
      errorMessage.value = response.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.name || !registerForm.value.code) {
    errorMessage.value = '请填写必填项'
    return
  }

  if (!registerForm.value.email.includes('@')) {
    errorMessage.value = '请输入有效的邮箱'
    return
  }

  if (registerForm.value.phone.length !== 11) {
    errorMessage.value = '请输入有效的手机号'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const success = await createUser({
      ...registerForm.value,
      gender: 0 // 默认性别
    })
    
    if (success) {
      alert('注册成功，请登录')
      isRegisterMode.value = false
      registerForm.value = {
        username: '',
        password: '',
        name: '',
        email: '',
        phone: '',
        code: '',
        roleType: 3
      }
    } else {
      errorMessage.value = '注册失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('注册错误:', error)
  } finally {
    loading.value = false
  }
}

const isForgotPasswordMode = ref(false)
const forgotPasswordForm = ref({
  username: '',
  phone: '',
  newPassword: ''
})

const handleForgotPassword = () => {
  isForgotPasswordMode.value = true
  errorMessage.value = ''
}

const handleResetPassword = async () => {
  if (!forgotPasswordForm.value.username || !forgotPasswordForm.value.phone || !forgotPasswordForm.value.newPassword) {
    errorMessage.value = '请填写所有必填项'
    return
  }

  if (forgotPasswordForm.value.phone.length !== 11) {
    errorMessage.value = '请输入有效的手机号'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    // 这里需要调用后端API来验证用户名和手机号是否匹配，并重置密码
    // 假设后端有一个resetPassword接口
    const response = await fetch('http://localhost:8081/api/auth/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(forgotPasswordForm.value)
    })

    const result = await response.json()

    if (result.code === 200) {
      alert('密码重置成功，请使用新密码登录')
      isForgotPasswordMode.value = false
      forgotPasswordForm.value = {
        username: '',
        phone: '',
        newPassword: ''
      }
    } else {
      errorMessage.value = result.message || '信息不匹配，修改失败，如有需要请联系学校管理员重置密码'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
    console.error('重置密码错误:', error)
  } finally {
    loading.value = false
  }
}

const switchToLoginFromForgot = () => {
  isForgotPasswordMode.value = false
  errorMessage.value = ''
}

const switchToRegister = () => {
  isRegisterMode.value = true
  errorMessage.value = ''
}

const switchToLogin = () => {
  isRegisterMode.value = false
  errorMessage.value = ''
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h1>高校爱心捐赠系统</h1>
      
      <!-- 登录表单 -->
      <form v-if="!isRegisterMode && !isForgotPasswordMode" @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="login-username">用户名</label>
          <input
            id="login-username"
            v-model="loginForm.username"
            type="text"
            placeholder="请输入用户名"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="login-password">密码</label>
          <input
            id="login-password"
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </div>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <!-- 忘记密码表单 -->
      <form v-else-if="isForgotPasswordMode" @submit.prevent="handleResetPassword" class="login-form">
        <div class="form-group">
          <label for="forgot-username">用户名</label>
          <input
            id="forgot-username"
            v-model="forgotPasswordForm.username"
            type="text"
            placeholder="请输入用户名"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="forgot-phone">手机号</label>
          <input
            id="forgot-phone"
            v-model="forgotPasswordForm.phone"
            type="tel"
            placeholder="请输入手机号"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="forgot-new-password">新密码</label>
          <input
            id="forgot-new-password"
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            :disabled="loading"
          />
        </div>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '重置中...' : '重置密码' }}
        </button>
      </form>
      
      <!-- 注册表单 -->
      <form v-else @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="register-username">用户名</label>
          <input
            id="register-username"
            v-model="registerForm.username"
            type="text"
            placeholder="请输入用户名"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-password">密码</label>
          <input
            id="register-password"
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-name">姓名</label>
          <input
            id="register-name"
            v-model="registerForm.name"
            type="text"
            placeholder="请输入姓名"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-code">学号</label>
          <input
            id="register-code"
            v-model="registerForm.code"
            type="text"
            placeholder="请输入学号"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-email">邮箱</label>
          <input
            id="register-email"
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-phone">手机号</label>
          <input
            id="register-phone"
            v-model="registerForm.phone"
            type="tel"
            placeholder="请输入手机号"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="register-role">角色类型</label>
          <select
            id="register-role"
            v-model="registerForm.roleType"
            :disabled="loading"
            class="role-select"
          >
            <option value="1">学生</option>
            <option value="2">教师</option>
            <option value="3">校友</option>
            <option value="4">校外爱心人士</option>
          </select>
        </div>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <div class="extra-links">
        <div v-if="!isRegisterMode && !isForgotPasswordMode" class="forgot-password" @click="handleForgotPassword">忘记密码？</div>
        <div v-if="isForgotPasswordMode" class="mode-switch">
          <span>返回登录？</span>
          <span 
            class="switch-link" 
            @click="switchToLoginFromForgot()"
          >
            立即登录
          </span>
        </div>
        <div v-else-if="!isForgotPasswordMode" class="mode-switch">
          <span v-if="!isRegisterMode">还没有账号？</span>
          <span v-else>已有账号？</span>
          <span 
            class="switch-link" 
            @click="isRegisterMode ? switchToLogin() : switchToRegister()"
          >
            {{ isRegisterMode ? '立即登录' : '立即注册' }}
          </span>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('@/assets/445cf917c4e1c00.jpg');
  background-size: 100% 100%;
  background-position: center center;
  background-repeat: repeat;
  padding: 0;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; 
}

.login-box {
  background: white;
  border-radius: 0;
  padding: 50px 45px;
  width: 100%;
  height: auto;
  max-width: 420px;
  box-shadow: none;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 35px;
  font-size: 15px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
  width: 100%;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

input {
  padding: 12px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.role-select {
  padding: 12px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  background-color: white;
}

.role-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.role-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  color: #f44336;
  font-size: 14px;
  text-align: center;
  padding: 10px;
  background-color: #ffebee;
  border-radius: 5px;
  border: 1px solid #ffcdd2;
}

.login-button {
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 6px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(-1px);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.extra-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 15px;
}

.forgot-password {
  color: aqua;
  cursor: pointer;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.register-link {
  font-size: 14px;
  color: #666;
}

.register-link span {
  color: aqua;
  cursor: pointer;
}

.register-link span:hover {
  text-decoration: underline;
}

.mode-switch {
  font-size: 14px;
  color: #666;
}

.switch-link {
  color: aqua;
  cursor: pointer;
  margin-left: 5px;
}

.switch-link:hover {
  text-decoration: underline;
}
</style>
