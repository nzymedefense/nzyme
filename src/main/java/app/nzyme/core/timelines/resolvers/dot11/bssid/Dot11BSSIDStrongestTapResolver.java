package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResult;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.*;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11BSSIDStrongestTapResolver {

    private final NzymeNode nzyme;
    private final Timelines timelines;

    private final UUID organizationId;
    private final UUID tenantId;

    private static final ObjectMapper OM = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public Dot11BSSIDStrongestTapResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        this.nzyme = nzyme;
        this.timelines = new Timelines(nzyme);

        this.organizationId = organizationId;
        this.tenantId = tenantId;
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

        Optional<UUID> previousTapUuid = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_BSSID,
                        bssid,
                        TimelineEventType.DOT11_BSSID_STRONGEST_TAP)
                .map(event -> {
                    Map<String, Object> details = OM.readValue(event.eventDetails(), new TypeReference<>() {});
                    return UUID.fromString((String) details.get("strongest_tap_uuid"));
                });

        if (previousTapUuid.isPresent() && previousTapUuid.get().equals(currentTapUuid)) {
            return Optional.empty();
        }

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("strongest_tap_uuid", currentTapUuid);
        payload.put("strongest_tap_name", currentStrongest.get().tapName());
        return Optional.of(ResolverResult.create(payload));
    }

}
