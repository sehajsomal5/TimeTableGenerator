@echo off
cd src
echo Compiling...
javac timetable/*.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)
echo Running...
java timetable.Main
pause
