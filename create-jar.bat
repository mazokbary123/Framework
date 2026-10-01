@echo off

set APP_NAME=framework
set SRC_DIR=src
set BUILD_DIR=build
set LIB_DIR=lib

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

dir /s /b "%SRC_DIR%\*.java" > sources.txt

javac -cp "%LIB_DIR%\*" -d "%BUILD_DIR%" @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR lors de la compilation.
    del sources.txt 2>nul
    pause
    exit /b 1
)

del sources.txt

if not exist "%BUILD_DIR%\lib" mkdir "%BUILD_DIR%\lib"
xcopy "%LIB_DIR%\*" "%BUILD_DIR%\lib\" /E /Y

cd /d "%BUILD_DIR%"
jar -cvf %APP_NAME%.jar *

echo.
echo JAR cree avec succes : %BUILD_DIR%/%APP_NAME%.jar
echo.
pause