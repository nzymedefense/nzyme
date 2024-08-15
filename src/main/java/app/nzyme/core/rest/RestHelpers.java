package app.nzyme.core.rest;

import app.nzyme.core.ethernet.L4AddressData;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.tcp.TcpSessionState;
import app.nzyme.core.rest.responses.ethernet.L4AddressAttributesResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressGeoResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressTypeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RestHelpers {

    private static final Logger LOG = LogManager.getLogger(RestHelpers.class);

    public static L4AddressResponse L4AddressDataToResponse(L4Type type, L4AddressData data) {
        L4AddressTypeResponse typeResponse;
        switch(type) {
            case TCP:
                typeResponse = L4AddressTypeResponse.TCP;
                break;
            case UDP:
                typeResponse = L4AddressTypeResponse.UDP;
                break;
            default:
                throw new RuntimeException("Unknown L4 type [" + type + "].");
        }

        L4AddressGeoResponse geo;
        if (data.geo() != null) {
            geo = L4AddressGeoResponse.create(
                    data.geo().asnNumber(),
                    data.geo().asnName(),
                    data.geo().asnDomain(),
                    data.geo().city(),
                    data.geo().countryCode(),
                    data.geo().latitude(),
                    data.geo().longitude()
            );
        } else {
            geo = null;
        }

        L4AddressAttributesResponse attributes;
        if (data.attributes() != null) {
            attributes = L4AddressAttributesResponse.create(
                    data.attributes().isSiteLocal(),
                    data.attributes().isLoopback(),
                    data.attributes().isMulticast()
            );
        } else {
            attributes = null;
        }

        return L4AddressResponse.create(
                typeResponse,
                data.mac(),
                data.address(),
                data.port(),
                geo,
                attributes,
                null
        );
    }

    public static String tcpSessionStateToGeneric(TcpSessionState state) {
        switch (state) {
            case SYNSENT:
            case SYNRECEIVED:
            case ESTABLISHED:
            case FINWAIT1:
            case FINWAIT2:
                return "Active";
            case CLOSEDFIN:
            case CLOSEDRST:
            case REFUSED:
            case CLOSEDTIMEOUT:
            case CLOSEDTIMEOUTNODE:
                return "Inactive";
        }

        return "Invalid";
    }
}
