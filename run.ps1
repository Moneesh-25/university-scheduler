Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  University Course Timetabling and Exam Scheduler" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$env:JAVA_HOME = "C:\Users\Moneesh B\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Verifying Java 21 environment..." -ForegroundColor Yellow
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java 21 could not be executed." -ForegroundColor Red
    pause
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Starting Full-Stack Application on http://localhost:8081 ..." -ForegroundColor Green
Write-Host "Open your browser at: http://localhost:8081" -ForegroundColor Green
Write-Host ""

Set-Location "$PSScriptRoot\backend"
java -jar target\university-scheduler-1.0.0.jar
