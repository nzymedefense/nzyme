package app.nzyme.core.util.filters;

import app.nzyme.core.rest.parameters.FiltersParameter;

public class FilterFactory {

    public static Filter fromRestQuery(FiltersParameter parameter) {
        switch (parameter.operator()) {
            case "equals":
                return Filter.create(
                        parameter.field(), FilterOperator.EQUALS, parameter.value()
                );
            case "not_equals":
                return Filter.create(
                        parameter.field(), FilterOperator.NOT_EQUALS, parameter.value()
                );
            case "equals_numeric":
                return Filter.create(
                        parameter.field(), FilterOperator.EQUALS_NUMERIC, Long.valueOf(parameter.value())
                );
            case "not_equals_numeric":
                return Filter.create(
                        parameter.field(), FilterOperator.NOT_EQUALS_NUMERIC, Long.valueOf(parameter.value())
                );
            default:
                throw new RuntimeException("Unknown filter operator: [" + parameter.operator() + "]");
        }
    }

}
