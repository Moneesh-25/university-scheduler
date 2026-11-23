@echo off
title University Scheduler - Frontend Dev Mode
echo Starting Frontend via Vite Dev Server on port 5173...

cd /d "%~dp0frontend"
npm.cmd run dev
pause
