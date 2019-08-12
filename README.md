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

## Installation and configuration on a Raspberry Pi


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

## Legal notice

Make sure to comply with local laws, especially with regards to wiretapping, when running nzyme. Note that nzyme is never decrypting any data but only reading unencrypted data on license-free frequencies.
