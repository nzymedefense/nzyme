package app.nzyme.core.util.filters;

public interface SqlFilterProvider {

    String buildSql(String binding, String fieldName, FilterOperator operator);

}