package app.nzyme.core.events.actions.syslog;

import app.nzyme.core.events.actions.Action;
import app.nzyme.core.events.actions.ActionExecutionResult;
import app.nzyme.core.events.types.DetectionEvent;
import app.nzyme.core.events.types.SystemEvent;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SyslogAction implements Action {

    private static final Logger LOG = LogManager.getLogger(SyslogAction.class);

    private final String protocol;
    private final String syslogHostname;
    private final String host;
    private final int port;

    public SyslogAction(SyslogActionConfiguration configuration) {
        this.protocol = configuration.protocol();
        this.syslogHostname = configuration.syslogHostname();
        this.host = configuration.host();
        this.port = configuration.port();
    }

    @Override
    public ActionExecutionResult execute(SystemEvent event) {
        LOG.info("Executing [{}] for event type [{}].", this.getClass().getCanonicalName(), event.type());

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("timestamp", event.timestamp().toString());
        payload.put("event_supertype", "system");
        payload.put("event_type", event.type().name());
        payload.put("event_type_category", event.type().getCategory());
        payload.put("event_type_description", event.type().getDescription());
        payload.put("event_type_human_readable", event.type().getHumanReadableName());
        payload.put("details", event.details());

        return execute(payload);
    }

    @Override
    public ActionExecutionResult execute(DetectionEvent event) {
        LOG.info("Executing [{}] for event type [{}].", this.getClass().getCanonicalName(), event.detectionType());

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("timestamp", event.timestamp().toString());
        payload.put("event_supertype", "detection");
        payload.put("event_type", event.detectionType().name());
        payload.put("event_type_subsystem", event.detectionType().getSubsystem().name());
        payload.put("event_type_human_readable", event.detectionType().getTitle());
        payload.put("details", event.details());

        return execute(payload);
    }

    private ActionExecutionResult execute(Map<String, Object> payload) {
        return ActionExecutionResult.FAILURE;
    }

}
