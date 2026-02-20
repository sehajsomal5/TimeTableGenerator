@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath src src/timetable/*.java src/timetable/gui/*.java
if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)
echo Compilation Successful! Starting GUI...
java -cp bin timetable.gui.TimetableGUI
