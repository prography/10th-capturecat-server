FROM openjdk:21-jdk

COPY build/libs/*SNAPSHOT.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "/app.jar"]
CMD ["--spring.profiles.active=dev"]
