param([string]$OutputDirectory = "", [switch]$Lint, [switch]$VerboseBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $projectRoot 'android'
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
 $jdkRoot = Join-Path $env:LOCALAPPDATA 'MalikOil/toolchains'
 $javaCandidate = Get-ChildItem -LiteralPath $jdkRoot -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin/java.exe') } | Sort-Object Name -Descending | Select-Object -First 1
 if (-not $javaCandidate) { throw 'Install JDK 17 or newer and set JAVA_HOME.' }
 $env:JAVA_HOME = $javaCandidate.FullName
}
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
if (-not (Test-Path (Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar'))) { throw 'Install Android SDK Platform 35 first.' }
$env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH
('sdk.dir=' + $env:ANDROID_HOME.Replace('\','/') + "`n") | Set-Content -LiteralPath (Join-Path $androidRoot 'local.properties') -Encoding ascii
Push-Location $androidRoot
try {
 $buildTasks = @(':app:assembleDebug')
 if ($Lint) { $buildTasks += ':app:lintDebug' }
 $buildOptions = @('--console=plain', '--max-workers=2')
 if ($VerboseBuild) { $buildOptions += '--info' }
 & .\gradlew.bat @buildTasks @buildOptions
 if ($LASTEXITCODE -ne 0) { throw "Android build failed with exit code $LASTEXITCODE" }
 $apk = Join-Path $androidRoot 'app/build/outputs/apk/debug/app-debug.apk'
 if (-not $OutputDirectory) { $OutputDirectory = Join-Path $projectRoot 'artifacts' }
 New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
 $destination = Join-Path $OutputDirectory 'Malik-Oil-0.1.0-debug.apk'
 Copy-Item -LiteralPath $apk -Destination $destination -Force
 Get-FileHash -LiteralPath $destination -Algorithm SHA256
 Write-Host "APK saved: $destination"
} finally { Pop-Location }
