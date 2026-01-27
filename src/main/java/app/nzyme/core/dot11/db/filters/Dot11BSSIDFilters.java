package app.nzyme.core.dot11.db.filters;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class Dot11BSSIDFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "bssid":
                return GeneratedSql.create(stringMatch(bindId, "b.bssid", operator), "");
            case "signal_strength":
                return GeneratedSql.create(numericMatch(bindId, "b.signal_strength_average", operator), "");
            case "mode":
                return GeneratedSql.create(jsonbStringMatch(bindId, "s.infrastructure_types", operator), "");
            case "advertised_ssid":
                return GeneratedSql.create(stringMatch(bindId, "s.ssid", operator), "");
            case "client_count":
                return GeneratedSql.create("", numericMatch(bindId, "COUNT(DISTINCT(c.client_mac))", operator));
            case "security":
                return GeneratedSql.create(stringNoRegexMatch(bindId, "ssp.value", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
