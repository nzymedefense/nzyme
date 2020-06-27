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

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.BanditListProvider;
import horse.wtf.nzyme.bandits.TrackTimeout;
import horse.wtf.nzyme.bandits.engine.ContactIdentifierEngine;
import horse.wtf.nzyme.bandits.engine.ContactIdentifierProcess;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifierFactory;
import horse.wtf.nzyme.bandits.trackers.BanditManagerEntry;
import horse.wtf.nzyme.bandits.trackers.TrackerTrackSummary;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.banditfile.BanditFile;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.banditfile.BanditFileIdentifierRecord;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.banditfile.BanditFileRecord;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrackerBanditManager implements BanditListProvider, ContactIdentifierProcess {

    private static final Logger LOG = LogManager.getLogger(TrackerBanditManager.class);

    private static final String BANDITFILE_NAME = "banditfile";

    private final NzymeTracker nzyme;

    private Map<UUID, BanditManagerEntry> bandits;
    private Bandit currentlyTrackedBandit;

    private final ContactIdentifierEngine identifierEngine;

    private final List<InitialTrackHandler> initialTrackHandlers;
    private final List<BanditTraceHandler> traceHandlers;

    private final Object mutex = new Object();

    private UUID currentTrack;
    private long currentTrackFrameCount = 0;
    private DateTime currentTrackLastContact;
    private int currentTrackSignal = 0;

    public TrackerBanditManager(NzymeTracker nzyme) {
        this.nzyme = nzyme;
        this.bandits = Maps.newHashMap();

        this.initialTrackHandlers = Lists.newArrayList();
        this.traceHandlers = Lists.newArrayList();

        loadFromBanditFile();

        this.identifierEngine = new ContactIdentifierEngine(nzyme.getMetrics());

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("bandit-retention-%d")
                .build())
                .scheduleWithFixedDelay(() -> {
                    synchronized (mutex) {
                        Map<UUID, BanditManagerEntry> newBandits = Maps.newHashMap();
                        for (Map.Entry<UUID, BanditManagerEntry> bandit : bandits.entrySet()) {
                            if (bandit.getValue().receivedAt().isAfter(DateTime.now().minusMinutes(3))) {
                                newBandits.put(bandit.getKey(), bandit.getValue());
                            } else {
                                LOG.info("Retention cleaning outdated bandit <{}/{}>",
                                        bandit.getValue().bandit().uuid(), bandit.getValue().bandit().name());
                            }
                        }
                        bandits = newBandits;
                    }
                }, 10, 10, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("contact-retention-%d")
                .build())
                .scheduleWithFixedDelay(() -> {
                    if (isCurrentlyTracking() && currentTrackLastContact.isBefore(DateTime.now().minusMinutes(TrackTimeout.MINUTES))) {
                        LOG.info("Track timeout exceeded. Resetting.");
                        resetCurrentTrack();
                    }
                }, 10, 10, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("banditfile-writer-%d")
                .build())
                .scheduleWithFixedDelay(this::writeBanditFile, 10, 10, TimeUnit.SECONDS);
    }

    private void writeBanditFile() {
        if (getBanditList().isEmpty()) {
            return;
        }

        List<BanditFileRecord> banditFileRecords = Lists.newArrayList();

        for (Bandit bandit : getBanditList()) {
            List<BanditFileIdentifierRecord> identifiers = Lists.newArrayList();

            if (bandit.identifiers() != null) {
                for (BanditIdentifier identifier : bandit.identifiers()) {
                    identifiers.add(BanditFileIdentifierRecord.create(
                            identifier.getUuid().toString(),
                            identifier.getType().toString(),
                            identifier.configuration()
                    ));
                }
            }

            banditFileRecords.add(BanditFileRecord.create(
                    bandit.uuid().toString(),
                    identifiers
            ));
        }


        BanditFile banditFile = BanditFile.create(
                DateTime.now().toString(),
                banditFileRecords
        );

        try {
            //noinspection UnstableApiUsage
            Files.write(
                    nzyme.getObjectMapper().writeValueAsBytes(banditFile),
                    new File(nzyme.getConfiguration().dataDirectory() + "/" + BANDITFILE_NAME)
            );
        } catch(Exception e) {
            LOG.error("Could not write banditfile.", e);
        }
    }

    private void loadFromBanditFile() {
        File banditFile = new File(nzyme.getConfiguration().dataDirectory() + "/" + BANDITFILE_NAME);

        if (!banditFile.exists()) {
            LOG.info("No banditfile found.");
            return;
        }

        try {
            //noinspection UnstableApiUsage
            String content = new String(Files.toByteArray(banditFile), Charsets.UTF_8);

            BanditFile parsed = nzyme.getObjectMapper().readValue(content, BanditFile.class);
            LOG.info("Replaying banditfile from [{}]", parsed.createdAt());

            for (BanditFileRecord bandit : parsed.bandits()) {
                List<BanditIdentifier> identifiers = Lists.newArrayList();
                for (BanditFileIdentifierRecord identifier : bandit.identifiers()) {
                    identifiers.add(BanditIdentifierFactory.create(
                            BanditIdentifier.TYPE.valueOf(identifier.type()),
                            identifier.configuration(),
                            null,
                            UUID.fromString(identifier.uuid())
                    ));
                }

                registerBandit(buildBandit(UUID.fromString(bandit.uuid()), identifiers));
            }

        } catch (Exception e) {
            LOG.error("Banditfile exists but failed to parse.", e);
            return;
        }
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

    public void setCurrentlyTrackedBandit(UUID banditUUID) {
        if (!bandits.containsKey(banditUUID)) {
            LOG.error("Cannot track bandit [{}]. Bandit not found in local list of bandits.", banditUUID);
            return;
        }

        if (currentlyTrackedBandit != null && currentlyTrackedBandit.uuid().equals(banditUUID)) {
            LOG.info("Already tracking bandit [{}].", banditUUID);
        }

        LOG.info("Setting currently tracked bandit to [{}].", banditUUID);
        this.currentlyTrackedBandit = bandits.get(banditUUID).bandit();
    }

    public boolean isCurrentlyTracking() {
        return currentlyTrackedBandit != null;
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

            registerBandit(buildBandit(UUID.fromString(broadcast.getUuid()), identifiers));
        } catch (BanditIdentifierFactory.NoSerializerException | BanditIdentifierFactory.MappingException | IOException e) {
            LOG.error("Invalid bandit identifier payload in bandit broadcast.", e);
        }
    }

    private Bandit buildBandit(UUID uuid, List<BanditIdentifier> identifiers) {
        return Bandit.create(
                null, uuid, "n/a", "n/a", false, DateTime.now(), DateTime.now(), identifiers
        );
    }

    public void cancelTracking() {
        if (!isCurrentlyTracking()) {
            return;
        }

        LOG.info("Canceling tracking of bandit.");
        this.currentlyTrackedBandit = null;

        resetCurrentTrack();
    }

    public void registerBandit(Bandit bandit) {
        synchronized (mutex) {
            // We either override an existing bandit (and by that update it's receive time) or create a new one.
            bandits.put(bandit.uuid(), BanditManagerEntry.create(bandit, DateTime.now()));
        }
    }

    private void resetCurrentTrack() {
        LOG.info("Resetting current track.");
        this.currentTrack = null;
        this.currentTrackFrameCount = 0;
        this.currentTrackLastContact = null;
    }

    @Override
    public void identify(Dot11Frame frame) {
        if (!isCurrentlyTracking()) {
            return;
        }

        Bandit bandit = getCurrentlyTrackedBandit();
        if (bandit.identifiers() != null && !bandit.identifiers().isEmpty()) {
            if (identifierEngine.identify(frame, bandit)) {
                if (currentTrack == null) {
                    // New track!
                    currentTrack = UUID.randomUUID();

                    for (InitialTrackHandler handler : initialTrackHandlers) {
                        handler.handle(bandit);
                    }

                    LOG.info("Initial contact with tracked bandit. Starting track [{}].", currentTrack.toString().substring(0,6));
                }

                currentTrackFrameCount++;
                currentTrackSignal = frame.meta().getAntennaSignal();
                currentTrackLastContact = DateTime.now();

                for (BanditTraceHandler handler : traceHandlers) {
                    handler.handle(bandit, frame.meta().getAntennaSignal());
                }
            }
        }
    }

    public boolean hasActiveTrack() {
        return isCurrentlyTracking() && currentTrack != null;
    }

    @Nullable
    public TrackerTrackSummary getTrackSummary() {
        if (!hasActiveTrack()) {
            return null;
        } else {
            return TrackerTrackSummary.create(currentTrack, currentTrackLastContact, currentTrackSignal, currentTrackFrameCount);
        }
    }

    public void onInitialTrack(InitialTrackHandler handler) {
        this.initialTrackHandlers.add(handler);
    }

    public void onBanditTrace(BanditTraceHandler handler) {
        this.traceHandlers.add(handler);
    }

    public interface InitialTrackHandler {
        void handle(Bandit bandit);
    }

    public interface BanditTraceHandler {
        void handle(Bandit bandit, int rssi);
    }
}
