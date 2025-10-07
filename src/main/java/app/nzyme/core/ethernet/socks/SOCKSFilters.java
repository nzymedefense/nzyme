package app.nzyme.core.ethernet.socks;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class SOCKSFilters implements SqlFilterProvider {
    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "session_key":
                return GeneratedSql.create(stringMatch(bindId, "tcp_session_key", operator), "");
            case "client_address":
                return GeneratedSql.create("", ipAddressMatch(bindId, "ANY_VALUE(tcp.source_address)", operator));
            case "server_address":
                return GeneratedSql.create("", ipAddressMatch(bindId, "ANY_VALUE(tcp.destination_address)", operator));
            case "type":
                return GeneratedSql.create(stringMatch(bindId, "socks_type", operator), "");
            case "status":
                return GeneratedSql.create(stringMatch(bindId, "connection_status", operator), "");
            case "tunneled_bytes":
                return GeneratedSql.create(numericMatch(bindId, "tunneled_bytes", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }
}
