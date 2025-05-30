package app.nzyme.core.events.actions;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.actions.email.EmailAction;
import app.nzyme.core.events.actions.email.EmailActionConfiguration;
import app.nzyme.core.events.actions.webhook.WebhookAction;
import app.nzyme.core.events.actions.webhook.WebhookActionConfiguration;
import app.nzyme.core.events.db.EventActionEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventActionFactory {

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static Action build(NzymeNode nzyme, EventActionEntry ea) throws NoSuchActionTypeException, JsonProcessingException{
        ObjectMapper om = new ObjectMapper();

        switch (ea.actionType()) {
            case "EMAIL":
                return new EmailAction(nzyme, om.readValue(ea.configuration(), EmailActionConfiguration.class));
            case "WEBHOOK":
                return new WebhookAction(nzyme, om.readValue(ea.configuration(), WebhookActionConfiguration.class));
            default:
                throw new NoSuchActionTypeException();
        }
    }

    public static final class NoSuchActionTypeException extends Exception { }

}
