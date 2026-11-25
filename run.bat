@echo off
title University Scheduler - Full Stack Application
echo ========================================================
echo   University Course Timetabling and Exam Scheduler
echo ========================================================
echo.

:: 1. Check if java is already in system PATH
where java >nul 2>nul
if %errorlevel% neq 0 (
    :: If not in PATH, check if JAVA_HOME is defined
    if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else if exist "C:\Users\Moneesh B\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe" (
        :: Moneesh's local fallback
        set "JAVA_HOME=C:\Users\Moneesh B\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    )
)

echo Verifying Java runtime environment...
java -version
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Java 21 was not found on this machine!
    echo Please install JDK 21 LTS from https://adoptium.net/
    pause
    exit /b 1
)

cd /d "%~dp0backend"

:: 2. Auto-build JAR if running from source archive without pre-compiled target
if not exist "target\university-scheduler-1.0.0.jar" (
    echo.
    echo [INFO] Pre-compiled JAR not detected. Building project via Maven Wrapper...
    echo (This may take 1-2 minutes on first run to download dependencies)
    call mvnw.cmd clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [ERROR] Maven build failed. Please check internet connection or JDK setup.
        pause
        exit /b %errorlevel%
    )
)

echo.
echo ========================================================
echo   Starting Spring Boot Server & Embedded React SPA
echo   Access URL: http://localhost:8081
echo ========================================================
echo.

java -jar target\university-scheduler-1.0.0.jar %*
pause
