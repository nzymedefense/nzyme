package app.nzyme.core.monitors;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class MonitorFilters implements SqlFilterProvider {

    public static final String ALERTED_EXPRESSION =
            "COALESCE(last_event > NOW() - (interval * INTERVAL '1 minute'), false)";

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "name":
                return GeneratedSql.create(stringMatch(bindId, "name", operator), "");
            case "description":
                return GeneratedSql.create(stringMatch(bindId, "description", operator), "");
            case "type":
                return GeneratedSql.create(stringMatch(bindId, "type", operator), "");
            case "enabled":
                return GeneratedSql.create(booleanMatch(bindId, "enabled", operator), "");
            case "alerted":
                return GeneratedSql.create(booleanMatch(bindId, ALERTED_EXPRESSION, operator), "");
            case "interval":
                return GeneratedSql.create(numericMatch(bindId, "interval", operator), "");
            case "lookback":
                return GeneratedSql.create(numericMatch(bindId, "lookback", operator), "");
            case "trigger_condition":
                return GeneratedSql.create(numericMatch(bindId, "trigger_condition", operator), "");
            case "tap_count":
                return GeneratedSql.create(numericMatch(bindId, "cardinality(taps)", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
