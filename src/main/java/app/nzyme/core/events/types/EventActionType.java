package app.nzyme.core.events.types;

public enum EventActionType {

    EMAIL("Email"),
    WEBHOOK("Webhook");

    private final String humanReadable;

    EventActionType(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

}
