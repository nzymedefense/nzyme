package app.nzyme.core.timelines.tasks;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.BSSIDSummary;
import app.nzyme.core.dot11.db.SSIDDetails;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.TimelineEventType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.TimelinesRegistryKeys;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.timelines.resolvers.dot11.ssid.*;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.Filters;
import app.nzyme.plugin.distributed.tasksqueue.ReceivedTask;
import app.nzyme.plugin.distributed.tasksqueue.TaskHandler;
import app.nzyme.plugin.distributed.tasksqueue.TaskProcessingResult;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.nzyme.core.timelines.Timelines.EVENT_HORIZON_MINUTES;

public class Dot11SSIDTimelineCalculationTaskHandler implements TaskHandler {

    private static final Logger LOG = LogManager.getLogger(Dot11SSIDTimelineCalculationTaskHandler.class);

    private final NzymeNode nzyme;
    private final Timelines timelines;

    private final Timer totalTimer;
    private final Timer individualTimer;

    public Dot11SSIDTimelineCalculationTaskHandler(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.timelines = new Timelines(nzyme);

        this.totalTimer = nzyme.getMetrics().timer(MetricNames.TIMELINES_SSIDS_TOTAL_CALCULATION_TIMER);
        this.individualTimer = nzyme.getMetrics().timer(MetricNames.TIMELINES_SSIDS_INDIVIDUAL_CALCULATION_TIMER);
    }

    @Override
    public TaskProcessingResult handle(ReceivedTask task) {
        try(Timer.Context ignored = totalTimer.time()) {
            LOG.debug("Received task {}", task);

            DateTime now = DateTime.now();

            nzyme.getDatabaseCoreRegistry().setValue(
                    TimelinesRegistryKeys.TIMELINES_DOT11_BSSIDS_LAST_EXECUTION.key(),
                    DateTime.now().toString()
            );

            /* For each tenant, find each currently active SSID and write either a
             * MARK (it's been seen) or an event if there are any.
             */
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
                    for (String ssid : nzyme.getDot11().findSSIDNamesOfBSSID(
                            TimeRange.create(now.minusMinutes(1), now, false), bssid.bssid(), taps)) {
                        try(Timer.Context ignored2 = individualTimer.time()) {
                            if (ssid.isBlank()) {
                                // Should never happen, but just to make sure.
                                continue;
                            }

                            String ssidKey = buildSSIDKey(bssid.bssid(), ssid);
                            int eventsWritten = 0;

                            Optional<SSIDDetails> ssidDetails = nzyme.getDot11().findSSIDDetails(
                                    TimeRange.create(now.minusMinutes(EVENT_HORIZON_MINUTES), now, false),
                                    bssid.bssid(),
                                    ssid,
                                    taps
                            );

                            if (ssidDetails.isPresent()) {
                                // Active channel.
                                Optional<ResolverResult> activeChannelResult = new Dot11SSIDActiveChannelResolver(
                                        nzyme, tenant.organizationUuid(), tenant.uuid()
                                ).resolve(bssid.bssid(), ssid, taps);

                                if (activeChannelResult.isPresent()) {
                                    timelines.writeDot11TimelineEvent(
                                            tenant.organizationUuid(),
                                            tenant.uuid(),
                                            TimelineAddressType.DOT11_SSID,
                                            ssidKey,
                                            TimelineEventType.DOT11_SSID_ACTIVE_CHANNEL,
                                            activeChannelResult.get().payload(),
                                            now
                                    );
                                    eventsWritten++;
                                }

                                // Rates
                                Optional<ResolverResult> ratesResult = new Dot11SSIDRatesResolver(
                                        nzyme, tenant.organizationUuid(), tenant.uuid()
                                ).resolve(ssidDetails.get(), bssid.bssid(), ssid);

                                if (ratesResult.isPresent()) {
                                    timelines.writeDot11TimelineEvent(
                                            tenant.organizationUuid(),
                                            tenant.uuid(),
                                            TimelineAddressType.DOT11_SSID,
                                            ssidKey,
                                            TimelineEventType.DOT11_SSID_RATES_DIFF,
                                            ratesResult.get().payload(),
                                            now
                                    );
                                    eventsWritten++;
                                }

                                // Security Protocols.
                                Optional<ResolverResult> securityProtocolsResult = new Dot11SSIDSecurityProtocolsResolver(
                                        nzyme, tenant.organizationUuid(), tenant.uuid()
                                ).resolve(ssidDetails.get(), bssid.bssid(), ssid);

                                if (securityProtocolsResult.isPresent()) {
                                    timelines.writeDot11TimelineEvent(
                                            tenant.organizationUuid(),
                                            tenant.uuid(),
                                            TimelineAddressType.DOT11_SSID,
                                            ssidKey,
                                            TimelineEventType.DOT11_SSID_SECURITY_PROTOCOLS_DIFF,
                                            securityProtocolsResult.get().payload(),
                                            now
                                    );
                                    eventsWritten++;
                                }

                                // Security Settings
                                Optional<ResolverResult> securitySuitesResult = new Dot11SSIDSecuritySuitesResolver(
                                        nzyme, tenant.organizationUuid(), tenant.uuid()
                                ).resolve(ssidDetails.get(), bssid.bssid(), ssid);

                                if (securitySuitesResult.isPresent()) {
                                    timelines.writeDot11TimelineEvent(
                                            tenant.organizationUuid(),
                                            tenant.uuid(),
                                            TimelineAddressType.DOT11_SSID,
                                            ssidKey,
                                            TimelineEventType.DOT11_SSID_SECURITY_SUITES_DIFF,
                                            securitySuitesResult.get().payload(),
                                            now
                                    );
                                    eventsWritten++;
                                }

                                // Fingerprints
                                Optional<ResolverResult> fingerprintsResult = new Dot11SSIDFingerprintsResolver(
                                        nzyme, tenant.organizationUuid(), tenant.uuid()
                                ).resolve(ssidDetails.get(), bssid.bssid(), ssid);

                                if (fingerprintsResult.isPresent()) {
                                    timelines.writeDot11TimelineEvent(
                                            tenant.organizationUuid(),
                                            tenant.uuid(),
                                            TimelineAddressType.DOT11_SSID,
                                            ssidKey,
                                            TimelineEventType.DOT11_SSID_FINGERPRINTS_DIFF,
                                            fingerprintsResult.get().payload(),
                                            now
                                    );
                                    eventsWritten++;
                                }
                            }

                            // No events, but mark that the SSID was seen.
                            if (eventsWritten == 0) {
                                timelines.writeDot11TimelineEvent(
                                        tenant.organizationUuid(),
                                        tenant.uuid(),
                                        TimelineAddressType.DOT11_SSID,
                                        ssidKey,
                                        TimelineEventType.MARK,
                                        Collections.emptyMap(),
                                        now
                                );
                            }
                        }
                    }
                }
            }

            return TaskProcessingResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return "802.11 SSID Timeline Calculation";
    }

    public static String buildSSIDKey(String bssid, String ssid) {
        return bssid + "/" + ssid;
    }

}
