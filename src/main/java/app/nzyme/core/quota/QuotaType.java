package app.nzyme.core.quota;

public enum QuotaType {

    TAPS("Taps"),
    TENANTS("Tenants"),
    TENANT_USERS("Users"),
    INTEGRATIONS_COT("Cursor on Target Integrations");

    private final String humanReadable;

    QuotaType(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

}
