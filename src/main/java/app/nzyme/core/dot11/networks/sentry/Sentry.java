/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.dot11.networks.sentry;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.dot11.networks.sentry.db.SentrySSID;
import app.nzyme.core.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.*;

public class Sentry {

    private static final Logger LOG = LogManager.getLogger(Sentry.class);

    private final NzymeLeader nzyme;
    private final ScheduledExecutorService executor;
    private final ConcurrentMap<String, SentrySSID> table;

    public Sentry(NzymeLeader nzyme, int syncInterval) {
        this.nzyme = nzyme;
        this.table = new ConcurrentHashMap<>();

        loadTable();

        executor = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("sentry-scanner")
                        .build()
        );
        executor.scheduleAtFixedRate(this::syncDatabase, syncInterval, syncInterval, TimeUnit.SECONDS);
    }

    private void loadTable() {
        List<SentrySSID> entries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM sentry_ssids")
                        .mapTo(SentrySSID.class)
                        .list()
        );

        LOG.info("Loading <{}> SSIDs from database into sentry table.", entries.size());
        for (SentrySSID entry : entries) {
            this.table.put(entry.ssid(), entry);
        }
    }

    private void syncDatabase() {
        for (SentrySSID entry : table.values()) {
            try {
                long count = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT COUNT(*) FROM sentry_ssids WHERE ssid = :ssid")
                                .bind("ssid", entry.ssid())
                                .mapTo(Long.class)
                                .first());

                if (count == 0) {
                    nzyme.getDatabase().useHandle(handle -> handle.createUpdate(
                            "INSERT INTO sentry_ssids(ssid, first_seen, last_seen) VALUES(:ssid, :first_seen, :last_seen)")
                            .bind("ssid", entry.ssid())
                            .bind("first_seen", entry.firstSeen().withMillisOfSecond(0))
                            .bind("last_seen", entry.lastSeen().withMillisOfSecond(0))
                            .execute()
                    );
                } else {
                    nzyme.getDatabase().useHandle(handle -> handle.createUpdate(
                            "UPDATE sentry_ssids SET last_seen = :last_seen WHERE ssid = :ssid")
                            .bind("last_seen", entry.lastSeen().withMillisOfSecond(0))
                            .bind("ssid", entry.ssid())
                            .execute()
                    );
                }
            } catch(Exception e) {
                LOG.error("Could not sync sentry SSID. Skipping.", e);
            }
        }
    }

    public void tickSSID(String ssid, DateTime date) {
        if (ssid == null || ssid.trim().isEmpty() || !Tools.isHumanlyReadable(ssid)) {
            throw new RuntimeException("Cannot use NULL, empty or non-humanly-readable SSID in Sentry.");
        }

        if (table.containsKey(ssid)) {
            SentrySSID entry = table.get(ssid);
            table.put(ssid, SentrySSID.create(ssid, entry.firstSeen(), date));
        } else {
            table.put(ssid, SentrySSID.create(ssid, date, date));
        }
    }

    public ImmutableList<SentrySSID> getSSIDs() {
        return ImmutableList.copyOf(table.values());
    }

    public ImmutableList<SentrySSID> findSeenToday() {
        ImmutableList.Builder<SentrySSID> result = new ImmutableList.Builder<>();

        for (SentrySSID ssid : getSSIDs()) {
            if (ssid.lastSeen().isAfter(DateTime.now().minusHours(24))) {
                result.add(ssid);
            }
        }

        return result.build();
    }

    public ImmutableList<SentrySSID> findNewToday() {
        ImmutableList.Builder<SentrySSID> result = new ImmutableList.Builder<>();

        // Pull those seen today and check if they were also seen first today.
        for (SentrySSID ssid : findSeenToday()) {
            if (ssid.firstSeen().isAfter(DateTime.now().minusHours(24))) {
                result.add(ssid);
            }
        }

        return result.build();
    }

    public boolean knowsSSID(String ssid) {
        if (ssid == null || ssid.trim().isEmpty() || !Tools.isHumanlyReadable(ssid)) {
            throw new RuntimeException("Cannot use NULL, empty or non-humanly-readable SSID in Sentry.");
        }

        return table.containsKey(ssid);
    }

    public void stop() {
        executor.shutdown();
    }

}
