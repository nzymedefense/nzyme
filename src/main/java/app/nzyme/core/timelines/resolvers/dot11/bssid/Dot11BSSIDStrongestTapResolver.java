package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResult;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11BSSIDStrongestTapResolver extends TimelineResolver {

    public Dot11BSSIDStrongestTapResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(String bssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        Optional<TapBasedSignalStrengthResult> currentStrongest = nzyme.getDot11()
                .findBSSIDSignalStrengthPerTap(bssid, TimeRange.create(now.minusMinutes(EVENT_HORIZON_MINUTES), now, false), taps)
                .stream()
                .max(Comparator.comparingDouble(TapBasedSignalStrengthResult::signalStrength));

        if (currentStrongest.isEmpty()) {
            return Optional.empty();
        }

        UUID currentTapUuid = currentStrongest.get().tapUuid();

        Optional<Map<String, Object>> previousTap = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_BSSID,
                        bssid,
                        TimelineEventType.DOT11_BSSID_STRONGEST_TAP)
                .map(event -> objectMapper.readValue(event.eventDetails(), new TypeReference<>() {}));

        if (previousTap.isPresent()) {
            UUID previousTapUuid = UUID.fromString((String) previousTap.get().get("strongest_tap_uuid"));
            if (previousTapUuid.equals(currentTapUuid)) {
                return Optional.empty();
            }
        }

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("strongest_tap_uuid", currentTapUuid);
        payload.put("strongest_tap_name", currentStrongest.get().tapName());
        payload.put("strongest_tap_rssi", currentStrongest.get().signalStrength());

        if (previousTap.isPresent()) {
            payload.put("previous_tap_uuid", previousTap.get().get("strongest_tap_uuid"));
            payload.put("previous_tap_name", previousTap.get().get("strongest_tap_name"));
            payload.put("previous_tap_rssi", previousTap.get().get("strongest_tap_rssi"));
        }

        return Optional.of(ResolverResult.create(payload));
    }

}
