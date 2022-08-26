package horse.wtf.nzyme.ethernet;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.ethernet.dns.DNS;

public class Ethernet {

    private final NzymeLeader nzyme;

    private final DNS dns;

    public Ethernet(NzymeLeader nzyme) {
        this.nzyme = nzyme;
        this.dns = new DNS(this);
    }

    public NzymeLeader getNzyme() {
        return this.nzyme;
    }

    public DNS dns() {
        return dns;
    }

}
