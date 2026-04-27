package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDChannelDetails;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.timelines.resolvers.helpers.StringSetComparison;
import app.nzyme.core.util.TimeRange;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11BSSIDSSIDAnnouncementResolver extends TimelineResolver {

    public Dot11BSSIDSSIDAnnouncementResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
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
                    Map<String, Object> details = objectMapper.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<String> known = (Collection<String>) details.getOrDefault(
                            "known_ssids", Collections.emptyList());
                    return new HashSet<>(known);
                });

        return StringSetComparison.compare(
                previousSSIDs,
                currentSSIDs,
                "new_ssids",
                "disappeared_ssids",
                "known_ssids"
        );
    }

}