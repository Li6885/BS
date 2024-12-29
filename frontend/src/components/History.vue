<template>
  <el-scrollbar height="100%" style="width: 100%; height: 100%;">
    <div class="container">
      <div class="content-area">
        <!-- 返回按钮 -->
        <el-button type="text" @click="goBack" style="margin-bottom: 20px;">返回</el-button>

        <el-table :data="[product]" style="width: 100%">
          <el-table-column label="在售平台" prop="platform" align="center"></el-table-column>
          <el-table-column label="当前价格" prop="price" align="center">
            <template v-slot="scope">
              <strong>{{ scope.row.price }} 元</strong>
            </template>
          </el-table-column>
          <el-table-column label="详情介绍" prop="specification" align="center"></el-table-column>
          <el-table-column label="商品图片" align="center">
            <template v-slot="scope">
              <img :src="scope.row.image" alt="Product Image" class="product-image"/>
            </template>
          </el-table-column>
        </el-table>
        <h3 class="title">历史价格折线图</h3>
        <div>
          <div ref="chart" style="width: 100%; height: 400%"></div>
        </div>
        <div style="display: block">
          <h3 class="title">历史价格清单</h3>
        </div>
        <el-table :data="priceHistory" style="width: 100%" v-if="priceHistory.length > 0">
          <el-table-column label="时间" prop="time" align="center"></el-table-column>
          <el-table-column label="历史价格" prop="price" align="center"></el-table-column>
        </el-table>
        <!-- 如果没有历史价格 -->
        <div v-if="priceHistory.length === 0" style="text-align: center; margin-top: 20px;">
          <p>没有找到该商品的历史价格记录。</p>
        </div>
      </div>
    </div>
  </el-scrollbar>
</template>

<script>
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
  data() {
    return {
      // 获取查询参数
      chart: null,
      product: {
        productId: this.$route.query.productId,
        productName: this.$route.query.productName,
        platform: this.$route.query.platform,
        specification: this.$route.query.specification,
        price: this.$route.query.price,
        image: this.$route.query.image
      },
      priceHistory: [],  // 存储历史价格记录
      // 图表的配置项
      option: {
        title: {
          fontSize: 30,
          text: '历史价格趋势',
          left: 'center',
          textStyle: {
            fontSize: 20, // 主标题字体大小
            fontWeight: 'bold' // 字体加粗
          },
        },
        tooltip: {
          trigger: 'item',
          show: true,
          formatter: function (params) {
            let time = params.name;  // 获取横坐标时间
            let price = params.data;  // 获取纵坐标价格
            return `时间: ${time}<br>价格: ${price} 元`; // 格式化显示时间和价格
          },
          textStyle: {
            fontSize: 14,  // 设置字体大小
            color: '#333', // 设置字体颜色
            fontWeight: 'normal' // 设置字体粗细（可选）
          },
          axisPointer: {
            type: 'cross',  // 鼠标悬停时显示的指示器样式，可以是 'line', 'shadow' 或 'cross'
            crossStyle: {
              color: '#999' // 设置十字指示器的颜色
            }
          },
        },
        xAxis: {
          type: 'category',
          data: [],  // 动态填充时间数据
          name: '时间',
          nameTextStyle: {
            fontSize: 10,  // 缩小字体大小
            color: '#333',
          },
          axisLabel: {
            fontSize: 10,
            color: '#333',
          },
        },
        yAxis: {
          type: 'value',
          name: '价格/元',
          nameTextStyle: {
            fontSize: 10,  // 缩小字体大小
            color: '#333',
          },
        },
        series: [{
          name: '历史价格',
          type: 'line',  // 选择折线图类型
          data: [],  // 动态填充价格数据
          smooth: true,  // 平滑曲线
          lineStyle: {
            color: '#42A5F5'
          },
          symbol: 'circle',  // 设置数据点为圆形
          symbolSize: 8,  // 设置数据点的大小
        }]
      }
    };
  },
  mounted() {
    this.fetchPriceHistory();  // 获取历史价格数据
  },

  methods: {
    // 获取商品历史价格
    fetchPriceHistory() {
      axios.post('/history', { productId: this.product.productId })
          .then(response => {
            if (response.data.success) {
              let histories = response.data.history;
              histories.forEach(history => {
                this.priceHistory.push(history);
              })
              this.getPage();
            } else {
              ElMessage.error(response.data.message);
            }
          })
          .catch(error => {
            ElMessage.error(error.response.data.message);
          });
    },
    getPage() {
      // 获取 DOM 元素并初始化图表实例
      this.chart = this.$echarts.init(this.$refs.chart, null, {
        devicePixelRatio: window.devicePixelRatio > 3 ? window.devicePixelRatio : 4 // 设置设备像素比，避免模糊
      });

      // 从 priceHistory 中提取时间和价格
      const times = this.priceHistory.map(item => item.time);
      const prices = this.priceHistory.map(item => item.price);

      // 更新配置项的数据
      this.option.xAxis.data = times;
      this.option.series[0].data = prices;

      // 使用配置项设置图表
      this.chart.setOption(this.option);
    },
    // 处理窗口大小变化，调整图表大小
    // 返回上一页
    goBack() {
      this.$router.push('/home');  // 返回到商品比价页面
    }
  }
};
</script>

<style scoped>
/* 主要布局 */
/* 主要布局 */
.container {
  display: flex;
  flex-direction: column; /* 在手机端使用竖向布局 */
  height: 100%;
  background-color: #fafafa;
  width: 100%;
}

.content-area {
  flex: 1;
  padding: 20px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  margin-left: 20px;
  min-height: 80vh;
  width: 100%;
}

/* 商品信息 */
.product-info {
  margin-bottom: 30px;
  text-align: center;
}

.product-info .title {
  font-size: 2em;
  margin-bottom: 10px;
  font-weight: bold;
}

.product-image {
  width: 100%;  /* 使用百分比宽度以适应屏幕 */
  max-width: 150px;  /* 限制最大宽度 */
  height: auto;
  border-radius: 5px;
  margin-top: 10px;
}

/* 响应式设计：在手机端调整页面大小 */
@media (max-width: 768px) {
  .content-area {
    padding: 10px;  /* 减小内边距 */
  }

  .product-info .title {
    font-size: 1.5em; /* 在小屏幕上缩小标题字体 */
  }

  .product-image {
    width: 100%; /* 限制商品图片的最大宽度 */
    max-width: 120px;  /* 限制最大宽度 */
  }
}
</style>
