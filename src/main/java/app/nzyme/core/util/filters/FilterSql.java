package app.nzyme.core.util.filters;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FilterSql {

    public static FilterSqlFragment generate(Filters filters, SqlFilterProvider filterProvider) {
        String sql;
        Map<String, Object> bindings = Maps.newHashMap();
        if (filters.filters().isEmpty()) {
            sql = "";
        } else {
            StringBuilder fqb = new StringBuilder(" AND (");





            int i = 0;
            for (Map.Entry<String, List<Filter>> filteredField : filters.filters().entrySet()) {
                String fieldName = filteredField.getKey();
                fqb.append("(");

                int x = 0;
                for (Filter filter : filteredField.getValue()) {
                    String bindId = UUID.randomUUID().toString().replace("-", "_");
                    bindings.put(bindId, filter.value());

                    fqb.append("(")
                            .append(filterProvider.buildSql(bindId, fieldName, filter.operator()))
                            .append(")");

                    if (x < filteredField.getValue().size() - 1) {
                        fqb.append(" OR ");
                    }

                    x++;
                }

                if (i < filters.filters().size() - 1) {
                    fqb.append(") AND ");
                } else {
                    fqb.append(") ");
                }

                i++;
            }





            fqb.append(") ");
            sql = fqb.toString();
        }

        System.out.println(sql);

        return FilterSqlFragment.create(sql, bindings);
    }

}
