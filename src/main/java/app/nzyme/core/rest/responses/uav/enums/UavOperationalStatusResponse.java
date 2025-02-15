package app.nzyme.core.rest.responses.uav.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum UavOperationalStatusResponse {

    UNDECLARED,
    GROUND,
    AIRBORNE,
    EMERGENCY,
    REMOTE_ID_SYSTEM_FAILURE,
    OTHER;

    private static final Logger LOG = LogManager.getLogger(UavOperationalStatusResponse.class);

    public static UavOperationalStatusResponse fromString(String value) {
        if (value == null) return null;

        return switch (value) {
            case "Undeclared" -> UNDECLARED;
            case "Ground" -> GROUND;
            case "Airborne" -> AIRBORNE;
            case "Emergency" -> EMERGENCY;
            case "RemoteIdSystemFailure" -> REMOTE_ID_SYSTEM_FAILURE;
            case "Other" -> OTHER;
            default -> {
                LOG.warn("Unknown UAV operational status type: [{}]. Returning [{}].", value, OTHER);
                yield OTHER;
            }
        };
    }

}
