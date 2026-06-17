package app.nzyme.core.timelines.resolvers.dot11.ssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDDetails;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.timelines.resolvers.helpers.DoubleSetComparison;
import app.nzyme.core.timelines.tasks.Dot11SSIDTimelineCalculationTaskHandler;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;
import static app.nzyme.core.timelines.tasks.Dot11SSIDTimelineCalculationTaskHandler.buildSSIDKey;

public class Dot11SSIDRatesResolver extends TimelineResolver {

    public Dot11SSIDRatesResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(SSIDDetails ssidDetails, String bssid, String ssid) {
        Set<Double> currentRates = Sets.newHashSet(ssidDetails.rates());

        if (currentRates.isEmpty()) {
            return Optional.empty();
        }

        Optional<Set<Double>> previousRates = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_SSID,
                        buildSSIDKey(bssid, ssid),
                        TimelineEventType.DOT11_SSID_RATES_DIFF)
                .map(event -> {
                    Map<String, Object> details = objectMapper.readValue(event.eventDetails(), new TypeReference<>() {});
                    @SuppressWarnings("unchecked")
                    Collection<Double> known = (Collection<Double>) details.getOrDefault(
                            "known_rates", Collections.emptyList());
                    return new HashSet<>(known);
                });

        return DoubleSetComparison.compare(
                previousRates,
                currentRates,
                "new_rates",
                "disappeared_rates",
                "known_rates"
        );
    }

}
