<template>
  <el-scrollbar height="100%" style="width: 100%; height: 100%;">
    <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
      <!-- 使用媒体查询调整 el-card 宽度和内边距 -->
      <el-card class="register-card">
        <h3 style="text-align: center; font-size: 2em; margin-bottom: 20px; font-weight: bold;">用户注册</h3>

        <el-form :model="registerForm" ref="registerForm" label-width="80px">
          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username" :rules="usernameRules">
            <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item label="邮箱" prop="email" :rules="emailRules">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item label="密码" prop="password" :rules="passwordRules">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item label="确认密码" prop="confirmPassword" :rules="confirmPasswordRules">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
          </el-form-item>

          <!-- 注册按钮 -->
          <el-form-item>
            <el-button type="primary" @click="handleRegister" style="width: 100%;">注册</el-button>
          </el-form-item>

          <!-- 错误提示 -->
          <el-form-item v-if="registerError" style="text-align: center; color: red;">
            <span>注册失败，请检查输入的信息！</span>
          </el-form-item>
        </el-form>

        <!-- 跳转到登录页面 -->
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="text" @click="goToLogin">已有账号？立即登录</el-button>
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
      // 注册表单
      registerForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      registerError: false,
      usernameRules: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        {
          pattern: /^.{6,20}$/, // 匹配任意字符，长度为 6 到 20
          message: '长度需为6到20个字符',
          trigger: 'blur'
        }
      ],
      emailRules: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        {
          pattern: /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/,
          message: '请输入有效的邮箱地址',
          trigger: 'blur'
        }
      ],
      passwordRules: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        {
          pattern: /^(?=.*[a-zA-Z])(?=.*\d)[A-Za-z\d]{6,20}$/,
          message: '包含字母和数字，且长度为6到20个字符',
          trigger: 'blur'
        }
      ],
      confirmPasswordRules: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: this.checkPassword, trigger: 'blur' }
      ],
    };
  },
  methods: {
    // 检查确认密码是否与密码匹配
    checkPassword(rule, value, callback) {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    },

    // 注册功能
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          // 发起注册请求
          axios.post('/register', {
            username: this.registerForm.username,
            email: this.registerForm.email,
            password: this.registerForm.password
          })
              .then(response => {
                if (response.data.success) {
                  // 注册成功
                  ElMessage.success('注册成功！');
                  this.$router.push('/login'); // 注册成功后跳转到登录页面
                } else {
                  // 注册失败
                  ElMessage.error(response.data.message);
                }
              })
              .catch(error => {
                // 请求失败或后端错误
                ElMessage.error(error.response.data.message);
              });
        } else {
          // 如果表单验证失败，提示用户填写完整信息
          ElMessage.error('请填写完整的注册信息');
        }
      });
    },

    // 跳转到登录页面
    goToLogin() {
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
/* 默认 el-card 样式 */
.register-card {
  width: 400px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 响应式设计：当屏幕宽度小于 768px 时，调整卡片的宽度和内边距 */
@media (max-width: 768px) {
  .register-card {
    width: 90%;    /* 宽度设为 90% 以适应小屏幕 */
    padding: 10px; /* 减小内边距 */
  }
}

/* 响应式设计：当屏幕宽度小于 480px 时，进一步调整卡片宽度 */
@media (max-width: 480px) {
  .register-card {
    width: 100%;   /* 更改宽度为 100% 以适应更小屏幕 */
    padding: 5px; /* 更小的内边距 */
  }
}
</style>