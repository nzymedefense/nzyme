package app.nzyme.core.dot11.db.filters;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class Dot11ConnectedClientFilters implements SqlFilterProvider  {
    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "client_mac":
                return GeneratedSql.create(macAddressMatch(bindId, "c.client_mac", operator), "");
            case "signal_strength":
                return GeneratedSql.create(numericMatch(bindId, "c.signal_strength_average", operator), "");
            case "connected_bssid":
                return GeneratedSql.create(macAddressMatch(bindId, "b.bssid", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }
}
