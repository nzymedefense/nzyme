package app.nzyme.core.ethernet.arp;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.ipAddressMatch;
import static app.nzyme.core.util.filters.FilterSql.stringMatch;

public class ARPFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "operation":
                return GeneratedSql.create(stringMatch(bindId, "operation", operator), "");
            case "ethernet_source_mac":
                return GeneratedSql.create(stringMatch(bindId, "ethernet_source_mac", operator), "");
            case "ethernet_destination_mac":
                return GeneratedSql.create(stringMatch(bindId, "ethernet_destination_mac", operator), "");
            case "arp_sender_mac":
                return GeneratedSql.create(stringMatch(bindId, "arp_sender_mac", operator), "");
            case "arp_sender_address":
                return GeneratedSql.create(ipAddressMatch(bindId, "arp_sender_address", operator), "");
            case "arp_target_mac":
                return GeneratedSql.create(stringMatch(bindId, "arp_target_mac", operator), "");
            case "arp_target_address":
                return GeneratedSql.create(ipAddressMatch(bindId, "arp_target_address", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
