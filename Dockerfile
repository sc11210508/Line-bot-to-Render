# 階段 1：使用 Maven 進行編譯
FROM maven:3.9-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

# 階段 2：執行環境
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
