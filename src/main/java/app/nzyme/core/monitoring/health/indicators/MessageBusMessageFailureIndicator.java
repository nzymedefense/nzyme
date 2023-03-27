package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.plugin.distributed.messaging.MessageBus;
import org.joda.time.DateTime;

public class MessageBusMessageFailureIndicator extends Indicator {

    private final MessageBus messages;

    public MessageBusMessageFailureIndicator(MessageBus messages) {
        this.messages = messages;
    }

    @Override
    protected IndicatorStatus doRun() {
        if (messages.getAllFailedMessagesSince(DateTime.now().minusHours(24)).isEmpty()) {
            return IndicatorStatus.green(this);
        } else {
            return IndicatorStatus.red(this);
        }
    }

    @Override
    public String getId() {
        return "message_bus_message_failure";
    }

    @Override
    public String getName() {
        return "Message Failure";
    }

}
