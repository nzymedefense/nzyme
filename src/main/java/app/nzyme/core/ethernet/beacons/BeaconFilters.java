package app.nzyme.core.ethernet.beacons;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class BeaconFilters implements SqlFilterProvider {

    private final String prefix;

    public BeaconFilters() {
        this("");
    }

    /**
     * @param tableAlias Alias of the beacons table in queries that join other tables, like "b". Columns are
     *                   qualified with it to avoid ambiguity.
     */
    public BeaconFilters(String tableAlias) {
        this.prefix = tableAlias == null || tableAlias.isEmpty() ? "" : tableAlias + ".";
    }

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "name":
                return GeneratedSql.create(stringMatch(bindId, prefix + "name", operator), "");
            case "description":
                return GeneratedSql.create(stringMatch(bindId, prefix + "description", operator), "");
            case "type":
                return GeneratedSql.create(stringMatch(bindId, prefix + "type", operator), "");
            case "classification":
                return GeneratedSql.create(stringMatch(bindId, prefix + "classification", operator), "");
            case "destination_host":
                return GeneratedSql.create(stringMatch(bindId, prefix + "destination_host", operator), "");
            case "destination_address":
                return GeneratedSql.create(ipAddressMatch(bindId, prefix + "destination_address", operator), "");
            case "destination_port":
                return GeneratedSql.create(numericMatch(bindId, prefix + "destination_port", operator), "");
            case "destination_is_site_local":
                return GeneratedSql.create(booleanMatch(bindId, prefix + "destination_is_site_local", operator), "");
            case "source_count":
                return GeneratedSql.create(numericMatch(bindId, prefix + "source_count", operator), "");
            case "checkin_count":
                return GeneratedSql.create(numericMatch(bindId, prefix + "checkin_count", operator), "");
            case "interval_seconds":
                return GeneratedSql.create(numericMatch(bindId, prefix + "interval_seconds", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
