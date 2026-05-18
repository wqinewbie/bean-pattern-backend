@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@if "%DEBUG%"=="" @echo off
@setlocal

set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9
set WRAPPER_JAR=%USERPROFILE%\.m2\wrapper\dists\maven-wrapper-3.3.2.jar

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Downloading Maven 3.9.9...
  powershell -Command "& { Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP%\maven.zip' }"
  powershell -Command "& { Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%MAVEN_HOME%\..' -Force }"
  del "%TEMP%\maven.zip"
)

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper...
  powershell -Command "& { Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' }"
)

set MVN_CMD="%MAVEN_HOME%\bin\mvn.cmd"
%MVN_CMD% %*
