param(
    [string]$KeystorePath = "nocnom-upload.jks",
    [string]$Alias = "nocnom-upload"
)

$ErrorActionPreference = "Stop"

try {
    $keytool = (Get-Command keytool -ErrorAction Stop).Source
} catch {
    Write-Error "Không tìm thấy keytool. Cài JDK 17+ hoặc dùng JDK đi kèm Android Studio rồi chạy lại."
    exit 1
}

if (Test-Path $KeystorePath) {
    Write-Error "File '$KeystorePath' đã tồn tại. Không ghi đè khóa ký hiện có. Hãy sao lưu file này hoặc chọn đường dẫn khác."
    exit 1
}

Write-Host "=== nOcnOm Google Play Upload Key ===" -ForegroundColor Cyan
Write-Host "Keytool sẽ hỏi mật khẩu. Hãy lưu mật khẩu vào password manager; không commit hoặc gửi vào chat."
Write-Host "Alias cố định: $Alias"
Write-Host ""

& $keytool -genkeypair -v `
    -keystore $KeystorePath `
    -alias $Alias `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000

if ($LASTEXITCODE -ne 0) {
    Write-Error "Tạo keystore thất bại."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "=== SHA-1 / SHA-256 để thêm vào Firebase ===" -ForegroundColor Cyan
& $keytool -list -v -keystore $KeystorePath -alias $Alias

if ($LASTEXITCODE -ne 0) {
    Write-Error "Không thể đọc fingerprint từ keystore."
    exit $LASTEXITCODE
}

$bytes = [IO.File]::ReadAllBytes((Resolve-Path $KeystorePath))
$base64 = [Convert]::ToBase64String($bytes)
$base64 | Set-Clipboard

Write-Host ""
Write-Host "ĐÃ TẠO: $KeystorePath" -ForegroundColor Green
Write-Host "ANDROID_KEYSTORE_BASE64 đã được copy vào clipboard." -ForegroundColor Green
Write-Host ""
Write-Host "Tạo 4 GitHub Repository Secrets:" -ForegroundColor Yellow
Write-Host "  ANDROID_KEYSTORE_BASE64  = paste clipboard"
Write-Host "  ANDROID_KEYSTORE_PASSWORD = mật khẩu keystore vừa đặt"
Write-Host "  ANDROID_KEY_ALIAS         = $Alias"
Write-Host "  ANDROID_KEY_PASSWORD      = mật khẩu key vừa đặt"
Write-Host ""
Write-Host "QUAN TRỌNG: Sao lưu $KeystorePath ở nơi an toàn. File *.jks đã được .gitignore bảo vệ nhưng vẫn không được upload lên repo."
