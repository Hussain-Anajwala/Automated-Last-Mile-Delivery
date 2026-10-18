# Creates ci/jenkins/.env (git-ignored) with a random Jenkins admin password, if it does not exist.
# The password is written only to that file; read it there when you need to sign in.
$envFile = Join-Path $PSScriptRoot "..\ci\jenkins\.env"
if (Test-Path $envFile) {
    Write-Host "ci/jenkins/.env already exists; leaving it unchanged."
    return
}
$bytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$password = [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
@(
    "JENKINS_ADMIN_ID=admin",
    "JENKINS_ADMIN_PASSWORD=$password"
) | Set-Content -Path $envFile -Encoding ascii
Write-Host "Created ci/jenkins/.env (admin user 'admin'; password stored in that file only)."
