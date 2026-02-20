@echo off
echo Compiling...
if not exist bin mkdir bin
javac -d bin -sourcepath src src/timetable/*.java src/timetable/gui/*.java
if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)

echo Packaging into JAR...
jar cfm TimetableGenerator.jar Manifest.txt -C bin .
if %errorlevel% neq 0 (
    echo JAR Creation Failed!
    pause
    exit /b %errorlevel%
)

echo.
echo =========================================
echo SUCCESS! TimetableGenerator.jar created.
echo You can send this file to others.
echo =========================================
echo.
pause
