# 使用 Node.js 官方镜像
FROM node:18

# 设置工作目录
WORKDIR /app
RUN npm config set registry  https://registry.npmmirror.com
# 复制 package 文件并安装依赖
COPY package*.json ./
RUN npm install

# 复制项目文件
COPY . .

# 暴露端口
EXPOSE 5173

# 启动前端开发服务
CMD ["npm", "run", "dev"]