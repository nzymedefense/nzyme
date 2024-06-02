package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.dns.DNS;
import app.nzyme.core.ethernet.socks.Socks;
import app.nzyme.core.ethernet.ssh.SSH;
import app.nzyme.core.ethernet.tcp.TCP;

public class Ethernet {

    private final NzymeNode nzyme;

    private final TCP tcp;
    private final DNS dns;
    private final SSH ssh;
    private final Socks socks;

    public Ethernet(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.tcp = new TCP(this);
        this.dns = new DNS(this);
        this.ssh = new SSH(this);
        this.socks = new Socks(this);
    }

    public NzymeNode getNzyme() {
        return this.nzyme;
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

    public Socks socks() {
        return socks;
    }

}
