#!/bin/bash
# Double-click this file to start QuizBlitz on Mac.
# If you see "permission denied", run this once in Terminal first:
#   chmod +x start-mac.command

cd "$(dirname "$0")"

echo "Starting QuizBlitz server..."
java -jar quizblitz.jar &
SERVER_PID=$!

echo "Waiting for server to start..."
sleep 3

echo "Opening host display..."
open "http://localhost:8081/display.html"

echo ""
echo "Server is running. Press Ctrl+C here to shut it down."
wait $SERVER_PID
