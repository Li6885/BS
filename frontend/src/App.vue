<template>
  <div>
    <div class="main" style="overflow-y: hidden;">
      <el-container>
        <el-header class="title">
          <div class="logo-container">
            <img src="./components/icons/logo.png"
                 class="logo" alt=""/>
            <span class="main-title">购物比价网站</span>
            <span class="sub-title">浙江大学B/S体系软件设计课程项目</span>
          </div>
        </el-header>
        <el-container style="width: 100%;">
          <el-aside class="aside" :style="asideStyle">
            <el-menu active-text-color="#ffd04b" background-color="#0270c1" default-active="1" text-color="#fff" style="height:100%; width: 100%;" :router="true">
              <el-menu-item index="login" class="menu-item">
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>登录</span>
              </el-menu-item>
              <el-menu-item index="register" class="menu-item">
                <el-icon>
                  <Edit />
                </el-icon >
                <span>注册</span>
              </el-menu-item>
            </el-menu>
          </el-aside>

          <el-main style="height: 100%; width: 100%;">
            <el-scrollbar height="100%">
              <RouterView class="content" style="height: 90vh; max-height: 100%; background-color: white; color: black;" />
            </el-scrollbar>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </div>
</template>

<script>
import { Edit, UserFilled } from "@element-plus/icons-vue";

export default {
  components: { Edit, UserFilled },
  data() {
    return {
      // 用于控制侧边栏在手机端是否折叠
      isMobile: false
    };
  },
  mounted() {
    // 检查屏幕大小并初始化 `isMobile`
    this.checkMobileScreen();
    window.addEventListener("resize", this.checkMobileScreen);
  },
  destroyed() {
    window.removeEventListener("resize", this.checkMobileScreen);
  },
  methods: {
    checkMobileScreen() {
      this.isMobile = window.innerWidth <= 768; // 设置为768px为临界点，手机设备宽度一般都小于768px
    }
  },
  computed: {
    asideStyle() {
      return {
        width: this.isMobile ? "0px" : "120px", // 在手机上缩小侧边栏
        transition: "width 1s" // 添加过渡效果
      };
    }
  }
}
</script>

<style scoped>
#app {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: #dcdcdc;
  width: 100vw;
  height: 100vh;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 100%;
  min-height: 100%;
  height: auto;
  background-color: #dcdcdc;
}

.title {
  background-color: #ffffff;
  height: 60px;
}

.aside {
  min-height: calc(100vh - 60px);
  transition: width 1s ease; /* 使得侧边栏在调整时有过渡效果 */
}

.menu-item {
  display: flex;               /* 使用 Flexbox */
  justify-content: center;     /* 水平居中 */
  align-items: center;         /* 垂直居中 */
}

.logo-container {
  display: inline-block;
  white-space: nowrap;
  margin-top: 12px;
}

.logo {
  margin-right: 20px;
  height: 40px;
  vertical-align: middle;
}

.main-title {
  font-size: 24px;
  font-family: 'Microsoft YaHei', serif;
  color: black;
  font-weight: bold;
}

.sub-title {
  margin-left: 40px;
  color: rgba(0, 0, 0, 0.2);
  font-size: 16px;
}

.content {
  background-color: white;
  color: black;
  padding: 20px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .title {
    text-align: center;
  }

  .content {
    padding: 5px; /* 在手机屏幕上减少内边距 */
  }

  .main-title {
    font-size: 15px; /* 在小屏幕上字体更小 */
  }

  .sub-title {
    font-size: 10px; /* 子标题字体也更小 */
  }

  .logo-container {
    white-space: normal; /* 如果屏幕较小，允许换行 */
    text-align: center; /* 在小屏幕上居中显示 */
  }
}
</style>
