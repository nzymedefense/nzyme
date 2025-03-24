package app.nzyme.core.integrations;

public interface ScheduledIntegration {

    Runnable execute();
    ScheduledIntegrationConfiguration getConfiguration();

}
