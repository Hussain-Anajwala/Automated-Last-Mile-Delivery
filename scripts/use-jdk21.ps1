# Points the CURRENT PowerShell session at JDK 21 for this project. Global settings are not changed.
# Usage (from the repository root):   . .\scripts\use-jdk21.ps1   then   .\mvnw.cmd verify
param([string]$JdkHome = "D:\tools\jdk-21")

if (-not (Test-Path (Join-Path $JdkHome "bin\java.exe"))) {
    Write-Error "JDK 21 not found at $JdkHome. See docs/SETUP.md."
    return
}
$env:JAVA_HOME = $JdkHome
$env:Path = (Join-Path $JdkHome "bin") + ";" + $env:Path
Write-Host "JAVA_HOME set to $JdkHome for this session."
