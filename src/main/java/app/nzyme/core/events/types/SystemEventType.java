package app.nzyme.core.events.types;

public enum SystemEventType {

    AUTHENTICATION_PASSWORD_CHANGED(
            SystemEventCategory.AUTHENTICATION,
            "A user password changed",
            "The password for an user has been modified, either by the user themselves or by an administrator."
    ),

    AUTHENTICATION_MFA_RESET(
            SystemEventCategory.AUTHENTICATION,
            "MFA method has been reset",
            "Multi-factor method for an user has been reset, either by the user themselves or by an " +
                    "administrator."
    ),

    AUTHENTICATION_MFA_RECOVERY_CODE_USED(
            SystemEventCategory.AUTHENTICATION,
            "MFA recovery code used",
            "A user has successfully utilized one of their MFA recovery codes to log in."
    ),

    AUTHENTICAITON_MFA_RECOVERY_CODE_REUSED(
            SystemEventCategory.AUTHENTICATION,
            "MFA recovery code re-used",
            "A user attempted to reuse one of their previously utilized MFA recovery codes for login, which " +
                    "was unsuccessful."
    );

    private final SystemEventCategory category;
    private final String name;
    private final String description;

    SystemEventType(SystemEventCategory category, String name, String description) {
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public SystemEventCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
