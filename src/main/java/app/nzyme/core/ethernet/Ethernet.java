package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.dns.DNS;
import app.nzyme.core.ethernet.socks.Socks;

public class Ethernet {

    private final NzymeNode nzyme;

    private final DNS dns;
    private final Socks socks;

    public Ethernet(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.dns = new DNS(this);
        this.socks = new Socks(this);
    }

    public NzymeNode getNzyme() {
        return this.nzyme;
    }

    public DNS dns() {
        return dns;
    }

    public Socks socks() {
        return socks;
    }

}
