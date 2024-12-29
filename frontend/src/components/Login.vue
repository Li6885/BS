<template>
  <el-scrollbar height="100%" style="width: 100%; height: 100%;">
    <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
      <!-- 使用媒体查询调整 el-card 宽度和内边距 -->
      <el-card class="login-card">
        <h3 style="text-align: center; font-size: 2em; margin-bottom: 20px; font-weight: bold;">用户登录</h3>

        <el-form :model="loginForm" ref="loginForm" label-width="80px">
          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username" :rules="usernameRules">
            <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item label="密码" prop="password" :rules="passwordRules">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button type="primary" @click="handleLogin" style="width: 100%;">登录</el-button>
          </el-form-item>

          <!-- 错误提示 -->
          <el-form-item v-if="loginError" style="text-align: center; color: red;">
            <span>用户名或密码错误！</span>
          </el-form-item>
        </el-form>

        <!-- 注册按钮 -->
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="text" @click="goToRegister">没有账号？立即注册</el-button>
        </div>
      </el-card>
    </div>
  </el-scrollbar>
</template>

<script>
import { ElMessage } from 'element-plus'; // 导入 ElMessage
import axios from 'axios'; // 导入 axios

export default {
  created() {
    let isLog = localStorage.getItem("isLog");
    if(isLog && isLog === "true") {
      let username = localStorage.getItem("sessionName");
      axios.post('/logout', {
        username: username
      })
          .then(response => {
            if (response.data.success) {// 退出会话成功
              localStorage.removeItem('TELLER_SESSION_ID');
              localStorage.removeItem("sessionName");
              localStorage.removeItem("isLog");
              localStorage.removeItem("email");
              localStorage.removeItem("products");
              localStorage.removeItem("searchForm");
              // 显示退出成功提示
              ElMessage.success(response.data.message);
            } else {
              ElMessage.error(response.data.message);
              this.$router.push('/home');  // 跳转回搜索页面
            }
          })
          .catch(error => {
            ElMessage.error(error.response.data.message);
            this.$router.push('/home');  // 跳转回搜索页面
          });
    }
  },
  data() {
    return {
      // 登录表单
      loginForm: {
        username: '',
        password: ''
      },
      loginError: false,
      usernameRules: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
      ],
      passwordRules: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
    };
  },
  methods: {
    // 登录功能
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          // 发起登录请求
          axios.post('/login', {
            username: this.loginForm.username,
            password: this.loginForm.password
          })
              .then(response => {
                if (response.data.success) {
                  // 登录成功
                  localStorage.setItem('TELLER_SESSION_ID', response.data.token);  // 可以存储在 sessionStorage 中
                  localStorage.setItem("sessionName", this.loginForm.username);
                  ElMessage.success('登录成功！');
                  localStorage.setItem("email", response.data.email);
                  this.$router.push('/home'); // 登录成功后跳转
                } else {
                  // 用户名或密码错误
                  ElMessage.error(response.data.message);
                }
              })
              .catch(error => {
                // 请求失败或后端错误
                ElMessage.error(error.response.data.message);
              });
        } else {
          // 如果表单验证失败，提示用户填写用户名和密码
          ElMessage.error('请输入用户名和密码');
        }
      });
    },

    // 跳转到注册页面
    goToRegister() {
      this.$router.push('/register');
    }
  }
};
</script>

<style scoped>
/* 默认 el-card 样式 */
.login-card {
  width: 400px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 响应式设计：当屏幕宽度小于 768px 时，调整卡片的宽度和内边距 */
@media (max-width: 768px) {
  .login-card {
    width: 90%;    /* 宽度设为 90% 以适应小屏幕 */
    padding: 10px; /* 减小内边距 */
  }
}

/* 响应式设计：当屏幕宽度小于 480px 时，进一步调整卡片宽度 */
@media (max-width: 480px) {
  .login-card {
    width: 100%;   /* 更改宽度为 100% 以适应更小屏幕 */
    padding: 5px; /* 更小的内边距 */
  }
}
</style>