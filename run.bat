@echo off
title University Scheduler - Full Stack Application
echo ========================================================
echo   University Course Timetabling and Exam Scheduler
echo ========================================================
echo.

set "JAVA_HOME=C:\Users\Moneesh B\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Verifying Java 21 environment...
java -version
if %errorlevel% neq 0 (
    echo [ERROR] Java 21 could not be executed.
    pause
    exit /b %errorlevel%
)

echo.
echo Starting Spring Boot Backend and React Frontend on port 8081...
echo Open your browser at: http://localhost:8081
echo.
cd /d "%~dp0backend"
java -jar target\university-scheduler-1.0.0.jar
pause
