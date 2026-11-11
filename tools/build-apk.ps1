param([string]$OutputDirectory="",[switch]$Lint)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
if(-not $env:JAVA_HOME){
 $candidate=Get-ChildItem -LiteralPath (Join-Path $env:LOCALAPPDATA 'MalikOil/toolchains') -Directory | Where-Object {Test-Path (Join-Path $_.FullName 'bin/java.exe')} | Sort-Object Name -Descending | Select-Object -First 1
 if(-not $candidate){throw 'Set JAVA_HOME to JDK 17 or newer.'}
 $env:JAVA_HOME=$candidate.FullName
}
if(-not $env:ANDROID_HOME){$env:ANDROID_HOME=Join-Path $env:LOCALAPPDATA 'Android/Sdk'}
('sdk.dir='+$env:ANDROID_HOME.Replace('\','/')) | Set-Content -LiteralPath (Join-Path $projectRoot 'local.properties') -Encoding ascii
Push-Location $projectRoot
try {
 $tasks=@(':app:testDebugUnitTest',':app:assembleDebug')
 if($Lint){$tasks+=':app:lintDebug'}
 & .\gradlew.bat @tasks --console=plain --max-workers=1
 if($LASTEXITCODE -ne 0){throw "Android checks failed ($LASTEXITCODE)"}
 if(-not $OutputDirectory){$OutputDirectory=Join-Path $projectRoot 'artifacts'}
 New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
 $target=Join-Path $OutputDirectory 'Malik-Oil-1.0.0-debug.apk'
 Copy-Item -LiteralPath 'app/build/outputs/apk/debug/app-debug.apk' -Destination $target -Force
 Get-FileHash -LiteralPath $target -Algorithm SHA256
}finally{Pop-Location}
