# 使用官方的 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将构建好的 .jar 文件复制到容器中
COPY ShoppingPriceSystem-1.0-SNAPSHOT.jar /app/ShoppingPriceSystem-1.0-SNAPSHOT.jar

# 运行 Spring Boot 应用
CMD ["java", "-jar", "ShoppingPriceSystem-1.0-SNAPSHOT.jar"]