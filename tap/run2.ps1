$env:RUST_LOG = 'info'
$env:RUST_BACKTRACE = '1'

cargo build

if ($LastExitCode -eq 0) {
    .\target\debug\nzyme-tap.exe --configuration-file nzyme-tap2.conf --log-level info
} else {
    Write-Output "BUILD ERROR"
}