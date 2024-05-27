package app.nzyme.core.rest;

import app.nzyme.core.ethernet.L4AddressData;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressTypeResponse;

public class RestHelpers {

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

        return L4AddressResponse.create(typeResponse, data.mac(), data.address(), data.port());
    }

}
