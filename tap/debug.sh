cargo build
sudo setcap cap_net_raw,cap_net_admin=eip target/debug/nzyme-tap
RUST_LOG=debug RUST_BACKTRACE=1 target/debug/nzyme-tap
