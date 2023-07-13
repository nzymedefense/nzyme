cargo build

if [ $? -ne 0 ]; then
    echo "Build failed."
    exit 0
fi

sudo setcap cap_net_raw,cap_net_admin=eip target/debug/nzyme-tap

if [ $? -ne 0 ]; then
    echo "Setcap failed."
    exit 0
fi

RUST_BACKTRACE=1 target/debug/nzyme-tap --configuration-file nzyme-tap.conf --log-level trace

