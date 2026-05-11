@echo off
setlocal

rem Always run from project root (where this .bat lives)
pushd "%~dp0"

if not exist "backend\mvnw.cmd" (
  echo [ERROR] File backend\mvnw.cmd not found.
  popd
  exit /b 1
)

if not exist "frontend\package.json" (
  echo [ERROR] File frontend\package.json not found.
  popd
  exit /b 1
)

echo Starting backend...
start "QuizMaster Backend" cmd /k "cd /d ""%~dp0backend"" && mvnw.cmd spring-boot:run"

echo Starting frontend on http://localhost:4200 ...
start "QuizMaster Frontend" cmd /k "cd /d ""%~dp0frontend"" && npm start -- --host localhost --port 4200"

echo.
echo Backend and frontend launch commands were sent.
echo Frontend URL: http://localhost:4200

popd
endlocal
