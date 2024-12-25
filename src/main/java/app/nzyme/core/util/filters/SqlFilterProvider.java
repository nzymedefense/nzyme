package app.nzyme.core.util.filters;

public interface SqlFilterProvider {

    GeneratedSql buildSql(String binding, String fieldName, FilterOperator operator);

}