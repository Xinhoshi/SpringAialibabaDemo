param(
    [ValidateRange(0, 99)]
    [int]$Lesson = 0,
    [switch]$Package
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Find-Jdk {
    $candidates = @(
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\jdk-21.0.4",
        $env:JAVA_HOME
    )
    foreach ($dir in $candidates) {
        if ($dir -and (Test-Path (Join-Path $dir "bin\java.exe"))) {
            return $dir
        }
    }
    throw "JDK 17+ not found. Install one or edit Find-Jdk in scripts/run.ps1."
}

function Find-Maven {
    $candidates = @(
        "D:\environment\apache-maven-3.8.4\bin\mvn.cmd",
        "D:\environment\apache-maven-3.8.4.2\bin\mvn.cmd",
        "D:\environment\apache-maven-3.6.3\bin\mvn.cmd"
    )
    foreach ($path in $candidates) {
        if (Test-Path $path) { return $path }
    }
    $cmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw "Maven not found. Install Maven 3.8+ first."
}

$env:JAVA_HOME = Find-Jdk
$mvn = Find-Maven

Write-Host "JAVA_HOME = $env:JAVA_HOME"
Write-Host "MAVEN     = $mvn"

if (-not $env:DEEPSEEK_API_KEY) {
    Write-Host "NOTE: DEEPSEEK_API_KEY is not set; real model calls will fail with 401." -ForegroundColor Yellow
    Write-Host '      PowerShell: $env:DEEPSEEK_API_KEY="sk-your-key"' -ForegroundColor Yellow
}

Push-Location $root
try {
    if ($Package) {
        & $mvn -B -DskipTests package
    }
    elseif ($Lesson -gt 0) {
        Write-Host "Running lesson $Lesson ..." -ForegroundColor Cyan
        & $mvn -B spring-boot:run "-Dspring-boot.run.arguments=--demo.lesson=$Lesson"
    }
    else {
        Write-Host "Starting web app (Ctrl+C to stop) ..." -ForegroundColor Cyan
        & $mvn -B spring-boot:run
    }
}
finally {
    Pop-Location
}
