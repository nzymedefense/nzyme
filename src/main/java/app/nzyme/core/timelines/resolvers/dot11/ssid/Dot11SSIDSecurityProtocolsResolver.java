package app.nzyme.core.timelines.resolvers.dot11.ssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDDetails;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.timelines.resolvers.helpers.StringSetComparison;
import com.google.common.collect.Sets;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static app.nzyme.core.timelines.tasks.Dot11SSIDTimelineCalculationTaskHandler.buildSSIDKey;

public class Dot11SSIDSecurityProtocolsResolver extends TimelineResolver {

    public Dot11SSIDSecurityProtocolsResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(SSIDDetails ssidDetails, String bssid, String ssid) {
        HashSet<String> currentProtocols = Sets.newHashSet(ssidDetails.securityProtocols());

        if (currentProtocols.isEmpty()) {
            return Optional.empty();
        }

        Optional<Set<String>> previousProtocols = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_SSID,
                        buildSSIDKey(bssid, ssid),
                        TimelineEventType.DOT11_SSID_SECURITY_PROTOCOLS_DIFF)
                .map(event -> {
                    Map<String, Object> details = objectMapper.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<String> known = (Collection<String>) details.getOrDefault(
                            "known_protocols", Collections.emptyList());
                    return new HashSet<>(known);
                });

        return StringSetComparison.compare(
                previousProtocols,
                currentProtocols,
                "new_protocols",
                "disappeared_protocols",
                "known_protocols"
        );
    }

}
