package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDChannelDetails;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.*;
import java.util.stream.Collectors;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11BSSIDTimelineSSIDAnnouncementResolver {

    private final NzymeNode nzyme;
    private final Timelines timelines;

    private final UUID organizationId;
    private final UUID tenantId;

    private static final ObjectMapper OM = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public Dot11BSSIDTimelineSSIDAnnouncementResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        this.nzyme = nzyme;
        this.timelines = new Timelines(nzyme);

        this.organizationId = organizationId;
        this.tenantId = tenantId;
    }

    public Optional<ResolverResult> resolve(String bssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        Set<String> currentSSIDs = nzyme.getDot11().findSSIDsOfBSSID(
                        TimeRange.create(now.minusMinutes(EVENT_HORIZON_MINUTES), now, false),
                        bssid,
                        taps
                ).stream()
                .map(SSIDChannelDetails::ssid)
                .collect(Collectors.toSet());

        if (currentSSIDs.isEmpty()) {
            return Optional.empty();
        }

        Optional<Set<String>> previousSSIDs = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_BSSID,
                        bssid,
                        TimelineEventType.DOT11_BSSID_SSID_DIFF)
                .map(event -> {
                    Map<String, Object> details = OM.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<String> known = (Collection<String>) details.getOrDefault(
                            "known_ssids", Collections.emptyList());
                    return new HashSet<>(known);
                });

        // First sight: no prior SSID_DIFF event.
        if (previousSSIDs.isEmpty()) {
            Map<String, Object> payload = Maps.newHashMap();
            payload.put("new_ssids", currentSSIDs);
            payload.put("known_ssids", currentSSIDs);
            return Optional.of(ResolverResult.create(payload));
        }

        Set<String> newSSIDs = Sets.difference(currentSSIDs, previousSSIDs.get());
        Set<String> disappearedSSIDs = Sets.difference(previousSSIDs.get(), currentSSIDs);

        if (newSSIDs.isEmpty() && disappearedSSIDs.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> payload = Maps.newHashMap();
        if (!newSSIDs.isEmpty()) {
            payload.put("new_ssids", newSSIDs);
        }
        if (!disappearedSSIDs.isEmpty()) {
            payload.put("disappeared_ssids", disappearedSSIDs);
        }
        payload.put("known_ssids", currentSSIDs);

        return Optional.of(ResolverResult.create(payload));
    }

}