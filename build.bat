@echo off
rem Compiles the app and the tests into the "out" folder.
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java test\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
echo Build finished (see errors above, if any).
