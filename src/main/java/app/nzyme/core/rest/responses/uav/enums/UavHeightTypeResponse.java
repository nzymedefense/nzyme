package app.nzyme.core.rest.responses.uav.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum UavHeightTypeResponse {

    ABOVE_GROUND,
    ABOVE_TAKEOFF_LOCATION,
    OTHER;

    private static final Logger LOG = LogManager.getLogger(UavHeightTypeResponse.class);

    public static UavHeightTypeResponse fromString(String value) {
        if (value == null) return null;

        return switch (value) {
            case "AboveGround" -> ABOVE_GROUND;
            case "AboveTakeoffLocation" -> ABOVE_TAKEOFF_LOCATION;
            default -> {
                LOG.warn("Unknown UAV height type: [{}]. Returning [{}].", value, OTHER);
                yield OTHER;
            }
        };
    }

}
