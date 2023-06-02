package app.nzyme.core.events.types;

public enum SystemEventCategory {

    AUTHENTICATION("Authentication"),
    HEALTH_INDICATOR("System Health Indicator");

    private final String humanReadableName;

    SystemEventCategory(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

}
