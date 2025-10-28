package app.nzyme.core.ethernet.l4.filters;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class L4Filters implements SqlFilterProvider  {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "l4_type":
                return GeneratedSql.create(stringMatch(bindId, "l4_Type", operator), "");
            case "source_mac":
                return GeneratedSql.create(stringMatch(bindId, "source_mac", operator), "");
            case "source_address":
                return GeneratedSql.create(ipAddressMatch(bindId, "source_address", operator), "");
            case "source_port":
                return GeneratedSql.create(numericMatch(bindId, "source_port", operator), "");
            case "destination_mac":
                return GeneratedSql.create(stringMatch(bindId, "destination_mac", operator), "");
            case "destination_address":
                return GeneratedSql.create(ipAddressMatch(bindId, "destination_address", operator), "");
            case "destination_port":
                return GeneratedSql.create(numericMatch(bindId, "destination_port", operator), "");
            case "bytes_rx_count":
                return GeneratedSql.create(numericMatch(bindId, "bytes_rx_count", operator), "");
            case "bytes_tx_count":
                return GeneratedSql.create(numericMatch(bindId, "bytes_tx_count", operator), "");
            case "segments_count":
                return GeneratedSql.create(numericMatch(bindId, "segments_count", operator), "");
            case "state":
                return GeneratedSql.create(stringMatch(bindId, "state", operator), "");
            case "session_key":
                return GeneratedSql.create(stringMatch(bindId, "session_key", operator), "");
            case "source_address_geo_asn_number":
                return GeneratedSql.create(numericMatch(bindId, "source_address_geo_asn_number", operator), "");
            case "source_address_geo_city":
                return GeneratedSql.create(stringMatch(bindId, "source_address_geo_city", operator), "");
            case "source_address_geo_country_code":
                return GeneratedSql.create(stringMatch(bindId, "source_address_geo_country_code", operator), "");
            case "destination_address_geo_asn_number":
                return GeneratedSql.create(numericMatch(bindId, "destination_address_geo_asn_number", operator), "");
            case "destination_address_geo_city":
                return GeneratedSql.create(stringMatch(bindId, "destination_address_geo_city", operator), "");
            case "destination_address_geo_country_code":
                return GeneratedSql.create(stringMatch(bindId, "destination_address_geo_country_code", operator), "");
            case "tcp_fingerprint":
                return GeneratedSql.create(stringMatch(bindId, "tcp_fingerprint", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
