# Build stage
FROM openjdk:11-jdk-slim AS build
WORKDIR /app
COPY . .
RUN mkdir -p bin
RUN javac -d bin -sourcepath src src/timetable/*.java src/timetable/web/*.java

# Run stage
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/bin ./bin
COPY src/timetable/web ./src/timetable/web

ENV PORT=10000

CMD ["java", "-cp", "bin", "timetable.web.WebServer"]
