package app.nzyme.core.events.actions;

import app.nzyme.core.events.types.SystemEvent;

public interface Action {

    ActionExecutionResult execute(SystemEvent event);
    // ActionExecutionResult execute(DetectionEvent event);

}
