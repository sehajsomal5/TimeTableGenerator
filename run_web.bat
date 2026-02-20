@echo off
echo Compiling Timetable Pro Cloud Edition...
mkdir bin 2>nul
javac -d bin -sourcepath src src/timetable/web/WebServer.java src/timetable/*.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)
echo.
echo ==================================================
echo   TIMETABLE PRO WEB SERVER IS RUNNING
echo ==================================================
echo.
java -cp bin timetable.web.WebServer
pause
