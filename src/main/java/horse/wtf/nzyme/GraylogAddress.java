package horse.wtf.nzyme;

public class GraylogAddress {

    private final String host;
    private final int port;

    public GraylogAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


}
