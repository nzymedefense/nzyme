package app.nzyme.core.ethernet.recon;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class ReconScanTargetFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "target_address":
                return GeneratedSql.create(ipAddressMatch(bindId, "target_address", operator), "");
            case "target_port":
                return GeneratedSql.create(numericMatch(bindId, "target_port", operator), "");
            case "outcome":
                return GeneratedSql.create(stringMatch(bindId, "outcome", operator), "");
            case "probe_count":
                return GeneratedSql.create(numericMatch(bindId, "probe_count", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}
