package app.nzyme.core.timelines.resolvers.dot11.ssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.TimelineResolver;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static app.nzyme.core.dot11.Dot11.frequencyToChannel;
import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;
import static app.nzyme.core.timelines.tasks.Dot11SSIDTimelineCalculationTaskHandler.buildSSIDKey;

public class Dot11SSIDActiveChannelResolver extends TimelineResolver {

    public Dot11SSIDActiveChannelResolver(NzymeNode nzyme, UUID organizationId, UUID tenantId) {
        super(nzyme, organizationId, tenantId);
    }

    public Optional<ResolverResult> resolve(String bssid, String ssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        Optional<Integer> currentChannel = nzyme.getDot11().findMostActiveChannelOfSSID(
                TimeRange.create(now.minusMinutes(EVENT_HORIZON_MINUTES), now, false),
                bssid,
                ssid,
                taps
        );

        if (currentChannel.isEmpty()) {
            return Optional.empty();
        }

        Optional<Map<String, Object>> previousChannel = timelines.findLatestEventOfTypeAndAddress(
                        organizationId,
                        tenantId,
                        TimelineAddressType.DOT11_SSID,
                        buildSSIDKey(bssid, ssid),
                        TimelineEventType.DOT11_SSID_ACTIVE_CHANNEL)
                .map(event -> objectMapper.readValue(event.eventDetails(), new TypeReference<>() {}));

        if (previousChannel.isPresent()) {
            int previousChannelFreq = (int) previousChannel.get().get("active_channel_freq");
            if (previousChannelFreq == currentChannel.get()) {
                return Optional.empty();
            }
        }

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("active_channel_freq", currentChannel.get());
        payload.put("active_channel", frequencyToChannel(currentChannel.get()));

        if (previousChannel.isPresent()) {
            payload.put("previous_active_channel_freq", previousChannel.get().get("active_channel_freq"));
            payload.put("previous_active_channel", previousChannel.get().get("active_channel"));
        }

        return Optional.of(ResolverResult.create(payload));
    }
}
