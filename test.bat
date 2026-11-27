@echo off
rem Builds, then runs all tests.
cd /d "%~dp0"
call build.bat
java -cp out com.byahero.TestRunner
