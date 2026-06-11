FROM eclipse-temurin:21-jdk-alpine

# 將編譯好的 JAR 檔複製到容器內
COPY target/*.jar app.jar

# 指定啟動命令
ENTRYPOINT ["java","-jar","/app.jar"]
