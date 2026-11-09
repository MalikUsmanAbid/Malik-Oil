$ErrorActionPreference='Stop'
$origin='http://127.0.0.1:8765'
$ready=$false
for($attempt=0;$attempt -lt 10;$attempt++){
 try{$state=Invoke-RestMethod -Uri "$origin/demo/state" -TimeoutSec 3;$ready=$true;break}catch{Start-Sleep -Milliseconds 500}
}
if(-not $ready){throw 'Local preview did not become ready.'}
if($state.webSearchConfigured -ne $true){throw 'Tavily key is not loaded. Save it using Setup-Tavily.cmd (FREE confirmation, hidden key prompt, Enter). No research attempt was used.'}
$body=@{question='Samundri sarson ka aaj ka rate kya hai? Dated Samundri source na mile to Faisalabad ka dated rate, unit aur asal source alag batayein.';market='Samundri';commodity='sarson';language='roman';depth='deep';context=@()} | ConvertTo-Json
$result=Invoke-RestMethod -Uri "$origin/demo/research" -Method Post -Headers @{Origin=$origin} -ContentType 'application/json' -Body $body -TimeoutSec 65
if($result.searchCoverage -ne 'web-search-excerpts'){throw 'Tavily is not active. Run setup-tavily-key.ps1 and restart preview.'}
Write-Host ('Live check: '+$result.mode+'; sources='+@($result.sources).Count+'; source-supported findings='+@($result.supports).Count)
if($result.mode -ne 'free-web-research' -or @($result.sources).Count -eq 0){Write-Warning 'Live research did not produce a verified AI answer. Check quota/provider availability; no paid fallback was used.';exit 1}
Write-Host 'Live Samundri Tavily + Gemini research passed. This is not automatic verification of a local mandi rate.'
Write-Host $result.text
foreach($source in $result.sources){Write-Host ('['+$source.id+'] '+$source.title+' — '+$source.url)}
Write-Host 'Refresh the Research tab to ask your own question.'
