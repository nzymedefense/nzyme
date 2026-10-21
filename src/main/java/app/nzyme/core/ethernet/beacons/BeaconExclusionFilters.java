package app.nzyme.core.ethernet.beacons;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class BeaconExclusionFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "name":
                return GeneratedSql.create(stringMatch(bindId, "name", operator), "");
            case "description":
                return GeneratedSql.create(stringMatch(bindId, "description", operator), "");
            case "enabled":
                return GeneratedSql.create(booleanMatch(bindId, "enabled", operator), "");
            case "destination_host":
                return GeneratedSql.create(stringMatch(bindId, "destination_host", operator), "");
            case "destination_cidr":
                return GeneratedSql.create(stringMatch(bindId, "destination_cidr", operator), "");
            case "destination_ports":
                return GeneratedSql.create(stringMatch(bindId, "destination_ports", operator), "");
            case "beacon_type":
                return GeneratedSql.create(beaconTypeMatch(bindId, operator), "");
            case "source_cidr":
                return GeneratedSql.create(stringMatch(bindId, "source_cidr", operator), "");
            case "source_mac":
                return GeneratedSql.create(stringMatch(bindId, "source_mac", operator), "");
            case "match_count":
                return GeneratedSql.create(numericMatch(bindId, "match_count", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

    /*
     * Beacon types are stored as a comma-separated list. An exclusion without types matches every type, so it is
     * never "equal" to a type and always "not equal".
     */
    private static String beaconTypeMatch(String bindId, FilterOperator operator) {
        String contains = "(',' || beacon_types || ',') LIKE ('%,' || :" + bindId + " || ',%')";
        switch (operator) {
            case EQUALS:
                return "(beacon_types IS NOT NULL AND " + contains + ")";
            case NOT_EQUALS:
                return "(beacon_types IS NULL OR NOT " + contains + ")";
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for beacon type field.");
        }
    }

}
