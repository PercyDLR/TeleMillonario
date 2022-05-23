FROM openjdk:17.0.2-jdk
VOLUME /tmp
EXPOSE 8080
ADD target/telemillonario-0.0.1-SNAPSHOT.jar telemillonario.jar
ENTRYPOINT ["java","-jar","telemillonario.jar"]

## Comandos para correrlo

# docker build -t telemillonario:v1 .
# docker run telemillonario:v1 --name telemillonario
# -p 80:8081 -p 3306:3306 -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/telemillonario
# --add-host=host.docker.internal:host-gateway