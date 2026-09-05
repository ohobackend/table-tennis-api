$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
$testExitCode = 1
try {
    docker compose -p tt-api-tests -f docker-compose.test.yml run --rm tests
    $testExitCode = $LASTEXITCODE
} finally {
    docker compose -p tt-api-tests -f docker-compose.test.yml down
    Pop-Location
}
exit $testExitCode
