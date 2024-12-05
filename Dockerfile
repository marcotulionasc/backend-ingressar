# Build stage
FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17
ARG PROFILE=prod
ARG APP_VERSION=1.0.0.0

WORKDIR /app
COPY --from=build /build/target/ticketeria-*.jar /app/

# Extract the JAR version
RUN APP_VERSION=$(ls /app | grep *.jar | awk 'NR==2{split($0,a,"-"); print a[3]}' | awk '{sub(/.jar$/,"")}1') \
    && echo "Building container with BSN v-$APP_VERSION"

EXPOSE 9090

ENV DB_URL=${DB_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar \
    -Dspring.profiles.active=${ACTIVE_PROFILE} \
    -Dspring.datasource.url=${DB_URL} \
    -Dspring.datasource.username=${DB_USER} \
    -Dspring.datasource.password=${DB_PASSWORD} \
    ticketeria-${JAR_VERSION}.jar