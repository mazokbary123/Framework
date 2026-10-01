@echo off
setlocal enabledelayedexpansion

pushd "%~dp0"

REM --------------------------
REM VARIABLES
REM --------------------------
set "APP_NAME=framework"
set "SRC_DIR=src"
set "OUT_DIR=out"
set "CLASSES_DIR=%OUT_DIR%\classes"
set "LIB_DIR=%OUT_DIR%\lib"
set "LIB=lib"

echo.
echo ===================================
echo Nettoyage...
echo ===================================

if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"

mkdir "%CLASSES_DIR%"
mkdir "%LIB_DIR%"

echo.
echo ===================================
echo Compilation Java...
echo ===================================

dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\sources.txt"

javac -cp "%LIB%\*" -d "%CLASSES_DIR%" @"%TEMP%\sources.txt"

set "RESULT=%ERRORLEVEL%"

del "%TEMP%\sources.txt" 2>nul

if %RESULT% neq 0 (
    echo.
    echo ERREUR : Compilation echouee.
    popd
    pause
    exit /b 1
)

echo.
echo ===================================
echo Copie des JARs dependances...
echo ===================================

for %%j in ("%LIB%\*.jar") do (
    if exist "%%~fj" (
        echo Copie de %%~nxj
        copy /Y "%%~fj" "%LIB_DIR%\" >nul
    )
)

echo.
echo ===================================
echo Creation du %APP_NAME%.jar...
echo ===================================

jar cf "%LIB_DIR%\%APP_NAME%.jar" -C "%CLASSES_DIR%" .

if errorlevel 1 (
    echo ERREUR : Creation du JAR impossible.
    popd
    pause
    exit /b 1
)

echo.
echo JAR cree :
echo   %LIB_DIR%\%APP_NAME%.jar

echo.
echo ===================================
echo BUILD TERMINE avec succes !
echo ===================================

popd
pause
endlocal