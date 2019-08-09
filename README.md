# Nzyme - A WiFi Defense System

[![CircleCI](https://circleci.com/gh/lennartkoopmann/nzyme/tree/master.svg?style=shield)](https://circleci.com/gh/lennartkoopmann/nzyme/tree/master)
[![Codecov](https://img.shields.io/codecov/c/github/lennartkoopmann/nzyme.svg)](https://codecov.io/gh/lennartkoopmann/nzyme/)
[![License](https://img.shields.io/github/license/lennartkoopmann/nzyme.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)

## Introduction

* TODO:
  * alerting
  * deception
  * threat hunting / long pcap
  * it's not a kismet/wardriving/low-power

## Installation and configuration on a Raspberry Pi 3


#### Requirements

The onboard WiFi chips of recent Raspberry Pi models can be put into monitor mode with the alternative [nexmon](https://github.com/seemoo-lab/nexmon) driver. The problem is, that the onboard antenna is not very good. If possible, use an external adapter that supports monitor mode instead.

Make sure you have Java 7 or 8 installed:

```
$ sudo apt install openjdk-8-jre
$ java -version
openjdk version "1.8.0_40-internal"
OpenJDK Runtime Environment (build 1.8.0_40-internal-b04)
OpenJDK Zero VM (build 25.40-b08, interpreted mode)
```

Also install `libpcap`:

```
$ sudo apt install libpcap0.8
```

#### Download and configure

Download the most recent Debian package (`.DEB`) from the [Releases](https://github.com/lennartkoopmann/nzyme/releases) page.

Install the package:

```
$ sudo dpkg -i [nzyme deb file]
```

Copy the automatically installed config file:

```
$ sudo cp /etc/nzyme/nzyme.conf.example /etc/nzyme/nzyme.conf
```

Change the parameters in the config file to adapt to your WiFi adapters, Graylog GELF input (See *What do I need to run it? -> A Graylog setup* and use-case. The file should be fairly well documented and self-explanatory.

Now enable the `nzyme` service to make it start on boot of the Raspberry Pi:

```
$ sudo systemctl enable nzyme
```

Because we are not rebooting, we have to start the service manually for once:

```
$ sudo systemctl start nzyme
$ sudo systemctl status nzyme
```

![Result of systemctl status](https://github.com/lennartkoopmann/nzyme/blob/master/systemctl-status.png)

That's it! Nzyme should now be logging into your Graylog setup. Logs can be found in `/var/log/nzyme/` and log rotation is enabled by default. You can change logging and log rotation settings in `/etc/nzyme/log4j2-debian.xml`. 

```
$ tail -f /var/log/nzyme/nzyme.log
18:11:43.598 [main] INFO  horse.wtf.nzyme.Main - Printing statistics every 60 seconds. Logs are in [logs/] and will be automatically rotated.                                                                                               
18:11:49.611 [main] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - Building PCAP handle on interface [wlan0]                                                                                                                                                 
18:12:12.908 [main] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - PCAP handle for [wlan0] acquired. Cycling through channels <1,2,3,4,5,6,8,9,10,11,12,13,14>.                                                                                              
18:12:13.009 [nzyme-loop-0] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - Commencing 802.11 frame processing on [wlan0] ... (⌐■_■)–︻╦╤─ – – pew pew                                                                                                        
18:12:14.662 [main] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - Building PCAP handle on interface [wlan1]                                                                                                                                                 
18:12:15.987 [main] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - PCAP handle for [wlan1] acquired. Cycling through channels <36,38,40,44,46,48,52,54,56,60,62,64,100,102,104,108,110,112>.                                                                 
18:12:15.992 [nzyme-loop-1] INFO  horse.wtf.nzyme.probes.dot11.Dot11Probe - Commencing 802.11 frame processing on [wlan1] ... (⌐■_■)–︻╦╤─ – – pew pew                                                                                                        
18:13:05.422 [statistics-0] INFO  horse.wtf.nzyme.Main -                                                                                                                                                                                    
+++++ Statistics: +++++                                                                                                                                                                                                                     
Total frames considered:           597 (92 malformed), beacon: 506, probe-resp: 15, probe-req: 76                                                                                                                                           
Frames per channel:                112: 21, 1: 26, 3: 10, 4: 158, 6: 97, 8: 2, 9: 15, 10: 2, 11: 264, 12: 2                                                                                                                                 
Malformed Frames per channel:      6: 1.03% (1), 8: 50.00% (1), 9: 13.33% (2), 11: 32.95% (87), 12: 50.00% (1),                                                                                                                             
Probing devices:                   5 (last 60s)                                                                                                                                                                                             
Access points:                     26 (last 60s)                                                                                                                                                                                            
Beaconing networks:                17 (last 60s)                                                                                                                                                                                            
18:14:05.404 [statistics-0] INFO  horse.wtf.nzyme.Main -  
```

#### Renaming WiFi interfaces (optional)

The interface names `wlan0`, `wlan1` etc are not always deterministic. Sometimes they can change after a reboot and suddenly nzyme will attempt to use the onboard WiFi chip that does not support monitor mode. To avoid this problem, you can "pin" interface names by MAC address. I like to rename the onboard chip to `wlanBoard` to avoid accidental usage.

**IMPORTANT NOTE:** Starting with Debian/Raspbian Stretch (late 2017), `udev` started to assign predictable network interface names by default. To enable this on Raspbian, you only have to delete the `/etc/systemd/network/99-default.link` symlink and restart your Raspberry Pi. After this, you'll see a predictable naming scheme that includes the MAC address of the device. For example, my previously named `wlan0` is now always `wlxx00c0ca95683b`. **Do this and skip all following steps for renaming network interfaces if you are on Debian/Raspbian Stretch.** (You can find out your version like this: `cat /etc/os-release`)

This is what `ifconfig` looks like with no external WiFi adapters plugged in.

```
pi@parabola:~ $ ifconfig
eth0      Link encap:Ethernet  HWaddr b8:27:eb:0f:0e:d4  
          inet addr:172.16.0.136  Bcast:172.16.0.255  Mask:255.255.255.0
          inet6 addr: fe80::8966:2353:4688:c9a/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:1327 errors:0 dropped:22 overruns:0 frame:0
          TX packets:1118 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:290630 (283.8 KiB)  TX bytes:233228 (227.7 KiB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:304 errors:0 dropped:0 overruns:0 frame:0
          TX packets:304 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:24552 (23.9 KiB)  TX bytes:24552 (23.9 KiB)

wlan0     Link encap:Ethernet  HWaddr b8:27:eb:5a:5b:81  
          inet6 addr: fe80::77be:fb8a:ad75:cca9/64 Scope:Link
          UP BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)
```

In this case `wlan0` is the onboard WiFi chip that we want to rename to `wifiBoard`.

Open the file `/lib/udev/rules.d/75-persistent-net-generator.rules` and add `wlan*` to the device name whitelist:

```
# device name whitelist
KERNEL!="wlan*|ath*|msh*|ra*|sta*|ctc*|lcs*|hsi*", \
                                        GOTO="persistent_net_generator_end"
```

Reboot the system. After it is back up, open `/etc/udev/rules.d/70-persistent-net.rules` and change the `NAME` variable:

```
SUBSYSTEM=="net", ACTION=="add", DRIVERS=="?*", ATTR{address}=="b8:27:eb:5a:5b:81", ATTR{dev_id}=="0x0", ATTR{type}=="1", KERNEL=="wlan*", NAME="wlanBoard"
```

Reboot the system again and enjoy the consistent naming. Any new WiFi adapter you plug in, will be a classic, numbered `wlan0`, `wlan1` etc that can be safely referenced in the nzyme config without the chance of accidentally selecting the onboard chip, because it's called `wlanBoard` now.

```
eth0      Link encap:Ethernet  HWaddr b8:27:eb:0f:0e:d4  
          inet addr:172.16.0.136  Bcast:172.16.0.255  Mask:255.255.255.0
          inet6 addr: fe80::8966:2353:4688:c9a/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:349 errors:0 dropped:8 overruns:0 frame:0
          TX packets:378 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:75761 (73.9 KiB)  TX bytes:69865 (68.2 KiB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:228 errors:0 dropped:0 overruns:0 frame:0
          TX packets:228 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:18624 (18.1 KiB)  TX bytes:18624 (18.1 KiB)

wlanBoard Link encap:Ethernet  HWaddr b8:27:eb:5a:5b:81  
          UP BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)
```

## Legal notice

Make sure to comply with local laws, especially with regards to wiretapping, when running nzyme. Note that nzyme is never decrypting any data but only reading unencrypted data on license-free frequencies.
