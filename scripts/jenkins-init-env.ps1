# Creates or completes ci/jenkins/.env (git-ignored) with random secrets for the local Jenkins:
#   JENKINS_ADMIN_ID / JENKINS_ADMIN_PASSWORD  - Jenkins sign-in
#   LASTMILE_DB_PASSWORD                       - PostgreSQL password of the deployed stack
#   LASTMILE_DEMO_PASSWORD                     - password of the fictional demo accounts
# Existing values are never changed. Values are written only to that file; read them there.
$envFile = Join-Path $PSScriptRoot "..\ci\jenkins\.env"

function New-Secret {
    $bytes = New-Object byte[] 24
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$existing = @{}
if (Test-Path $envFile) {
    foreach ($line in Get-Content $envFile) {
        if ($line -match '^([A-Z_]+)=') { $existing[$Matches[1]] = $true }
    }
}
$wanted = [ordered]@{
    "JENKINS_ADMIN_ID"       = "admin"
    "JENKINS_ADMIN_PASSWORD" = $null
    "LASTMILE_DB_PASSWORD"   = $null
    "LASTMILE_DEMO_PASSWORD" = $null
}
$added = @()
foreach ($key in $wanted.Keys) {
    if (-not $existing.ContainsKey($key)) {
        $value = if ($wanted[$key]) { $wanted[$key] } else { New-Secret }
        Add-Content -Path $envFile -Value "$key=$value" -Encoding ascii
        $added += $key
    }
}
if ($added.Count -gt 0) {
    Write-Host ("Added to ci/jenkins/.env: " + ($added -join ", "))
} else {
    Write-Host "ci/jenkins/.env already complete; nothing changed."
}
