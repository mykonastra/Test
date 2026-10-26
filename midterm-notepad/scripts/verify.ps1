param(
    [string]$SdkPath = '',
    [string]$DeviceSerial = '',
    [switch]$Offline,
    [switch]$Rebuild
)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
Push-Location $repoRoot
try {
    if (-not $SdkPath) { $SdkPath = $env:ANDROID_HOME }
    if (-not $SdkPath) { $SdkPath = $env:ANDROID_SDK_ROOT }
    if (-not $SdkPath -and (Test-Path 'local.properties')) {
        $sdkLine = Get-Content 'local.properties' | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) { $SdkPath = $sdkLine.Substring(8).Replace('\:', ':').Replace('\\', '\').Replace('\ ', ' ') }
    }
    $adb = Join-Path $SdkPath 'platform-tools/adb.exe'
    if (-not (Test-Path -LiteralPath $adb)) { throw 'Set -SdkPath or configure ANDROID_HOME/local.properties.' }
    $gradleArgs = @(':midterm-notepad:assembleDebug', ':midterm-notepad:assembleDebugAndroidTest', ':midterm-notepad:lintDebug', '--no-configuration-cache', '--console=plain')
    if ($Offline) { $gradleArgs += '--offline' }
    if ($Rebuild) { $gradleArgs += '--rerun-tasks' }
    $evidence = Join-Path $repoRoot 'build/notepad-verification'
    New-Item -ItemType Directory -Force $evidence | Out-Null
    # Windows PowerShell 5 treats native stderr (including successful javac notes) as errors.
    # Keep those notes in the log, then judge Gradle by its process exit code.
    $ErrorActionPreference = 'Continue'
    try {
        & .\gradlew.bat @gradleArgs 2>&1 | ForEach-Object { $_.ToString() } | Tee-Object (Join-Path $evidence 'build-and-lint.txt')
        $gradleExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = 'Stop' }
    if ($gradleExit -ne 0) { throw 'Build or lint failed.' }
    if (-not $DeviceSerial) {
        $devices = @(& $adb devices | Where-Object { $_ -match '^\S+\s+device$' } | ForEach-Object { ($_ -split '\s+')[0] })
        if ($devices.Count -ne 1) { throw 'Connect one dedicated emulator, or pass -DeviceSerial.' }
        $DeviceSerial = $devices[0]
    }
    foreach ($apk in @('midterm-notepad/build/outputs/apk/debug/midterm-notepad-debug.apk', 'midterm-notepad/build/outputs/apk/androidTest/debug/midterm-notepad-debug-androidTest.apk')) {
        & $adb -s $DeviceSerial install -r $apk
        if ($LASTEXITCODE -ne 0) { throw "Install failed: $apk" }
    }
    $result = & $adb -s $DeviceSerial shell am instrument -w com.example.test.notepad.test/com.example.android.notepad.NotePadTestRunner
    $result | Tee-Object (Join-Path $evidence 'device-tests.txt')
    if ($LASTEXITCODE -ne 0 -or ($result -join "`n") -notmatch 'RESULT: 20 passed, 0 failed') { throw 'Device integration tests did not all pass.' }
    $shots = Join-Path $evidence 'screenshots'
    New-Item -ItemType Directory -Force $shots | Out-Null
    & $adb -s $DeviceSerial pull /sdcard/Android/data/com.example.test.notepad/files/verification/. $shots
    if ($LASTEXITCODE -ne 0) { throw 'Screenshot retrieval failed.' }
    Write-Output "Verified: 20 device tests passed; build and lint passed. Evidence: $evidence"
} finally { Pop-Location }
