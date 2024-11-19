# The nzyme tap

## Running in Jetbrains IDEs

### Configuring the *run* dialog.

The `nzyme-tap` needs permissions to set up listening on interfaces and usually only `root` has such permissions. We do
not want to run as `root`, so we execute `setcap` after the `cargo build` step and before the `cargo run` step using an
*external tool* build step after *build* with the following configuration:


| Configuration     | Value                                                                   |
|-------------------|-------------------------------------------------------------------------|
| Program           | `/usr/bin/sudo`                                                         |
| Arguments         | `/usr/sbin/setcap cap_net_raw,cap_net_admin=eip target/debug/nzyme-tap` |
| Working Directory | `/path/to/your/nzyme/tap`                                               |

The *run command* is the following:

```bash
run -- --configuration-file nzyme-tap.conf --log-level info
```

It is good practice to configure multiple *run* configurations for different configuration files (for example, only
listening on ethernet interfaces and no WiFi interfaces) with different log levels like `debug` and `trace`.

### Setting up `sudo` for passwordless execution

We do not want to enter the `sudo` password each time we execute the tap, so we allow `setcap` to be executed by our
user via `sudo` without a password in the `suoders` file:

```
# Allow setcap
youruser ALL = (root) NOPASSWD: /usr/sbin/setcap
```

**Make sure to place this line at the very end of your `sudoers` file.**

## Compiling `nzyme-tap`

Packages are provided for each release, but you can also easily compile the tap yourself. This can make sense if 
you are running `nzyme-tap` on a CPU architecture or operating system that we do not provide packages for. 

### Considerations


* Custom builds are not covered by nzyme support, but we will do our best to help you in the community support
  channels.
* It is a good idea to `git` check out a stable tag of `nzyme`. This way, you have stable documentation for
  the build you are creating. There could be inconsistencies, undocumented required configuration or missing 
  upgrade notes if you compile from a `git` `SHA` directly. Reach out if you are unsuare what to check out.
* `nzyme-tap` only runs on Linux and should only be built on Linux.
* Our automated build systems for official releases follow the same build procedure.

### Installing Rust

You can install Rust by following the [official installation guide](https://www.rust-lang.org/tools/install).
The `curl` one-liner works very well.


After this step, you should have `rustc` (the Rust compiler) and `cargo` (the Rust package manager) installed.

### Installing required dependencies

You need the `libssl` and `libpcap` development headers to compile the tap.

#### Ubuntu / Debian-based

```
sudo apt install libpcap-dev libssl-dev
```

### Compiling

Now you can compile the tap:

```
cargo install cargo-deb
cd nzyme/tap
cargo build --profile=release
cargo deb
```

This will build the tap in the `release` profile (which takes long to compile, but is extremely optimized) 
and create a Debian package. You may see some warnings that you can ignore.

You will find the compiled binaries and packages in:

* `target/release/nzyme-tap`
* `target/debian/nzyme-tap_*.deb`
