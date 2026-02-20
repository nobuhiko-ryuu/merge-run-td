@ECHO OFF
set ROOT_DIR=%~dp0
set DIST_DIR=%ROOT_DIR%\.gradle-dist
set GRADLE_VERSION=8.10.2
set GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Please run ./gradlew once on Linux/macOS to bootstrap Gradle distribution.
  exit /b 1
)
"%GRADLE_HOME%\bin\gradle.bat" %*
