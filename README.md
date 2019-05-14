# Nzyme

[![CircleCI](https://circleci.com/gh/lennartkoopmann/nzyme/tree/master.svg?style=shield)](https://circleci.com/gh/lennartkoopmann/nzyme/tree/master)
[![Codecov](https://img.shields.io/codecov/c/github/lennartkoopmann/nzyme.svg)](https://codecov.io/gh/lennartkoopmann/nzyme/)
[![License](https://img.shields.io/github/license/lennartkoopmann/nzyme.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)

## Introduction

Nzyme collects 802.11 management frames directly from the air and sends them to a [Graylog](https://www.graylog.org/) (Open Source log management) setup for WiFi IDS, monitoring, and incident response. It only needs a JVM and a WiFi adapter that supports monitor mode.

Think about this like a long-term (months or years) distributed Wireshark/tcpdump that can be analyzed and filtered in real-time, using a powerful UI.

If you are new to the fascinating space of WiFi security, you might want to read my *[Common WiFi Attacks And How To Detect Them](https://wtf.horse/2017/09/19/common-wifi-attacks-explained/)* blog post.

**A longer blog post with nzyme examples and use-cases is published on my blog:** [Introducing Nzyme: WiFi Monitoring, Intrusion Detection And Forensics](https://wtf.horse/2017/10/02/introducing-nzyme-wifi-802-11-frame-recording-and-forensics/)

![Picture of a nzyme sensor](https://github.com/lennartkoopmann/nzyme/blob/master/sensor.jpg)

## What kind of data does it collect?

Nzyme collects, parses and forwards all relevant 802.11 management frames. Management frames are unencrypted so anyone close enough to a sending station (an access point, a computer, a phone, a lightbulb, a car, a juice maker, ...) can pick them up with nzyme.

* Association request
* Association response
* Probe request
* Probe response
* Beacon
* Disassociation
* Authentication
* Deauthentication

## What do I need to run it? 

Everything you need is available from Amazon Prime and is not very expensive. There even is a good chance you have the parts around already.

### One or more WiFi adapters that support monitor mode on your operating system.

The most important component is one (or more) WiFi adapters that support monitor mode. Monitor mode is the special state of a WiFi adapter that makes it read and report all 802.11 frames and not only certain management frames or frames of a network it is connected to. You could also call this mode *sniffing mode*: The adapter just spits out everything it sees on the channel it is tuned to.

The problem is, that many adapter/driver/operating system combinations do not support monitor mode.

The internet is full of compatibility information but here are the adapters I run nzyme with on a Raspberry Pi 3 Model B:

* ALFA AWUS036NH - 2.4Ghz and 5Ghz (Amazon Prime, about $40)
* ALFA AWUS036NEH - 2.4Ghz (Amazon Prime, about $50)
* ALFA AWUS036ACH - 2.4Ghz and 5Ghz (Amazon Prime, about $50) (with the [aircrack-ng/rtl8812au](https://github.com/aircrack-ng/rtl8812au) driver)
* Panda PAU05 - 2.4Ghz (Amazon Prime, about $15)

If you have another one that supports monitor mode, you can use that one. Nzyme does by far not require any specific hardware.

### A small computer to run nzyme on.

I recommend to run nzyme on a Raspberry Pi 3 Model B. This is pretty much the reference architecture, because that is what I run it on. A Raspberry Pi 3 Model B running Nzyme with three WiFi adapters in monitor mode has about 25% CPU utilization in the busy frequencies of Downtown Houston, TX.

In the end, it shouldn’t really matter what you run it on, but the docs and guides will most likely refer to a Raspberry Pi with a Raspbian on it.

### A Graylog setup

You need a Graylog setup with ah GELF TCP input that is reachable by your nzyme sensors. GELF is a Graylog-specific and structured log format. Because nzyme sends GELF, you don't have to set up any kind of parsing rules in Graylog and still have all fields available as key:value pairs for powerful search and analysis.

You can start a GELF input for nzyme using your Graylog Web Interface. Navigate to *System* -> *Inputs*, select *GELF TCP* in the dropdown menu and hit *Launch new input*. A modal dialog will open and ask you a few questions about, for example, which address to bind on and what port to use. The input will be immediately available for nzyme after pressing *Save*.

![How to start a Graylog input](https://github.com/lennartkoopmann/nzyme/blob/master/launch_input.png)

## Channel hopping

The 802.11 standard [defines](https://en.wikipedia.org/wiki/List_of_WLAN_channels) many frequencies (channels) a network can operate on. This is useful to avoid contention and bandwidth issues, but also means that your wireless adapter has to be tuned to a single channel. During normal operations, your operating system will do this automatically for you.

Because we don’t want to listen on only one, but possibly **all** WiFi channels, we either need dozens of adapters, with one adapter for each channel, or we cycle over multiple channels on a single adapter rapidly. Nzyme allows you to configure multiple channels per WiFi adapter.

For example, if you configure nzyme to listen on channel 1,2,3,4,5,6 on `wlan0` and 7,8,9,10,11 on `wlan1`, it will tune `wlan0` to channel 1 for a configurable time (default is 1 second) and then switch to channel 2, then to channel 3 and so on. By doing this, we might miss a bunch of wireless frames but are not missing out on some channels completely.

The best configuration depends on your use-case but usually you will want to tune to all 2.4 Ghz and 5 Ghz WiFi channels.

On Linux, you can get a list of channels your WiFi adapter supports like this:

```
$ iwlist wlan0 channel
wlan0     32 channels in total; available frequencies :
          Channel 01 : 2.412 GHz
          Channel 02 : 2.417 GHz
          Channel 03 : 2.422 GHz
          Channel 04 : 2.427 GHz
          Channel 05 : 2.432 GHz
          Channel 06 : 2.437 GHz
          Channel 07 : 2.442 GHz
          Channel 08 : 2.447 GHz
          Channel 09 : 2.452 GHz
          Channel 10 : 2.457 GHz
          Channel 11 : 2.462 GHz
          Channel 12 : 2.467 GHz
          Channel 13 : 2.472 GHz
          Channel 14 : 2.484 GHz
          Channel 36 : 5.18 GHz
          Channel 38 : 5.19 GHz
          Channel 40 : 5.2 GHz
          Channel 44 : 5.22 GHz
          Channel 46 : 5.23 GHz
          Channel 48 : 5.24 GHz
          Channel 52 : 5.26 GHz
          Channel 54 : 5.27 GHz
          Channel 56 : 5.28 GHz
          Channel 60 : 5.3 GHz
          Channel 62 : 5.31 GHz
          Channel 64 : 5.32 GHz
          Channel 100 : 5.5 GHz
          Channel 102 : 5.51 GHz
          Channel 104 : 5.52 GHz
          Channel 108 : 5.54 GHz
          Channel 110 : 5.55 GHz
          Channel 112 : 5.56 GHz
          Current Frequency:2.432 GHz (Channel 5)
```

## Things to keep in mind

A few general things to know before you get started:

* Success will highly depend on how well supported your WiFi adapters and drivers are. Use the recommended adapters for best results. You can get them from Amazon Prime and have them ready in one or two days.
* At least on OSX, your adapter will not switch channels when already connected to a network. Make sure to disconnect from networks before using nzyme with the on-board WiFi adapter. On other systems, switching to monitor mode should disconnect the adapter from a possibly connected network.
* Nzyme works well with both the OpenJDK or the Oracle JDK and requires Java 7 or 8. 
* Wifi adapters can draw quite some current and I have seen Raspberry Pi 3’s shut down when connecting more than 3 ALFA adapters. Consider this before buying tons of adapters.

## Testing on a MacBook

(You can skip this and go straight to a real installation on a Raspberry Pi or install it on any other device that runs Java and has supported WiFi adapters connected to it.)

#### Requirements

Nzyme is able to put the onboard WiFi adapter of recent MacBooks into monitor mode so you don’t need an external adapter for testing. Remember that you cannot be connected to a wireless network while running nzyme, so the Graylog setup you send data to has to be local or you need a wired network connection or a second WiFi adapter as LAN/WAN uplink.

Make sure you have Java 7 or 8 installed:

```
$ java -version
java version "1.8.0_121"
Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)
```

#### Download and configure

Download the most recent build from the [Releases](https://github.com/lennartkoopmann/nzyme/releases) page.

Create a new file called `nzyme.conf` in the same folder as your `nzyme.jar` file:

```
nzyme_id = nzyme-macbook-1
channels = en0:1,2,3,4,5,6,8,9,10,11
channel_hop_command = sudo /System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport {interface} channel {channel}
channel_hop_interval = 1
graylog_addresses = graylog.example.org:12000
beacon_frame_sampling_rate = 0
```

Note  the `graylog_addresses` variable that has to point to a GELF TCP input in your Graylog setup. Adapt it accordingly.

Please refer to the [example config in the repository](https://github.com/lennartkoopmann/nzyme/blob/master/nzyme.conf.example) for a more verbose version with comments.
 
#### Run

After disconnecting from all WiFi networks (you might have to "forget" them in the macOS WiFi settings), you can start nzyme like this:

```
$ java -jar nzyme-0.1.jar -c nzyme.conf
18:35:00.261 [main] INFO  horse.wtf.nzyme.Main - Printing statistics every 60 seconds. Logs are in [logs/] and will be automatically rotated.                                                                                                 
18:35:00.307 [main] WARN  horse.wtf.nzyme.Nzyme - No Graylog uplinks configured. Falling back to Log4j output                                                                                                                                 
18:35:00.459 [main] INFO  horse.wtf.nzyme.Nzyme - Building PCAP handle on interface [en0]                                                                                                                                                     
18:35:00.474 [main] INFO  horse.wtf.nzyme.Nzyme - PCAP handle for [en0] acquired. Cycling through channels <1,2,3,4,5,6,8,9,10,11>.                                                                                                           
18:35:00.483 [nzyme-loop-0] INFO  horse.wtf.nzyme.Nzyme - Commencing 802.11 frame processing on [en0] ... (⌐■_■)–︻╦╤─ – – pew pew 
```

Nzyme is now collecting data and writing it into the Graylog input you configured. A message will look like this:

![Example message in Graylog](https://github.com/lennartkoopmann/nzyme/blob/master/example_message.png)

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

Alterntively, you can use the templatized systemd service unit. This method handles setting a interface into monitor mode when started, managed mode when stopped, and uses a per-device configuration file. Configuration files must ffollow this naming convention ```/etc/nzyme/nzyme-NIC.conf``` where NIC is replaced with the name of your wireless device (not phy0, but something like wlan0). NOTE: currently nzyme does not support logging to unique filenames. The contents of the log file, when using the service templates, may be corrupt dependent on a variety of circumstances.

```
$ sudo systemctl enable nzyme@wlan0
```


Because we are not rebooting, we have to start the service manually for once:

```
$ sudo systemctl start nzyme
$ sudo systemctl status nzyme
```

Or, if you chose to use the systemd service template, this starts the service, including putting the interface into monitor mode, and checks the status.

```
$ sudo systemctl start nzyme@wlan0
$ sudo systemctl status nzyme@wlan0
```

![Result of systemctl status](https://github.com/lennartkoopmann/nzyme/blob/master/systemctl-status.png)

That's it! Nzyme should now be logging into your Graylog setup. Logs can be found in `/var/log/nzyme/` and log rotation is enabled by default. You can change logging and log rotation settings in `/etc/nzyme/log4j2-debian.xml`. 

```
$ tail -f /var/log/nzyme/nzyme.log
18:11:43.598 [main] INFO  horse.wtf.nzyme.Main - Printing statistics every 60 seconds. Logs are in [logs/] and will be automatically rotated.                                                                                               
18:11:49.611 [main] INFO  horse.wtf.nzyme.Nzyme - Building PCAP handle on interface [wlan0]                                                                                                                                                 
18:12:12.908 [main] INFO  horse.wtf.nzyme.Nzyme - PCAP handle for [wlan0] acquired. Cycling through channels <1,2,3,4,5,6,8,9,10,11,12,13,14>.                                                                                              
18:12:13.009 [nzyme-loop-0] INFO  horse.wtf.nzyme.Nzyme - Commencing 802.11 frame processing on [wlan0] ... (⌐■_■)–︻╦╤─ – – pew pew                                                                                                        
18:12:14.662 [main] INFO  horse.wtf.nzyme.Nzyme - Building PCAP handle on interface [wlan1]                                                                                                                                                 
18:12:15.987 [main] INFO  horse.wtf.nzyme.Nzyme - PCAP handle for [wlan1] acquired. Cycling through channels <36,38,40,44,46,48,52,54,56,60,62,64,100,102,104,108,110,112>.                                                                 
18:12:15.992 [nzyme-loop-1] INFO  horse.wtf.nzyme.Nzyme - Commencing 802.11 frame processing on [wlan1] ... (⌐■_■)–︻╦╤─ – – pew pew                                                                                                        
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

## Known issues

* Some WiFi adapters will not report the MAC timestamp in the radiotap header. The field will simply be missing in Graylog. This is usually an issue with the driver.
* Some Linux distributions will try to manage the network adapters for you and interfere with nzyme. For example, on Ubuntu, you have to disable `NetworkManager`. There is plenty of documentation for this available and I will not duplicate it. **I also did not encounter this on any Raspbian based Raspberry Pi yet.** The `airmon-ng` project has a built in way to find and kill processes that might interfere:

```
~# airmon-ng check
Found 5 processes that could cause trouble.
If airodump-ng, aireplay-ng or airtun-ng stops working after
a short period of time, you may want to kill (some of) them!

  PID Name
  718 NetworkManager
  870 dhclient
 1104 avahi-daemon
 1105 avahi-daemon
 1115 wpa_supplicant
```

If you are running or developing nzyme on a Ubuntu machine, you can exclude your WiFi adapters from management by `NetworkManager` by configuring this in `/etc/NetworkManager/NetworkManager.conf`:

```
[keyfile]
unmanaged-devices=mac:00:c0:ca:97:12:0e;mac:00:c0:ca:97:12:16;mac:00:c0:ca:97:12:01
```

Remember to restart `NetworkManager` after the change:

```
sudo service network-manager restart
```

* Running without root rights is possible on many Linux distributions by adding special capabilities to the `java` executable: (Make sure to use the correct path to your specific `java`)

```
$ sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
$ getcap /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java = cap_net_admin,cap_net_raw+eip
```

## Protips

### Use Graylog lookup tables

A simple CSV [lookup table](http://docs.graylog.org/en/latest/pages/lookuptables.html) for Graylog can translate BSSIDs/MAC addresses to real device names for easier browsing and quicker analysis.

```
$ cat /etc/graylog/station-mac-addresses.csv 
"mac","station"
"82:2A:A8:07:4C:8D", "Home Main"
"2C:30:33:A5:8D:94", "Home Extender"
```

A message with translated fields could look like this:

![Enriched message](https://github.com/lennartkoopmann/nzyme/blob/master/lookup_tables.png)

### CLI parameters

Nzyme has a few CLI parameters, some of which can be helpful for debugging.

* `--config-file`, `-c`
  * Path to config file. This is the only required parameter.
* `--debug`, `-d`
  * Override Log4j configuration and start with log level `DEBUG`.
* `--trace`, `-t`
  * Override Log4j configuration and start with log level `TRACE`.
* `--packet-info`, `-p`
  * Print simple packet size information for every frame that is received.

As an example for CLI parameter usage, here is how to start nzyme in debug mode with packet information printing:

```
java -jar nzyme.jar --debug --packet-info 
```

### Version Checks
By default, nzyme will check if there is a more recent stable release available by requesting information about the latest
release from 

## Legal notice

Make sure to comply with local laws, especially with regards to wiretapping, when running nzyme. Note that nzyme is never decrypting any data but only reading unencrypted data on license-free frequencies.
