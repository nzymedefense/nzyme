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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.trackers.payloads.BanditBroadcast;
import horse.wtf.nzyme.bandits.trackers.payloads.BanditIdentifierBroadcast;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BanditListBroadcaster {

    private static final Logger LOG = LogManager.getLogger(BanditListBroadcaster.class);

    private final NzymeLeader nzyme;

    public BanditListBroadcaster(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("bandit-broadcaster-%d")
                .build())
                .scheduleWithFixedDelay(() -> {
                    List<Bandit> shuffledBandits = Lists.newArrayList(nzyme.getContactIdentifier().getBandits().values());
                    Collections.shuffle(shuffledBandits);

                    for (Bandit bandit : shuffledBandits) {
                        List<BanditIdentifierBroadcast> identifiers = Lists.newArrayList();
                        if (bandit.identifiers() != null) {
                            for (BanditIdentifier identifier : bandit.identifiers()) {
                                identifiers.add(BanditIdentifierBroadcast.create(
                                        identifier.getUuid(),
                                        identifier.getType(),
                                        identifier.configuration())
                                );
                            }
                        }

                        String payload;
                        try {
                            payload = nzyme.getObjectMapper().writeValueAsString(
                                    BanditBroadcast.create(bandit.uuid(), bandit.name(), bandit.description(), identifiers)
                            );

                            LOG.debug("Broadcasting bandit [{}/{}] via GroundStation.", bandit.uuid(), bandit.name());
                            nzyme.getGroundStation().transmit(
                                    TrackerMessage.Wrapper.newBuilder().setBanditBroadcast(
                                            TrackerMessage.BanditBroadcast.newBuilder()
                                                    .setSource(nzyme.getConfiguration().nzymeId())
                                                    .setTimestamp(DateTime.now().getMillis())
                                                    .setBandit(payload)
                                                    .build()
                                    ).build()
                            );
                        } catch (JsonProcessingException e) {
                            LOG.error("Could not build bandit list for broadcast.", e);
                            return;
                        }
                    }
                }, 0, 1, TimeUnit.MINUTES);
    }

}
