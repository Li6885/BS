<template>
  <div>
    <div class="main" style="overflow-y: hidden; ">
      <el-container >
        <el-header class="title">
          <div style="margin-top: 12px; display: inline-block;">
            <img src="./components/icons/logo.png"
              style=" margin-right: 20px; height: 40px;vertical-align: middle;"  alt=""/>
            <span style="font-size: large; font-family: 'Microsoft YaHei',serif;
                color: black; font-weight: bold;">购物比价网站</span>
            <span style="margin-left: 40px; color:rgba(0, 0, 0, 0.2)">浙江大学B/S体系软件设计课程项目</span>
          </div>
        </el-header>
        <el-container style="width: 100%; ">
          <el-aside class="aside" style="display: flex;">
            <el-menu active-text-color="#ffd04b" background-color="#0270c1" default-active="1" text-color="#fff"
              style="height:100%; width: 100%;" :router="true">
              <el-menu-item index="login">
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>登录</span>
              </el-menu-item>
              <el-menu-item index="register">
                <el-icon>
                  <Edit  />
                </el-icon>
                <span>注册</span>
              </el-menu-item>
            </el-menu>
          </el-aside>

          <el-main style="height: 100%; width: 100%; ">
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
import {Edit, Plus, Postcard, Reading, Tickets, UserFilled} from "@element-plus/icons-vue";

export default {
  components: {Edit, Plus, UserFilled, Tickets, Postcard, Reading}
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
  width: 180px;
  background-color: red;
}

</style>
