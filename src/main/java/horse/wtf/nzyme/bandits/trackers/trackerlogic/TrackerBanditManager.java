/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.bandits.trackers.trackerlogic;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.BanditListProvider;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifierFactory;
import horse.wtf.nzyme.bandits.trackers.BanditManagerEntry;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrackerBanditManager implements BanditListProvider {

    private static final Logger LOG = LogManager.getLogger(TrackerBanditManager.class);

    private final NzymeTracker nzyme;

    private Map<UUID, BanditManagerEntry> bandits;
    private Bandit currentlyTrackedBandit;

    private final Object mutex = new Object();

    public TrackerBanditManager(NzymeTracker nzyme) {
        this.nzyme = nzyme;
        this.bandits = Maps.newHashMap();

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("bandit-retention-%d")
                .build())
                .scheduleWithFixedDelay(() -> {
                    synchronized (mutex) {
                        Map<UUID, BanditManagerEntry> newBandits = Maps.newHashMap();
                        for (Map.Entry<UUID, BanditManagerEntry> bandit : bandits.entrySet()) {
                            if (bandit.getValue().receivedAt().isAfter(DateTime.now().minusMinutes(3))) { ;
                                newBandits.put(bandit.getKey(), bandit.getValue());
                            } else {
                                LOG.info("Retention cleaning outdated bandit <{}/{}>",
                                        bandit.getValue().bandit().uuid(), bandit.getValue().bandit().name());
                            }
                        }
                        bandits = newBandits;
                    }
                }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public List<Bandit> getBanditList() {
        synchronized (mutex) {
            List<Bandit> result = new ArrayList<>();
            for (BanditManagerEntry bandit : bandits.values()) {
                result.add(bandit.bandit());
            }

            return result;
        }
    }

    @Override
    @Nullable
    public Bandit getCurrentlyTrackedBandit() {
        return currentlyTrackedBandit;
    }

    public void registerBandit(TrackerMessage.BanditBroadcast broadcast) {
        try {
            List<BanditIdentifier> identifiers = Lists.newArrayList();
            for (TrackerMessage.ContactIdentifier broadcastedIdentifier : broadcast.getIdentifierList()) {
                Map<String, Object> parsedConfiguration = Maps.newHashMap();
                for (String s : broadcastedIdentifier.getConfigurationList()) {
                    int cPos = s.indexOf(":");
                    String key = s.substring(0, cPos);
                    String value = s.substring(cPos+2, s.length()-1);
                    if (value.startsWith("nzl:")) {
                        String json = value.substring(4);
                        parsedConfiguration.put(key, nzyme.getObjectMapper().readValue(json, List.class));
                    } else if(value.startsWith("nzm:")) {
                        String json = value.substring(4);
                        parsedConfiguration.put(key, nzyme.getObjectMapper().readValue(json, Map.class));
                    } else {
                        parsedConfiguration.put(key, value);
                    }
                }

                identifiers.add(BanditIdentifierFactory.create(
                        BanditIdentifier.TYPE.valueOf(broadcastedIdentifier.getType()),
                        parsedConfiguration,
                        null,
                        UUID.fromString(broadcastedIdentifier.getUuid())
                ));
            }

            registerBandit(Bandit.create(
                    null, UUID.fromString(broadcast.getUuid()), "n/a", "n/a", false, DateTime.now(), DateTime.now(), identifiers
            ));
        } catch (BanditIdentifierFactory.NoSerializerException | BanditIdentifierFactory.MappingException | IOException e) {
            LOG.error("Invalid bandit identifier payload in bandit broadcast.", e);
        }
    }

    public void setCurrentlyTrackedBandit(UUID banditUUID) {
        if (!bandits.containsKey(banditUUID)) {
            LOG.error("Cannot track bandit [{}]. Bandit not found in local list of bandits.", banditUUID);
            return;
        }

        LOG.info("Setting currently tracked bandit to [{}].", banditUUID);
        this.currentlyTrackedBandit = bandits.get(banditUUID).bandit();
    }

    public void cancelTracking() {
        if (this.currentlyTrackedBandit == null) {
            return;
        }

        LOG.info("Canceling tracking of bandit.");
        this.currentlyTrackedBandit = null;
    }

    public void registerBandit(Bandit bandit) {
        synchronized (mutex) {
            // We either override an existing bandit (and by that update it's receive time) or create a new one.
            bandits.put(bandit.uuid(), BanditManagerEntry.create(bandit, DateTime.now()));
        }
    }

}
