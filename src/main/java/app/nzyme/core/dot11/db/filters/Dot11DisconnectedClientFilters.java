package app.nzyme.core.dot11.db.filters;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.numericMatch;
import static app.nzyme.core.util.filters.FilterSql.stringMatch;

public class Dot11DisconnectedClientFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "client_mac":
                return GeneratedSql.create(stringMatch(bindId, "c.client_mac", operator), "");
            case "signal_strength":
                return GeneratedSql.create(numericMatch(bindId, "c.signal_strength_average", operator), "");
            case "probe_request":
                return GeneratedSql.create(stringMatch(bindId, "pr.ssid", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
