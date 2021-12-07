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

package horse.wtf.nzyme.dot11.deauth;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.DeauthFloodAlert;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DeauthenticationMonitor {

    private static final Logger LOG = LogManager.getLogger(DeauthenticationMonitor.class);

    private final NzymeLeader nzyme;

    private final AtomicInteger counter;
    private final ScheduledExecutorService service;

    public DeauthenticationMonitor(NzymeLeader nzyme) {
        this(nzyme, 60);
    }

    public DeauthenticationMonitor(NzymeLeader nzyme, int syncIntervalSeconds) {
        this.nzyme = nzyme;
        this.counter = new AtomicInteger(0);

        // Regularly delete networks that have not been seen for a while.
        service = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("deauthmonitor")
                        .build()
        );

        service.scheduleAtFixedRate(this::run, syncIntervalSeconds, syncIntervalSeconds, TimeUnit.SECONDS);
    }

    protected void run() {
        try {
            int count = this.counter.get();
            this.counter.set(0);

            // Check if we need to alert if enabled.
            if (nzyme.getConfiguration().deauth() != null && nzyme.getConfiguration().dot11Alerts().contains(Alert.TYPE_WIDE.DEAUTH_FLOOD)) {
                int threshold = nzyme.getConfiguration().deauth().globalThreshold();
                if (count > threshold) {
                    nzyme.getAlertsService().handle(DeauthFloodAlert.create(DateTime.now(), count, threshold));
                }
            } else {
                LOG.debug("DEAUTH_FLOOD alert disabled or not fully configured. Not checking.");
            }

            // Write count to database.
            nzyme.getDatabase().useHandle(handle -> handle.createUpdate(
                    "INSERT INTO deauth_monitor(total_frame_count, created_at) VALUES(:frame_count, :created_at)")
                    .bind("frame_count", count)
                    .bind("created_at", DateTime.now().withMillisOfSecond(0))
                    .execute()
            );
        } catch(Exception e) {
            LOG.error("Could not run deauthentication monitor.", e);
        }
    }

    public void stop() {
        service.shutdown();
    }

    public long currentCount() {
        return this.counter.get();
    }

    public void record(Dot11DeauthenticationFrame frame) {
        this.counter.incrementAndGet();
    }

    public void record(Dot11DisassociationFrame frame) {
        this.counter.incrementAndGet();
    }

}
