@echo off
REM =====================================================
REM Contact Management System - Compilation Script
REM =====================================================

echo.
echo ========================================
echo Compiling Contact Management System...
echo ========================================
echo.

cd /d "%~dp0"

REM Compile all Java files
javac -cp ".;mysql-connector-j-9.6.0.jar;lib\*" ^
    Main.java ^
    model\Contact.java ^
    model\ImportResult.java ^
    model\User.java ^
    dao\ContactDAO.java ^
    dao\ContactDAOImpl.java ^
    dao\UserDAO.java ^
    dao\UserDAOImpl.java ^
    service\ContactService.java ^
    service\UserService.java ^
    util\DBConnection.java ^
    util\DatabaseInitializer.java ^
    ui\UITheme.java ^
    ui\IconFactory.java ^
    ui\IconRenderer.java ^
    ui\EmojiLabel.java ^
    ui\Toast.java ^
    ui\AvatarPanel.java ^
    ui\StatisticsPanel.java ^
    ui\ContactPreviewPanel.java ^
    ui\ContactFormDialog.java ^
    ui\RecycleBinDialog.java ^
    ui\ImportCSVDialog.java ^
    ui\LoginDialog.java ^
    ui\QRCodeDialog.java ^
    ui\ContactUI.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation Successful!
    echo ========================================
    echo.
    echo To run the application, use: run.bat
    echo.
) else (
    echo.
    echo ========================================
    echo Compilation Failed!
    echo ========================================
    echo.
    pause
)
