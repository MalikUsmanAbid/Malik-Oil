param([int]$Port=8765)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$previewScript=Join-Path $PSScriptRoot 'preview/server.py'
$privateDirectory=Join-Path $projectRoot '.preview'
New-Item -ItemType Directory -Force -Path $privateDirectory | Out-Null
$settings=Join-Path $privateDirectory 'research.local.json'
$previousKey=$env:GEMINI_API_KEY
$previousEnabled=$env:ENABLE_RESEARCH
$previousTavily=$env:TAVILY_API_KEY
$previousTavilyConfirmed=$env:TAVILY_FREE_TIER_CONFIRMED
try {
 $encryptedKey=Join-Path $privateDirectory 'gemini.credential.xml'
 if(Test-Path -LiteralPath $encryptedKey){
  $savedCredential=Import-Clixml -LiteralPath $encryptedKey
  $env:GEMINI_API_KEY=$savedCredential.GetNetworkCredential().Password.Trim()
  if(-not $env:GEMINI_API_KEY){throw 'Encrypted key is empty; run tools/setup-research-key.ps1 again.'}
  $env:ENABLE_RESEARCH='true'
 }elseif(Test-Path -LiteralPath $settings){
  $config=Get-Content -LiteralPath $settings -Raw | ConvertFrom-Json
  if(-not $config.apiKey -or $config.freeTierConfirmed -ne $true){throw 'Set apiKey and freeTierConfirmed=true in .preview/research.local.json after checking the project has no billing.'}
  $env:GEMINI_API_KEY=[string]$config.apiKey
  $env:ENABLE_RESEARCH='true'
 }
 $tavilyFile=Join-Path $privateDirectory 'tavily.credential.xml'
 if(Test-Path -LiteralPath $tavilyFile){
  $tavilyCredential=Import-Clixml -LiteralPath $tavilyFile
  $env:TAVILY_API_KEY=$tavilyCredential.GetNetworkCredential().Password.Trim()
  $env:TAVILY_FREE_TIER_CONFIRMED='true'
 }
 $pidFile=Join-Path $privateDirectory 'server.pid'
 if(Test-Path -LiteralPath $pidFile){
  $previousId=Get-Content -LiteralPath $pidFile
  if($previousId -match '^\d+$'){
   $previousProcess=Get-CimInstance Win32_Process -Filter "ProcessId=$previousId"
   if($previousProcess -and $previousProcess.CommandLine.Contains($previewScript)){Stop-Process -Id ([int]$previousId)}
  }
 }
 $python=(Get-Command python).Source
 $process=Start-Process -FilePath $python -ArgumentList @(('"'+$previewScript+'"'),'--port',"$Port") -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $privateDirectory 'stdout.log') -RedirectStandardError (Join-Path $privateDirectory 'stderr.log') -PassThru
 $process.Id | Set-Content -LiteralPath $pidFile
 Write-Host "Local preview: http://127.0.0.1:$Port/"
} finally {$env:GEMINI_API_KEY=$previousKey;$env:ENABLE_RESEARCH=$previousEnabled;$env:TAVILY_API_KEY=$previousTavily;$env:TAVILY_FREE_TIER_CONFIRMED=$previousTavilyConfirmed}
