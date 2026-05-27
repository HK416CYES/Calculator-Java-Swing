@echo off
setlocal
cd /d %~dp0
start "" javaw -jar ScientificCalculator.jar
if errorlevel 1 java -jar ScientificCalculator.jar
endlocal
