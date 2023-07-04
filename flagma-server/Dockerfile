FROM eclipse-temurin:17
RUN mkdir /opt/app

COPY app/build/libs/app-uber.jar /opt/app
CMD ["java", "-jar", "/opt/app/app-uber.jar"]
