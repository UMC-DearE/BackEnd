FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# GitHub Actions에서 빌드된 JAR를 컨텍스트에 app.jar로 넣어둔 뒤 COPY
COPY app.jar /app/app.jar

ENV TZ=Asia/Seoul

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]