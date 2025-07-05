package app.nzyme.core.util.filters;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FilterSql {

    public static FilterSqlFragment generate(Filters filters, SqlFilterProvider filterProvider) {
        String whereSql;
        String havingSql;
        Map<String, Object> bindings = Maps.newHashMap();
        if (filters.filters().isEmpty()) {
            whereSql = " ";
            havingSql = " ";
        } else {
            // Build lists of all filtered fields that require a WHERE clause, same for HAVING clause.
            Map<String, List<Filter>> whereFilteredFields = Maps.newHashMap();
            Map<String, List<Filter>> havingFilteredFields = Maps.newHashMap();

            for (Map.Entry<String, List<Filter>> filteredField : filters.filters().entrySet()) {
                String fieldName = filteredField.getKey();

                for (Filter filter : filteredField.getValue()) {
                    String bindId = UUID.randomUUID().toString().replace("-", "_");

                    GeneratedSql generatedSql = filterProvider.buildSql(bindId, fieldName, filter.operator());

                    if (!generatedSql.where().isEmpty()) {
                        whereFilteredFields.put(fieldName, filteredField.getValue());
                    }

                    if (!generatedSql.having().isEmpty()) {
                        havingFilteredFields.put(fieldName, filteredField.getValue());
                    }
                }
            }

            if (whereFilteredFields.isEmpty()) {
                whereSql = " ";
            } else {
                GeneratedFilterTypeSql generated = generateFilterTypeSql(whereFilteredFields, filterProvider);
                whereSql = generated.sql();
                bindings.putAll(generated.bindings());
            }

            if (havingFilteredFields.isEmpty()) {
                havingSql = " ";
            } else {
                GeneratedFilterTypeSql generated = generateFilterTypeSql(havingFilteredFields, filterProvider);
                havingSql = generated.sql();
                bindings.putAll(generated.bindings());
            }
        }

        return FilterSqlFragment.create(whereSql, havingSql, bindings);
    }

    private static GeneratedFilterTypeSql generateFilterTypeSql(Map<String, List<Filter>> filters, SqlFilterProvider filterProvider) {
        Map<String, Object> bindings = Maps.newHashMap();

        if (filters.isEmpty()) {
            return GeneratedFilterTypeSql.create(bindings, " ");
        }

        StringBuilder fqb = new StringBuilder(" AND (");

        int i = 0;
        for (Map.Entry<String, List<Filter>> filteredField : filters.entrySet()) {
            String fieldName = filteredField.getKey();
            fqb.append("(");

            int x = 0;
            for (Filter filter : filteredField.getValue()) {
                String bindId = UUID.randomUUID().toString().replace("-", "_");
                bindings.put(bindId, filter.value());

                GeneratedSql generatedSql = filterProvider.buildSql(bindId, fieldName, filter.operator());
                String sqlPart;
                if (!generatedSql.where().isEmpty()) {
                    sqlPart = generatedSql.where();
                } else {
                    sqlPart = generatedSql.having();
                }

                fqb.append("(")
                        .append(sqlPart)
                        .append(")");

                if (x < filteredField.getValue().size() - 1) {
                    fqb.append(" OR ");
                }

                x++;
            }

            if (i < filters.size() - 1) {
                fqb.append(") AND ");
            } else {
                fqb.append(") ");
            }

            i++;
        }

        fqb.append(") ");

        return GeneratedFilterTypeSql.create(bindings, fqb.toString());
    }

    public static String stringMatch(String bindId, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS:
                return fieldName + " = :" + bindId;
            case NOT_EQUALS:
                return fieldName + " <> :" + bindId;
            case REGEX_MATCH:
                return fieldName + " ~ :" + bindId;
            case NOT_REGEX_MATCH:
                return fieldName + " !~ :" + bindId;
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for string field [" + fieldName + "].");
        }
    }

    public static String stringNoRegexMatch(String bindId, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS:
                return fieldName + " = :" + bindId;
            case NOT_EQUALS:
                return fieldName + " <> :" + bindId;
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for string field [" + fieldName + "].");
        }
    }

    public static String numericMatch(String bindId, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS_NUMERIC:
                return fieldName + " = :" + bindId;
            case NOT_EQUALS_NUMERIC:
                return fieldName + " <> :" + bindId;
            case GREATER_THAN:
                return fieldName + " > :" + bindId;
            case SMALLER_THAN:
                return fieldName + " < :" + bindId;
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for numeric field [" + fieldName + "].");
        }
    }

    public static String ipAddressMatch(String bindId, String fieldName, FilterOperator operator) {
        switch (operator) {
            case EQUALS:
                return fieldName + " = :" + bindId + "::inet";
            case NOT_EQUALS:
                return fieldName + " <> :" + bindId + "::inet";
            case REGEX_MATCH:
                return fieldName + " ~ :" + bindId;
            case NOT_REGEX_MATCH:
                return fieldName + " !~ :" + bindId;
            case IN_CIDR:
                return fieldName + " <<= :" + bindId + "::cidr";
            case NOT_IN_CIDR:
                return fieldName + " <<| :" + bindId + "::cidr";
            case IS_PRIVATE:
                return "(" + fieldName + " <<= '10.0.0.0/8'::cidr OR "
                        + fieldName + " <<= '172.16.0.0/12'::cidr OR "
                        + fieldName + " <<= '192.168.0.0/16'::cidr)";
            case IS_NOT_PRIVATE:
                return "NOT (" + fieldName + " <<= '10.0.0.0/8'::cidr OR "
                        + fieldName + " <<= '172.16.0.0/12'::cidr OR "
                        + fieldName + " <<= '192.168.0.0/16'::cidr)";
            default:
                throw new RuntimeException("Invalid operator [" + operator + "] for IP address " +
                        "field [" + fieldName + "].");
        }
    }

    @AutoValue
    public static abstract class GeneratedFilterTypeSql {

        public abstract Map<String, Object> bindings();
        public abstract String sql();

        public static GeneratedFilterTypeSql create(Map<String, Object> bindings, String sql) {
            return builder()
                    .bindings(bindings)
                    .sql(sql)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_FilterSql_GeneratedFilterTypeSql.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder bindings(Map<String, Object> bindings);

            public abstract Builder sql(String sql);

            public abstract GeneratedFilterTypeSql build();
        }
    }

}
