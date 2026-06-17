package app.nzyme.core.timelines.resolvers.dot11.ssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.SSIDDetails;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.timelines.resolvers.helpers.StringSetComparison;
import tools.jackson.core.type.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

import static app.nzyme.core.timelines.tasks.Dot11SSIDTimelineCalculationTaskHandler.buildSSIDKey;


public class Dot11SSIDSecuritySuitesResolver extends TimelineResolver {

    public Dot11SSIDSecuritySuitesResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(SSIDDetails ssidDetails, String bssid, String ssid) {
        Set<String> currentSuites = ssidDetails
                .securitySuites()
                .stream()
                .map(Dot11::securitySuitesToIdentifier)
                .collect(Collectors.toSet());

        if (currentSuites.isEmpty()) {
            return Optional.empty();
        }

        Optional<Set<String>> previousSuites = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_SSID,
                        buildSSIDKey(bssid, ssid),
                        TimelineEventType.DOT11_SSID_SECURITY_SUITES_DIFF)
                .map(event -> {
                    Map<String, Object> details = objectMapper.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<String> known = (Collection<String>) details.getOrDefault(
                            "known_suites", Collections.emptyList());
                    return new HashSet<>(known);
                });

        return StringSetComparison.compare(
                previousSuites,
                currentSuites,
                "new_suites",
                "disappeared_suites",
                "known_suites"
        );
    }

}
