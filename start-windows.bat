@echo off
cd /d "%~dp0"

echo Starting QuizBlitz server...
start "QuizBlitz Server" java -jar quizblitz.jar

echo Waiting for server to start...
timeout /t 3 /nobreak > nul

echo Opening host display...
start "" "http://localhost:8081/display.html"

echo.
echo Server is running in the QuizBlitz Server window.
echo Close that window to shut down the server.
pause
