param([switch]$SelfTest)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$privateDirectory=Join-Path $projectRoot '.preview'
$credentialFile=Join-Path $privateDirectory 'gemini.credential.xml'
function Save-PrivateKey([Security.SecureString]$Value,[string]$Path){
 $credential=New-Object System.Management.Automation.PSCredential('MalikOilGemini',$Value)
 $credential | Export-Clixml -LiteralPath $Path -Encoding UTF8
}
if($SelfTest){
 $testFile=Join-Path ([IO.Path]::GetTempPath()) ([guid]::NewGuid().ToString()+'.xml')
 try{
  $fixture='isolated-test-only-not-an-api-key'
  Save-PrivateKey (ConvertTo-SecureString $fixture -AsPlainText -Force) $testFile
  $read=Import-Clixml -LiteralPath $testFile
  if($read.GetNetworkCredential().Password -ne $fixture){throw 'Credential round-trip failed'}
  if((Get-Content -LiteralPath $testFile -Raw).Contains($fixture)){throw 'Plaintext found in encrypted storage'}
  Write-Host 'Windows encrypted credential round-trip PASS; no plaintext stored.'
 }finally{Remove-Item -LiteralPath $testFile -ErrorAction SilentlyContinue}
 exit
}
New-Item -ItemType Directory -Force -Path $privateDirectory | Out-Null
Write-Host 'Malik Oil: private Gemini API key setup'
Write-Host 'Use your FREE/unbilled Google AI Studio project. No billing is enabled here.'
Write-Host 'Paste the API key below. Input is hidden; it is not saved in command history.'
$secret=Read-Host 'Gemini API key' -AsSecureString
$credential=New-Object System.Management.Automation.PSCredential('MalikOilGemini',$secret)
$plain=$credential.GetNetworkCredential().Password.Trim()
if($plain.Length -lt 20 -or $plain -match '\s'){throw 'Key is empty or contains whitespace. Nothing saved.'}
$confirmed=Read-Host 'Project is Free/unbilled? Type YES'
if($confirmed -ne 'YES'){throw 'Cancelled. Nothing saved.'}
Save-PrivateKey (ConvertTo-SecureString $plain -AsPlainText -Force) $credentialFile
$plain=$null;$secret=$null;$credential=$null
Write-Host 'Saved encrypted for this Windows user. Restarting local preview...'
& (Join-Path $PSScriptRoot 'start-preview.ps1')
$base='http://127.0.0.1:8765'
for($attempt=0;$attempt -lt 20;$attempt++){
 try{$state=Invoke-RestMethod "$base/demo/state" -TimeoutSec 2;if($state.researchConfigured){break}}catch{}
 Start-Sleep -Milliseconds 250
}
$report=@{checkedAt=[DateTime]::UtcNow.ToString('o');configured=[bool]$state.researchConfigured;liveTestPassed=$false}
try{
 $body=@{language='roman';market='Samundri';commodity='sarson'} | ConvertTo-Json
 $result=Invoke-RestMethod "$base/demo/research" -Method Post -ContentType 'application/json' -Headers @{Origin=$base} -Body $body -TimeoutSec 65
 $report.mode=$result.mode;$report.sourceCount=@($result.sources).Count
 $report.liveTestPassed=$result.mode -in @('grounded-api','free-web-research')
 Write-Host ('Research mode: '+$result.mode+' | sources: '+$report.sourceCount)
}catch{
 $report.error='Live research failed; inspect sanitized local research endpoint status.'
 Write-Host $report.error
}finally{
 $report | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $privateDirectory 'key-setup-result.json') -Encoding UTF8
}
Write-Host 'Key never printed. You can return to Codex; the test report is saved locally.'
