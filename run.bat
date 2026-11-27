@echo off
rem Builds, then starts the website at http://localhost:8080/board
cd /d "%~dp0"
call build.bat
java -cp out com.byahero.Main
