package app.nzyme.core.rest.responses.uav.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum UavDetectionSourceResponse {

    REMOTE_ID_WIFI,
    REMOTE_ID_BLUETOOTH,
    OTHER;

    private static final Logger LOG = LogManager.getLogger(UavDetectionSourceResponse.class);

    public static UavDetectionSourceResponse fromString(String value) {
        if (value == null) return null;

        return switch (value) {
            case "RemoteIdWiFi" -> REMOTE_ID_WIFI;
            case "RemoteIdBluetooth" -> REMOTE_ID_BLUETOOTH;
            default -> {
                LOG.warn("Unknown UAV detection source type: [{}]. Returning [{}].", value, OTHER);
                yield OTHER;
            }
        };
    }

}
