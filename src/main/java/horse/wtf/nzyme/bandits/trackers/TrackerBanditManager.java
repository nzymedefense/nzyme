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

package horse.wtf.nzyme.bandits.trackers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.BanditListProvider;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifierFactory;
import horse.wtf.nzyme.bandits.trackers.payloads.BanditBroadcast;
import horse.wtf.nzyme.bandits.trackers.payloads.BanditIdentifierBroadcast;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

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

    public void registerBandit(TrackerMessage.BanditBroadcast broadcast) {
        try {
            BanditBroadcast payload = nzyme.getObjectMapper().readValue(broadcast.getBandit(), BanditBroadcast.class);

            List<BanditIdentifier> identifiers = Lists.newArrayList();
            for (BanditIdentifierBroadcast broadcastedIdentifier : payload.contactIdentifiers()) {
                identifiers.add(BanditIdentifierFactory.create(
                        broadcastedIdentifier.type(), broadcastedIdentifier.configuration(),null, broadcastedIdentifier.uuid()
                ));
            }

            registerBandit(Bandit.create(
                    null, payload.uuid(), payload.name(), payload.description(), false, DateTime.now(), DateTime.now(), identifiers
            ));
        } catch (IOException e) {
            LOG.error("Could not decode bandit broadcast message payload.", e);
        } catch (BanditIdentifierFactory.NoSerializerException | BanditIdentifierFactory.MappingException e) {
            LOG.error("Invalid bandit identifier payload in bandit broadcast.", e);
        }
    }

    public void registerBandit(Bandit bandit) {
        synchronized (mutex) {
            // We either override an existing bandit (and by that update it's receive time) or create a new one.
            bandits.put(bandit.uuid(), BanditManagerEntry.create(bandit, DateTime.now()));
        }
    }

}
