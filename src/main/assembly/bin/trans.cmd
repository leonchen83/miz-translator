@echo off
@setlocal

set ERROR_CODE=0

@REM ==== START VALIDATION ====

:chkMHome
rem Get directory of this script, then go one level up
pushd "%~dp0\.." >nul
set "RCT_HOME=%CD%"
popd >nul

if not "%RCT_HOME%"=="" goto stripMHome
goto error

:stripMHome
if not "_%RCT_HOME:~-1%"=="_\" goto setJava
set "RCT_HOME=%RCT_HOME:~0,-1%"
goto stripMHome

:setJava
rem ==== 优先使用内置 JDK ====
set "JAVACMD=%RCT_HOME%\jdk\bin\java.exe"

if exist "%JAVACMD%" goto checkMCmd

rem ==== fallback 到 PATH 中的 java ====
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
if not "%JAVACMD%"=="" goto checkMCmd

rem ==== fallback 到 JAVA_HOME ====
if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVACMD=%JAVA_HOME%\bin\java.exe"
        goto checkMCmd
    )
)

echo No Java found (bundled JDK missing and system Java not available). >&2
goto error

:checkMCmd
if exist "%RCT_HOME%\bin\trans.cmd" goto chkVersion
goto error

:chkVersion
for /f tokens^=2-5^ delims^=.-+_^" %%j in ('"%JAVACMD%" -fullversion 2^>^&1') do @set "JVER=%%j%%k%%l"

if %JVER% GEQ 180 goto init
echo java version is less than 1.8
goto error

@REM ==== END VALIDATION ====

:init
setLocal EnableDelayedExpansion

set CLASS_PATH="
for %%i in ("%RCT_HOME%\lib\*.jar") do (
    set CLASS_PATH=!CLASS_PATH!;%%i
)
set CLASS_PATH=!CLASS_PATH!"

set LOG_DIR=%RCT_HOME%\log
set CON_DIR=%RCT_HOME%\conf
set LOG_FILE=%CON_DIR%\log4j2.xml
set CON_FILE=%CON_DIR%\trans.conf
set MAIN_CLASS=org.example.Main

set RCT_OPS=-server ^
 -XX:+UseG1GC ^
 -XX:MaxGCPauseMillis=20 ^
 -XX:+ExitOnOutOfMemoryError ^
 -XX:InitiatingHeapOccupancyPercent=35 ^
 -XX:+ExplicitGCInvokesConcurrent ^
 -Dlog4j.configurationFile="%LOG_FILE%" ^
 -Dcli.log.path="%LOG_DIR%" ^
 -Dconf="%CON_FILE%" ^
 -Dtrans.home="%RCT_HOME%" ^
 -Dsun.stderr.encoding=UTF-8 ^
 -Dsun.stdout.encoding=UTF-8 ^
 -Dsun.err.encoding=UTF-8 ^
 -Dfile.encoding=UTF-8

if %JVER% GEQ 900 set ADD_OPENS=--add-opens=java.base/java.lang.invoke=ALL-UNNAMED

"%JAVACMD%" %ADD_OPENS% %RCT_OPS% -cp %CLASS_PATH% %MAIN_CLASS% %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%