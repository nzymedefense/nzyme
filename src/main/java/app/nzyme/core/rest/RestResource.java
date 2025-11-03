package app.nzyme.core.rest;

import app.nzyme.core.rest.parameters.FiltersParameter;
import app.nzyme.core.rest.parameters.TimeRangeParameter;
import app.nzyme.core.util.filters.Filter;
import app.nzyme.core.util.filters.FilterFactory;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.TimeRangeFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class RestResource {

    private static final Logger LOG = LogManager.getLogger(RestResource.class);

    private final ObjectMapper om;

    public RestResource() {
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JodaModule());
    }

    public TimeRange parseTimeRangeQueryParameter(String query) {
        TimeRangeParameter param;
        try {
            param = this.om.readValue(query, TimeRangeParameter.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid time range parameter provided.", e);
        }

        return TimeRangeFactory.fromRestQuery(param);
    }

    public Filters parseFiltersQueryParameter(String query) {
        if (query == null || query.isEmpty()) {
            return Filters.create(Maps.newHashMap());
        }

        Map<String, List<FiltersParameter>> param;
        try {
            param = this.om.readValue(query, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOG.error(e);
            throw new IllegalArgumentException("Invalid filters parameter provided.", e);
        }

        Map<String, List<Filter>> filters = Maps.newHashMap();
        for (Map.Entry<String, List<FiltersParameter>> filterParam : param.entrySet()) {
            try {
                List<Filter> fieldFilters = Lists.newArrayList();
                for (FiltersParameter filter : filterParam.getValue()) {
                    fieldFilters.add(FilterFactory.fromRestQuery(filter));
                }
                filters.put(filterParam.getKey(), fieldFilters);
            } catch (Exception e) {
                LOG.error(e);
                throw new IllegalArgumentException("Invalid filters parameter provided.", e);
            }
        }

        return Filters.create(filters);
    }

}
