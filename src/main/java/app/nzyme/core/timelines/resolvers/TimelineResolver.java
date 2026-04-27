package app.nzyme.core.timelines.resolvers;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.timelines.Timelines;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.UUID;

public abstract class TimelineResolver {

    protected final NzymeNode nzyme;
    protected final Timelines timelines;

    protected final UUID organizationId;
    protected final UUID tenantId;

    protected final ObjectMapper objectMapper;

    protected TimelineResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        this.nzyme = nzyme;
        this.timelines = new Timelines(nzyme);
        this.organizationId = organizationId;
        this.tenantId = tenantId;

        this.objectMapper = JsonMapper.builder()
                .addModule(new JodaModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

}
