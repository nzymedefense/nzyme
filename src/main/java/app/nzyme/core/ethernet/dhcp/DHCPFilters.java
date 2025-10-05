package app.nzyme.core.ethernet.dhcp;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.ipAddressMatch;
import static app.nzyme.core.util.filters.FilterSql.stringMatch;

public class DHCPFilters implements SqlFilterProvider {
    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "transaction_type":
                return GeneratedSql.create(stringMatch(bindId, "transaction_type", operator), "");
            case "client_mac":
                return GeneratedSql.create(stringMatch(bindId, "client_mac", operator), "");
            case "server_mac":
                return GeneratedSql.create(stringMatch(bindId, "server_mac", operator), "");
            case "requested_ip":
                return GeneratedSql.create(ipAddressMatch(bindId, "requested_ip_address", operator), "");
            case "fingerprint":
                return GeneratedSql.create(stringMatch(bindId, "fingerprint", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }
}
