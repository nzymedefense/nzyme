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

package horse.wtf.nzyme.dot11.probes;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.dot11.deception.traps.Trap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.pcap4j.core.*;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11SenderProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11SenderProbe.class);

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private PcapHandle pcap;
    private final Dot11ProbeConfiguration configuration;

    private final ChannelHopper channelHopper;
    private final Trap trap;

    private long totalFrames;

    private DateTime mostRecentFrameTimestamp;

    public Dot11SenderProbe(Dot11ProbeConfiguration configuration, Trap trap, MetricRegistry metrics) {
        super(configuration, metrics);

        this.trap = trap;
        this.configuration = configuration;

        this.totalFrames = 0;

        channelHopper = new ChannelHopper(this, configuration);
        channelHopper.initialize();
    }

    @Override
    public void initialize() throws Dot11ProbeInitializationException {
        // Get network interface for PCAP.
        PcapNetworkInterface networkInterface;
        try {
            networkInterface = Pcaps.getDevByName(configuration.networkInterfaceName());
        } catch (PcapNativeException e) {
            throw new Dot11ProbeInitializationException("Could not get network interface [" + configuration.networkInterfaceName() + "].", e);
        }

        if (networkInterface == null) {
            throw new Dot11ProbeInitializationException("Could not get network interface [" + configuration.networkInterfaceName() + "]. Does it exist and could it be that you have to be root? Is it up?");
        }

        PcapHandle.Builder phb = new PcapHandle.Builder(networkInterface.getName())
                .rfmon(true)
                .snaplen(65536)
                .promiscuousMode(PcapNetworkInterface.PromiscuousMode.PROMISCUOUS)
                .timeoutMillis(100)
                .bufferSize(5 * 1024 * 1024)
                .timestampPrecision(PcapHandle.TimestampPrecision.MICRO);

        try {
            this.pcap = phb.build();
            this.pcap.setFilter(
                    "type mgt and (subtype deauth or subtype probe-req or subtype probe-resp or subtype beacon or subtype assoc-req or subtype assoc-resp or subtype disassoc or subtype auth)",
                    BpfProgram.BpfCompileMode.OPTIMIZE
            );
        } catch (Exception e) {
            throw new Dot11ProbeInitializationException("Could not build PCAP handle.", e);
        }

        LOG.info("PCAP handle for [{}] acquired. Cycling through channels <{}>.", configuration.probeName(), Joiner.on(",").join(configuration.channels()));
    }

    @Override
    public Runnable loop() {
        return () -> {
            while(true) {
                try {
                    if(!isInLoop()) {
                        initialize();
                    }
                } catch (Dot11ProbeInitializationException e) {
                    inLoop.set(false);

                    LOG.error("Could not initialize probe [{}]. Retrying soon.", this.getName(), e);
                    // Try again with delay.
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ex) { /* noop */ }

                    continue;
                }

                try {
                    inLoop.set(true);

                    boolean status = trap.run();
                    totalFrames += trap.framesPerExecution();

                    if (status) {
                        mostRecentFrameTimestamp = DateTime.now();
                    }
                } catch(Exception e) {
                    inLoop.set(false);
                    LOG.error("Could not set trap [{}].", this.trap.getClass().getCanonicalName(), e);
                } finally {
                    try {
                        Thread.sleep(trap.getDelayMilliseconds());
                    } catch (InterruptedException e) { }
                }
            }
        };
    }

    @Override
    public boolean isInLoop() {
        return inLoop.get();
    }

    @Override
    public Integer getCurrentChannel() {
        return channelHopper.getCurrentChannel();
    }

    @Override
    public Long getTotalFrames() {
        return totalFrames;
    }

    public Trap getTrap() {
        return trap;
    }

    @Override
    @Nullable
    public DateTime getMostRecentFrameTimestamp() {
        return mostRecentFrameTimestamp;
    }

}
