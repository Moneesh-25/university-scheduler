@echo off
title University Scheduler - Backend Dev Mode
echo Starting Backend via Maven Wrapper on port 8081...

set "JAVA_HOME=C:\Users\Moneesh B\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0backend"
mvnw.cmd spring-boot:run
pause
