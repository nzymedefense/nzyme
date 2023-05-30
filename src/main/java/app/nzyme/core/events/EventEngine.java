package app.nzyme.core.events;

import app.nzyme.core.events.types.SystemEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public interface EventEngine {

    void processEvent(SystemEvent event, @Nullable UUID organizationId, @Nullable UUID tenantId);
    //void processEvent(DetectionEvent event, @Nullable UUID organizationId, @Nullable UUID tenantId);

}
