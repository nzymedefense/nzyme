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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
                    try {
                        List<Bandit> shuffledBandits = Lists.newArrayList(nzyme.getContactIdentifier().getBandits().values());
                        Collections.shuffle(shuffledBandits);

                        for (Bandit bandit : shuffledBandits) {
                            LOG.debug("Broadcasting bandit [{}/{}] via GroundStation.", bandit.uuid(), bandit.name());
                            TrackerMessage.BanditBroadcast.Builder builder = TrackerMessage.BanditBroadcast.newBuilder()
                                    .setSource(nzyme.getConfiguration().nzymeId())
                                    .setUuid(bandit.uuid().toString());

                            if (bandit.identifiers() != null) {
                                for (BanditIdentifier identifier : bandit.identifiers()) {
                                    TrackerMessage.ContactIdentifier.Builder idBuilder = TrackerMessage.ContactIdentifier.newBuilder()
                                            .setType(identifier.getType().toString())
                                            .setUuid(identifier.getUuid().toString());

                                    for (Map.Entry<String, Object> config : identifier.configuration().entrySet()) {
                                        Object value;
                                        if (config.getValue() instanceof List) {
                                            value = "nzl:" + nzyme.getObjectMapper().writeValueAsString(config.getValue());
                                        } else if(config.getValue() instanceof Map) {
                                            value = "nzm:" + nzyme.getObjectMapper().writeValueAsString(config.getValue());
                                        } else {
                                            value = config.getValue();
                                        }
                                        String configRep = config.getKey() + ":\"" + value + "\"";
                                        idBuilder.addConfiguration(configRep);
                                    }

                                    builder.addIdentifier(idBuilder.build());
                                }
                            }

                            nzyme.getGroundStation().transmit(
                                    TrackerMessage.Wrapper.newBuilder().setBanditBroadcast(builder.build()).build()
                            );
                        }
                    } catch(Exception e) {
                        LOG.error("Could not broadcast bandit.", e);
                    }
                }, 0, 1, TimeUnit.MINUTES);
    }

}
