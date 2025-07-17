package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.arp.ARP;
import app.nzyme.core.ethernet.dhcp.DHCP;
import app.nzyme.core.ethernet.dns.DNS;
import app.nzyme.core.ethernet.l4.L4;
import app.nzyme.core.ethernet.socks.SOCKS;
import app.nzyme.core.ethernet.ssh.SSH;
import app.nzyme.core.ethernet.l4.tcp.TCP;

public class Ethernet {

    private final NzymeNode nzyme;

    private final ARP arp;
    private final DHCP dhcp;
    private final L4 l4;
    private final TCP tcp;
    private final DNS dns;
    private final SSH ssh;
    private final SOCKS socks;

    public Ethernet(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.arp = new ARP(this);
        this.dhcp = new DHCP(this);
        this.l4 = new L4(this);
        this.tcp = new TCP(this);
        this.dns = new DNS(this);
        this.ssh = new SSH(this);
        this.socks = new SOCKS(this);
    }

    public NzymeNode getNzyme() {
        return this.nzyme;
    }

    public ARP arp() {
        return arp;
    }

    public DHCP dhcp() {
        return dhcp;
    }

    public L4 l4() {
        return l4;
    }

    public TCP tcp() {
        return tcp;
    }

    public DNS dns() {
        return dns;
    }

    public SSH ssh() {
        return ssh;
    }

    public SOCKS socks() {
        return socks;
    }

}
