package app.nzyme.core.rest.responses.uav.enums;

import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum UavTypeResponse {

    UNDECLARED,
    AEROPLANE,
    MULTIROTOR_HELICOPTER,
    GYROPLANE,
    VTOL,
    ORNITHOPTER,
    GLIDER,
    KITE,
    FREE_BALLOON,
    CAPTIVE_BALLOON,
    AIRSHIP,
    UNPOWERED_FREEFALL,
    ROCKET,
    TETHERED_POWERED,
    GROUND_OBSTACLE,
    OTHER;

    private static final Logger LOG = LogManager.getLogger(UavTypeResponse.class);

    @Nullable
    public static UavTypeResponse fromString(String value) {
        if (value == null) return null;

        return switch (value) {
            case "Undeclared" -> UNDECLARED;
            case "Aeroplane" -> AEROPLANE;
            case "MultirotorHelicopter" -> MULTIROTOR_HELICOPTER;
            case "Gyroplane" -> GYROPLANE;
            case "Vtol" -> VTOL;
            case "Ornithopter" -> ORNITHOPTER;
            case "Glider" -> GLIDER;
            case "Kite" -> KITE;
            case "FreeBalloon" -> FREE_BALLOON;
            case "CaptiveBalloon" -> CAPTIVE_BALLOON;
            case "Airship" -> AIRSHIP;
            case "UnpoweredFreeFall" -> UNPOWERED_FREEFALL;
            case "Rocket" -> ROCKET;
            case "TetheredPowered" -> TETHERED_POWERED;
            case "GroundObstacle" -> GROUND_OBSTACLE;
            case "Other" -> OTHER;
            default -> {
                LOG.warn("Unknown UAV type: [{}]. Returning [{}].", value, OTHER);
                yield OTHER;
            }
        };
    }

}
