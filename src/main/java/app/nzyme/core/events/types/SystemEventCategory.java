package app.nzyme.core.events.types;

public enum SystemEventCategory {

    AUTHENTICATION("Authentication");

    private final String name;

    SystemEventCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
