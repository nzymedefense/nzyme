$env:RUST_LOG = 'debug'
$env:RUST_BACKTRACE = '1'

cargo build

if ($LastExitCode -eq 0) {
    .\target\debug\nzyme-tap.exe  --configuration-file nzyme-tap.conf --log-level debug
} else {
    Write-Output "BUILD ERROR"
}