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

**Make sure to place this line at the very end of your `sudoers` file**