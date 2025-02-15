package app.nzyme.core.rest.responses.uav.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum UavOperatorLocationTypeResponse {

    TAKEOFF,
    DYNAMIC,
    FIXED,
    OTHER;

    private static final Logger LOG = LogManager.getLogger(UavOperatorLocationTypeResponse.class);

    public static UavOperatorLocationTypeResponse fromString(String value) {
        if (value == null) return null;

        return switch (value) {
            case "Takeoff" -> TAKEOFF;
            case "Dynamic" -> DYNAMIC;
            case "Fixed" -> FIXED;
            default -> {
                LOG.warn("Unknown UAV operator location type: [{}]. Returning [{}].", value, OTHER);
                yield OTHER;
            }
        };
    }

}
