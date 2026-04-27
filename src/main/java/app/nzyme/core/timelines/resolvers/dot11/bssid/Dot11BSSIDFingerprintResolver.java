package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.timelines.resolvers.helpers.StringSetComparison;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11BSSIDFingerprintResolver extends TimelineResolver {

    public Dot11BSSIDFingerprintResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(String bssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        HashSet<String> currentFingerprints = Sets.newHashSet(nzyme.getDot11().findFingerprintsOfBSSID(
                bssid, TimeRange.create(now.minusMinutes(EVENT_HORIZON_MINUTES), now, false), taps)
        );

        if (currentFingerprints.isEmpty()) {
            return Optional.empty();
        }

        Optional<Set<String>> previousFingerprints = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_BSSID,
                        bssid,
                        TimelineEventType.DOT11_BSSID_FINGERPRINT_DIFF)
                .map(event -> {
                    Map<String, Object> details = objectMapper.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<String> known = (Collection<String>) details.getOrDefault(
                            "known_fingerprints", Collections.emptyList());
                    return new HashSet<>(known);
                });

        return StringSetComparison.compare(
                previousFingerprints,
                currentFingerprints,
                "new_fingerprints",
                "disappeared_fingerprints",
                "known_fingerprints"
        );
    }


}
