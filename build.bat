@echo off
title Compilando UltimateBackpacks...

echo.
echo ===== COMPILANDO PLUGIN ULTIMATEBACKPACKS =====
echo.

:: Ejecutar Maven para limpiar y compilar
mvn clean package

echo.
echo ===== COMPILACIÓN TERMINADA =====
pause
