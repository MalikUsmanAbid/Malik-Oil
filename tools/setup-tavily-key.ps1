param([switch]$SelfTest)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$privateDirectory=Join-Path $projectRoot '.preview'
New-Item -ItemType Directory -Force -Path $privateDirectory | Out-Null
if($SelfTest){
 $testPath=Join-Path $privateDirectory ('tavily-test-'+[guid]::NewGuid()+'.xml')
 try{
  $secure=ConvertTo-SecureString 'fixture-not-a-real-key' -AsPlainText -Force
  $credential=New-Object System.Management.Automation.PSCredential('MalikOilTavily',$secure)
  $credential | Export-Clixml -LiteralPath $testPath -Encoding UTF8
  if((Import-Clixml -LiteralPath $testPath).GetNetworkCredential().Password -ne 'fixture-not-a-real-key'){throw 'Credential verification failed'}
  Write-Host 'Encrypted credential self-test passed.'
 }finally{if(Test-Path -LiteralPath $testPath){Remove-Item -LiteralPath $testPath}}
 exit
}
Write-Host 'Use the free Researcher plan. Do not add a card or enable pay-as-you-go.'
$confirmation=Read-Host 'Type FREE after checking pay-as-you-go is OFF'
if($confirmation -cne 'FREE'){throw 'Free-tier confirmation required; nothing saved.'}
$secure=Read-Host 'Paste Tavily API key (hidden)' -AsSecureString
$credential=New-Object System.Management.Automation.PSCredential('MalikOilTavily',$secure)
if($credential.GetNetworkCredential().Password.Trim() -notmatch '^tvly-[A-Za-z0-9_-]{12,}$'){throw 'Tavily key format invalid; nothing saved.'}
$credential | Export-Clixml -LiteralPath (Join-Path $privateDirectory 'tavily.credential.xml') -Encoding UTF8
Write-Host 'Key saved encrypted for this Windows account; never included in APK or source archive.'
& (Join-Path $PSScriptRoot 'start-preview.ps1')

& (Join-Path $PSScriptRoot "test-research.ps1")
