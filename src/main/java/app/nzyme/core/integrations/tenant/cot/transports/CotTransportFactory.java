package app.nzyme.core.integrations.tenant.cot.transports;

import jakarta.annotation.Nullable;

public class CotTransportFactory {

    public static CotTransport buildTransport(CotTransportType transportType,
                                              String address,
                                              int port,
                                              @Nullable byte[] certificate) {
        switch (transportType) {
            case UDP_PLAINTEXT:
                return new CotPlaintextUdpTransport(address, port);
            case TCP_X509:
                return new CotTlsTcpTransport(address, port, certificate);
        }

        throw new IllegalArgumentException("Unknown CoT transport type: [" + transportType + "]");
    }

}
