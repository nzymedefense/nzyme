# -*- coding: utf-8 -*-

from scapy.all import *

# Come and take it.
def texasProbeRequest(interface, ssid):
    print "TexasProbeRequest armed. (⌐■_■)–︻╦╤─ – – pew pew"
    sendp(RadioTap()/
          Dot11(type=0,subtype=4,
                addr1="ff:ff:ff:ff:ff:ff",
                addr2=RandMAC(),
                addr3="ff:ff:ff:ff:ff:ff")/
          Dot11Elt(ID='SSID', info=ssid)/
          Dot11Elt(ID='Rates', info="\x02\x04\x0b\x0c\x12\x16\x18\x24"),
          iface=interface,loop=False,verbose=False)

texasProbeRequest("wlx00c0ca956856", "texasprobe")