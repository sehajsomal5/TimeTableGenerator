# -------- Build Stage --------
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy entire project
COPY . .

# Create bin folder
RUN mkdir -p bin

# Compile Java files
RUN javac -d bin -sourcepath src src/timetable/*.java src/timetable/web/*.java


# -------- Run Stage --------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy compiled classes from build stage
COPY --from=build /app/bin ./bin
COPY src/timetable/web ./web

# Expose Render port (Render provides PORT automatically)
EXPOSE 10000

# Start the web server
CMD ["java", "-cp", "bin", "timetable.web.WebServer"]
