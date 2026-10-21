package app.nzyme.core.ethernet.beacons;

public enum BeaconType {

    TLS("TLS Beacon"),
    HTTP("HTTP Beacon"),
    SSH("SSH Beacon"),
    SOCKS("SOCKS Beacon"),
    DNS("DNS Beacon"),
    TCP("TCP Beacon"),
    UDP("UDP Beacon");

    private final String humanReadable;

    BeaconType(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

}
