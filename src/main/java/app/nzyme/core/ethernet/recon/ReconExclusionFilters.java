package app.nzyme.core.ethernet.recon;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class ReconExclusionFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "name":
                return GeneratedSql.create(stringMatch(bindId, "name", operator), "");
            case "description":
                return GeneratedSql.create(stringMatch(bindId, "description", operator), "");
            case "enabled":
                return GeneratedSql.create(booleanMatch(bindId, "enabled", operator), "");
            case "scan_type":
                return GeneratedSql.create(scanTypeMatch(bindId, operator), "");
            case "source_cidr":
                return GeneratedSql.create(stringMatch(bindId, "source_cidr", operator), "");
            case "source_mac":
                return GeneratedSql.create(stringMatch(bindId, "source_mac", operator), "");
            case "target_cidr":
                return GeneratedSql.create(stringMatch(bindId, "target_cidr", operator), "");
            case "match_count":
                return GeneratedSql.create(numericMatch(bindId, "match_count", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

    /*
     * Scan types are stored as a comma-separated list. An exclusion without types matches every type, so it is
     * never "equal" to a type and always "not equal".
     */
    private static String scanTypeMatch(String bindId, FilterOperator operator) {
        String contains = "(',' || scan_types || ',') LIKE ('%,' || :" + bindId + " || ',%')";
        switch (operator) {
            case EQUALS:
                return "(scan_types IS NOT NULL AND " + contains + ")";
            case NOT_EQUALS:
                return "(scan_types IS NULL OR NOT " + contains + ")";
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for scan type field.");
        }
    }

}
