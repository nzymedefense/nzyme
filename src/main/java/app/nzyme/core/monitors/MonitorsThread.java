package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.TenantCacheKey;
import app.nzyme.core.util.TimeRangeFactory;
import app.nzyme.core.util.filters.FilterParser;
import app.nzyme.core.util.filters.Filters;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MonitorsThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitorsThread.class);

    private final NzymeNode nzyme;

    public MonitorsThread(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        Map<TenantCacheKey, List<UUID>> tenantTaps = Maps.newHashMap();

        for (MonitorEntry monitor : nzyme.getMonitors().findAllMonitorsOfAllTenants()) {
            try {
                MonitorType type;
                try {
                    type = MonitorType.valueOf(monitor.type());
                } catch (IllegalArgumentException e) {
                    LOG.error("Unknown monitor type [{}]. Skipping.", monitor.type());
                    continue;
                }
                
                Filters filters = FilterParser.parseFiltersQueryParameter(monitor.filters());
                List<UUID> taps;
                if (monitor.taps() != null) {
                    taps = monitor.taps();
                } else {
                    TenantCacheKey tenantCacheKey = TenantCacheKey.create(monitor.organizationId(), monitor.tenantId());
                    if (tenantTaps.containsKey(tenantCacheKey)) {
                        taps = tenantTaps.get(tenantCacheKey);
                    } else {
                        taps = nzyme.getTapManager().findAllTapsOfTenant(monitor.organizationId(), monitor.tenantId())
                                .stream()
                                .map(Tap::uuid)
                                .toList();
                        tenantTaps.put(tenantCacheKey, taps);
                    }
                }

                long count;
                switch (type) {
                    case DOT11_BSSID -> {
                        count = nzyme.getDot11()
                                .countBSSIDs(TimeRangeFactory.relative(monitor.lookback()), filters, taps);
                    }
                    case DOT11_CLIENT -> {
                        // TODO
                        continue;
                    }
                    default -> {
                        LOG.error("Monitor type [{}] is not implemented. Skipping.", monitor.type());
                        continue;
                    }
                }

                if (count > monitor.triggerCondition()) {
                    // TODO trigger alert.
                } else {
                    LOG.debug("Monitor [{}] result count <{}> is below trigger condition <{}>. No alert.",
                            monitor.uuid(), count, monitor.triggerCondition());
                }
            } catch (Exception e) {
                LOG.error("Could not execute monitor [{}]. Skipping.", monitor.uuid(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "MonitorsThread";
    }
}
