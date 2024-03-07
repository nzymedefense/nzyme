package app.nzyme.core.rest;

import app.nzyme.core.rest.parameters.TimeRangeParameter;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.TimeRangeFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class RestResource {

    private final ObjectMapper om;

    public RestResource() {
        this.om = new ObjectMapper()
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

}
