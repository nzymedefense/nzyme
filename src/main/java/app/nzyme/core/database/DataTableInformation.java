package app.nzyme.core.database;

import jakarta.annotation.Nullable;

public class DataTableInformation {

    private final String tableName;
    private final String rowCountQuery;

    @Nullable
    private final String purgeQuery;

    public DataTableInformation(String tableName, String rowCountQuery, String purgeQuery) {
        this.tableName = tableName;
        this.rowCountQuery = rowCountQuery;
        this.purgeQuery = purgeQuery;
    }

    public String getTableName() {
        return tableName;
    }

    public String getRowCountQuery() {
        return rowCountQuery;
    }

    @Nullable
    public String getPurgeQuery() {
        return purgeQuery;
    }
}
