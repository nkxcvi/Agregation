# Базовый образ с Maven и Java 17
FROM maven:3.8.5-openjdk-17-slim AS build

# Копируем проект и устанавливаем зависимости
COPY . /app
WORKDIR /app
RUN mvn clean install

# Разворачиваем приложение с Java
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/Agregation-0.0.1-SNAPSHOT.jar  /app.jar
CMD ["java", "-jar", "/app.jar"]
