<template>
  <el-scrollbar height="100%" style="width: 100%; height: 100%;">
    <div class="container">
      <!-- 右边内容区域 -->
      <div class="content-area">
        <div class="user-info">
          <span class="welcome-message">尊敬的 {{ username }}, 您好!</span>
          <el-button type="danger" size="small" @click="logout" class="logout-btn">退出登录</el-button>
        </div>
        <!-- 商品信息展示 -->
        <h3 class="title">商品比价</h3>
        <!-- 查询条件 -->
        <el-form :model="searchForm" ref="searchForm" label-width="80px" class="search-form">
          <el-form-item label="商品名" prop="productName" :rules="productNameRules">
            <el-input v-model="searchForm.productName" placeholder="请输入商品名" class="input-field"></el-input>
          </el-form-item>

          <!-- 平台选择 -->
          <el-form-item label="平台">
            <el-checkbox-group v-model="searchForm.platforms" class="checkbox-group">
              <el-checkbox label="苏宁">苏宁</el-checkbox>
              <el-checkbox label="淘宝">淘宝</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <div class="form-buttons">
          <el-button type="primary" @click="handleSearch" class="search-btn">查询</el-button>
          <el-button type="info" @click="clearSearch" class="clear-btn">清空查询条件</el-button>
        </div>
        </el-form>

        <!-- 查询结果 -->
        <el-table v-if="productList.length > 0" :data="productList" class="product-table">
          <el-table-column label="平台" prop="platformName" align="center"></el-table-column>
          <el-table-column label="当前价格" prop="price" align="center"></el-table-column>
          <el-table-column label="规格" prop="specification" align="center"></el-table-column>
          <el-table-column label="图片" align="center">
            <template v-slot="scope">
              <img :src="scope.row.image" alt="Product Image" class="product-image"/>
            </template>
          </el-table-column>
          <el-table-column label="降价提醒" align="center">
            <template v-slot="scope">
              <el-button
                  type="primary"
                  size="small"
                  class="dynamic-font-size"
                  @click="setPriceAlert(scope.row.productId, scope.row.price)"
              >
                设置降价提醒
              </el-button>
            </template>
          </el-table-column>

          <el-table-column label="历史价格" align="center">
            <template v-slot="scope">
              <el-button
                  type="primary"
                  size="small"
                  class="dynamic-font-size"
                  @click="goToPriceHistory(scope.row.productId, scope.row.productName, scope.row.platformName, scope.row.price, scope.row.specification, scope.row.image)"
              >
                查询历史价格
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-scrollbar>
</template>

<script>
import { ElLoading, ElMessage } from 'element-plus';
import axios from 'axios';

export default {
  created() {
    localStorage.setItem("isLog", "true");
    this.username = localStorage.getItem("sessionName");
    let products = localStorage.getItem("products");
    if (products !== null) {
      this.productList = JSON.parse(products);
    }
    let searchForm = localStorage.getItem("searchForm");
    if (searchForm !== null) {
      this.searchForm = JSON.parse(searchForm);
    }
  },
  data() {
    return {
      // 查询表单
      searchForm: {
        productName: '',
        platforms: []  // 存储选择的平台
      },
      productList: [],
      productNameRules: [
        { required: true, message: '请输入商品名', trigger: 'blur' },
      ],
    };
  },
  methods: {
    // 处理查询
    handleSearch() {
      if (!this.searchForm.productName) {
        // 如果商品名为空，提醒用户输入商品名
        ElMessage.warning('请输入商品名');
        return;
      }
      this.productList = []
      let loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
      });
      // 发起查询请求
      axios.post('/search', this.searchForm)
          .then(response => {
            if (response.data.success) {
              // 查询成功，更新商品列表
              let products = response.data.products;
              products.forEach(product => {
                this.productList.push(product);
              })
            } else {
              ElMessage.error(response.data.message);
            }
            localStorage.setItem("products", JSON.stringify(this.productList));
            localStorage.setItem("searchForm", JSON.stringify(this.searchForm));
          })
          .catch(error => {
            ElMessage.error(error.response.data.message);
            localStorage.setItem("products", JSON.stringify(this.productList));
            localStorage.setItem("searchForm", JSON.stringify(this.searchForm));
          })
          .finally(() => {
            loadingInstance.close();
          });
    },
    // 清空查询条件
    clearSearch() {
      this.searchForm.productName = '';
      this.searchForm.platforms = [];
      this.productList = [];
      localStorage.removeItem("products");
      localStorage.removeItem("searchForm");
    },
    setPriceAlert(productId, price) {
      let email = localStorage.getItem("email");
      axios.post('/alert', {
        productId: productId,
        email: email,
        price: price
      })
          .then(response => {
            if (response.data.success) { // 设置降价提醒成功
              ElMessage.success(response.data.message);
            } else {
              ElMessage.error(response.data.message);
            }
          })
          .catch(error => {
            ElMessage.error(error.response.data.message);
          });
    },

    // 查询历史价格
    goToPriceHistory(productId, productName, platform, price, specification, image) {
      // 传递商品信息并跳转到历史价格页面
      this.$router.push({ name: 'History',
        query: {
          productId: productId,
          productName: productName,
          platform: platform,
          price: price,
          specification: specification,
          image: image
        }
      });
    },
    logout() {
      let username = localStorage.getItem("sessionName");
      axios.post('/logout', {
        username: username
      })
          .then(response => {
            if (response.data.success) { // 退出会话成功
              localStorage.removeItem('TELLER_SESSION_ID');
              localStorage.removeItem("sessionName");
              localStorage.removeItem("email");
              localStorage.removeItem("isLog");
              localStorage.removeItem("products");
              localStorage.removeItem("searchForm");
              // 显示退出成功提示
              this.$router.push('/login');  // 跳转到登录页面
              ElMessage.success('退出登录成功');
            } else {
              ElMessage.error(response.data.message);
            }
          })
          .catch(error => {
            ElMessage.error(error.response.data.message);
          });
    }
  }
};
</script>

<style scoped>
/* 主要布局 */
.container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fafafa;
}

.content-area {
  flex: 1;
  padding: 10px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  margin-left: 10px;
  min-height: 80vh;
}

.title {
  font-size: 1.5em;
  margin-bottom: 10px;
  font-weight: bold;
  text-align: center;
  padding: 0 10px;
}

/* 用户信息 */
.user-info {
  display: flex;
  flex-direction: column; /* 改为垂直布局 */
  align-items: flex-end; /* 右对齐 */
  margin-bottom: 20px;
}

.welcome-message {
  font-size: 14px;
  margin-bottom: 8px;
}

.logout-btn {
  margin: 0;
}

/* 查询表单 */
.search-form {
  margin-top: 10px;
  margin-bottom: 15px;
}

.input-field {
  width: 100%;
}

.checkbox-group {
  display: flex;
  justify-content: space-between;
  margin-top: 0;
}

.form-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.search-btn, .clear-btn {
  width: 48%; /* 保证按钮间距 */
  margin: 5px 0;
}

/* 表格样式 */
.product-table {
  margin-top: 20px;
}

.product-image {
  width: 100%;  /* 使图片宽度占满容器 */
  height: auto; /* 自动调整高度，保持图片比例 */
  max-width: 80px;  /* 限制最大宽度，避免图片过大 */
  border-radius: 10px;
}

.dynamic-font-size {
  font-size: 12px;  /* 默认字体大小 */
  padding: 8px 12px; /* 增加内边距，确保按钮内容不会被裁剪 */
  border-radius: 5px; /* 圆角边框 */
  box-sizing: border-box; /* 包括边框和内边距在内的总宽度计算 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title {
    font-size: 1.2em;
  }

  .input-field {
    width: 100%;
  }

  .product-table {
    font-size: 10px;
  }

  .search-form {
    width: 100%;
  }

  .form-buttons {
    display: flex;
    justify-content: space-between;
    gap: 10px; /* 按钮之间的间距 */
  }

  .search-btn, .clear-btn {
    width: 48%; /* 保证按钮间距 */
  }

  .dynamic-font-size {
    font-size: 8px; /* 小屏幕字体大小 */
    padding: 4px 9px; /* 增加内边距，确保按钮内容不会被裁剪 */
  }

  .product-image {
    width: 100%;  /* 图片宽度100%，适应小屏幕 */
    max-width: 150px;  /* 限制最大宽度 */
  }
}
</style>
