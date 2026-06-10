FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/notification-service-1.0.0.jar app.jar
EXPOSE 8084
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
