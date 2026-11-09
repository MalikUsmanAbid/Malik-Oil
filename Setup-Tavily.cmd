@echo off
powershell.exe -NoProfile -File "%~dp0tools\setup-tavily-key.ps1" %*
echo.
pause
