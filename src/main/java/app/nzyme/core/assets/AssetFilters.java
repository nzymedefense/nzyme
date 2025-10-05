package app.nzyme.core.assets;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.stringMatch;

public class AssetFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "mac":
                return GeneratedSql.create(stringMatch(bindId, "mac", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }    }

}
