FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/points-service-1.0.0.jar app.jar
EXPOSE 8085
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
