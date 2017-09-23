# Nzyme

[![Build Status](https://travis-ci.org/lennartkoopmann/nzyme.svg?branch=master)](https://travis-ci.org/lennartkoopmann/nzyme)
[![Codecov](https://img.shields.io/codecov/c/github/lennartkoopmann/nzyme.svg)](https://codecov.io/gh/lennartkoopmann/nzyme/)
[![License](https://img.shields.io/github/license/lennartkoopmann/nzyme.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)

## Introduction

Nzyme collects 802.11 management frames directly from the air and sends them to a [Graylog] (Open Source log management) setup for WiFi IDS, monitoring, and incident response. It only needs a JVM and a WiFi adapter that supports monitor mode.

If you are new to the fascinating space of WiFi security, you might want to read my *[Common WiFi Attacks And How To Detect Them]* blog post.

## What kind of data does it collect?

Nzyme collects, parses and forwards all relevant 802.11 management frames. Management frames are unecrypted so anyone close enough to a sending station (an access point, a computer, a phone, a lightbulb, a car, a juice maker, ...) can pick them up with nzyme.

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

If you have another one that supports monitor mode, you can use that one. Nzyme does by far not require any specific hardware.

### A small computer to run nzyme on.

I recommend to run nzyme on a Raspberry Pi 3 Model B. This is pretty much the reference architecture, because that is what I run it on.

In the end, it shoulnd’t really matter what you run it on, but the docs and guides will most likely refer to a Raspberry Pi with a Raspbian on in.

### A Graylog setup

## Channel hopping

The 802.11 standard [defines] many frequencies (channels) a network can operate on. This is useful to avoid contention and bandwidth issues, but also means that your wireless adapter has to be tuned to a single channel. During normal operations, your operating system will do this automatically for you.

Because we don’t want to listen on only one, but possibly **all** WiFi channels, we either need dozens of adapters, with one adapter for each channel, or we cycle over multiple channels on a single adapter rapidly. Nzyme allows you to configure multiple channels per WiFi adapter.

For example, if you configure nzyme to listen on channel 1,2,3,4,5,6 on `wlan0` and 7,8,9,10,11 on `wlan1`, it will tune `wlan0` to channel 1 for a configurable time (default is 1 second) and then switch to channel 2, then to channel 3 and so on. By doing this, we might miss a bunch of wireless frames but are not missing out on some channels completely.

The best configuration depends on your use-case but usually you will want to tune to all 2.4 Ghz and 5 Ghz WiFi channels.

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

Download the most recent build from the  [Releases] page.

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



## Installation and configuration on a Raspberry Pi 3

## Compatibility matrix

test with the other adapter

## Known issues

* Some WiFi adapters will not report the MAC timestamp in the radiotap header. The field will simply be missing in Graylog. This is usually an issue with the driver.
* The deauthentication and disassociation `reason` field is not reported correctly on some systems. This is known to be an issue on a 2016 MacBook Pro running macOS Sierra.
