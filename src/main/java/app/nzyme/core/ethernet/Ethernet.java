package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.dns.DNS;

public class Ethernet {

    private final NzymeNode nzyme;

    private final DNS dns;

    public Ethernet(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.dns = new DNS(this);
    }

    public NzymeNode getNzyme() {
        return this.nzyme;
    }

    public DNS dns() {
        return dns;
    }

}
