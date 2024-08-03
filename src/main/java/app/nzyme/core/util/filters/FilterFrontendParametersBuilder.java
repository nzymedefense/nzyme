package app.nzyme.core.util.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class FilterFrontendParametersBuilder {

    private final Map<String, List<FilterFrontendParameter>> parameters;

    public FilterFrontendParametersBuilder() {
        this.parameters = Maps.newHashMap();
    }

    public FilterFrontendParametersBuilder addFilter(String field, FilterFrontendParameter parameter) {
        if (!parameters.containsKey(field)) {
            parameters.put(field, Lists.newArrayList());
        }

        parameters.get(field).add(parameter);

        return this;
    }

    public String build() {
        ObjectMapper om = new ObjectMapper();

        try {
            return om.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
