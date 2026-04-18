package app.nzyme.core.timelines.tasks;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.BSSIDSummary;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.TimelinesRegistryKeys;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.dot11.bssid.Dot11BSSIDStrongestTapResolver;
import app.nzyme.core.timelines.resolvers.dot11.bssid.Dot11BSSIDTimelineSSIDAnnouncementResolver;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.distributed.tasksqueue.ReceivedTask;
import app.nzyme.plugin.distributed.tasksqueue.TaskHandler;
import app.nzyme.plugin.distributed.tasksqueue.TaskProcessingResult;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Dot11BSSIDTimelineCalculationTaskHandler implements TaskHandler {

    private static final Logger LOG = LogManager.getLogger(Dot11BSSIDTimelineCalculationTaskHandler.class);

    private final NzymeNode nzyme;
    private final Timelines timelines;

    public Dot11BSSIDTimelineCalculationTaskHandler(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.timelines = new Timelines(nzyme);
    }

    @Override
    public TaskProcessingResult handle(ReceivedTask task) {
        LOG.debug("Received task {}", task);

        DateTime now = DateTime.now();

        nzyme.getDatabaseCoreRegistry().setValue(
                TimelinesRegistryKeys.TIMELINES_DOT11_BSSIDS_LAST_EXECUTION.key(),
                DateTime.now().toString()
        );

        // For each tenant, find each currently active BSSID and write either a MARK (it's been seen) or an event if there are any.
        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfAllOrganizations()) {
            List<UUID> taps = nzyme.getAuthenticationService()
                    .findAllTapsOfTenant(tenant.organizationUuid(), tenant.uuid())
                    .stream()
                    .map(TapPermissionEntry::uuid).toList();

            for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(
                    TimeRange.create(now.minusMinutes(1), now, false),
                    Filters.create(Maps.newHashMap()),
                    Dot11.BssidOrderColumn.BSSID,
                    OrderDirection.ASC,
                    Integer.MAX_VALUE,
                    0,
                    taps)) {
                try {
                    // SSIDs.
                    Dot11BSSIDTimelineSSIDAnnouncementResolver ssids = new Dot11BSSIDTimelineSSIDAnnouncementResolver(nzyme);
                    Optional<ResolverResult> ssidsResult = ssids.resolve(bssid.bssid(), taps);

                    if (ssidsResult.isPresent()) {
                        timelines.writeDot11TimelineEvent(
                                tenant.organizationUuid(),
                                tenant.uuid(),
                                TimelineAddressType.DOT11_BSSID,
                                bssid.bssid(),
                                TimelineEventType.DOT11_BSSID_SSID_DIFF,
                                ssidsResult.get().payload(),
                                now
                        );
                    }

                    // SSIDs.
                    Dot11BSSIDStrongestTapResolver strongestTap = new Dot11BSSIDStrongestTapResolver(nzyme);
                    Optional<ResolverResult> strongestTapResult = strongestTap.resolve(bssid.bssid(), taps);

                    if (strongestTapResult.isPresent()) {
                        timelines.writeDot11TimelineEvent(
                                tenant.organizationUuid(),
                                tenant.uuid(),
                                TimelineAddressType.DOT11_BSSID,
                                bssid.bssid(),
                                TimelineEventType.DOT11_BSSID_STRONGEST_TAP,
                                strongestTapResult.get().payload(),
                                now
                        );
                    }

                } catch (Exception e) {
                    LOG.error("Error while calculating timeline for 802.11 BSSID [{}]. Skipping.", "FOO", e);
                }
            }
        }

        return TaskProcessingResult.SUCCESS;
    }

    @Override
    public String getName() {
        return "802.11 BSSID Timeline Calculation";
    }


}
