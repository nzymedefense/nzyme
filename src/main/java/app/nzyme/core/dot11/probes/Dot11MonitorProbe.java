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

package app.nzyme.core.dot11.probes;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Joiner;
import app.nzyme.core.RemoteConnector;
import app.nzyme.core.bandits.trackers.trackerlogic.ChannelDesignator;
import app.nzyme.core.channels.ChannelHopper;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.frames.Dot11FrameFactory;
import app.nzyme.core.notifications.FieldNames;
import app.nzyme.core.notifications.Notification;
import app.nzyme.core.processing.FrameProcessor;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.packet.namednumber.Dot11FrameType;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11MonitorProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11MonitorProbe.class);

    private final Dot11ProbeConfiguration configuration;

    private PcapHandle pcap;
    private final ChannelHopper channelHopper;
    private final ChannelDesignator channelDesignator;
    private final RemoteConnector remote;

    private final Dot11FrameFactory frameFactory;
    private final FrameProcessor frameProcessor;

    // Metrics
    private final Meter globalFrameMeter;
    private final Timer globalFrameTimer;
    private final Meter localFrameMeter;

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private DateTime mostRecentFrameTimestamp;

    public Dot11MonitorProbe(Dot11ProbeConfiguration configuration, FrameProcessor frameProcessor, MetricRegistry metrics, Anonymizer anonymizer, RemoteConnector remote, boolean hasDesignator) {
        super(configuration, metrics);

        this.remote = remote;
        this.configuration = configuration;

        this.frameFactory = new Dot11FrameFactory(metrics, anonymizer);
        this.frameProcessor = frameProcessor;

        // Metrics.
        this.globalFrameMeter = metrics.meter(MetricNames.FRAME_COUNT);
        this.globalFrameTimer = metrics.timer(MetricNames.FRAME_TIMER);
        this.localFrameMeter = metrics.meter(MetricRegistry.name(this.getClass(), this.getName(), "frameCount"));

        channelHopper = new ChannelHopper(this, configuration);
        channelHopper.initialize();

        channelDesignator = hasDesignator ? new ChannelDesignator(this) : null;
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

        LOG.info("Building PCAP handle on interface [{}]", configuration.networkInterfaceName());

        PcapHandle.Builder phb = new PcapHandle.Builder(networkInterface.getName())
                .rfmon(!configuration.skipEnableMonitor())
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
            LOG.info("Commencing 802.11 frame processing on [{}] ... (⌐■_■)–︻╦╤─ – – pew pew", configuration.networkInterfaceName());

            while (true) {
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

                // We are in the loop and active if we reach here.
                inLoop.set(true);

                Packet packet;

                try {
                    packet = pcap.getNextPacketEx();
                } catch (NotOpenException | PcapNativeException | EOFException e) {
                    inLoop.set(false);
                    LOG.error(e);
                    continue;
                } catch (TimeoutException e) {
                    // This happens all the time when waiting for packets.
                    continue;
                } catch (IllegalArgumentException e) {
                    // This is a symptom of malformed data.
                    LOG.trace(e);
                    continue;
                }

                if (packet != null) {
                    this.globalFrameMeter.mark();
                    this.localFrameMeter.mark();

                    try {
                        if (packet instanceof RadiotapPacket) {
                            Timer.Context time = this.globalFrameTimer.time();

                            RadiotapPacket r = (RadiotapPacket) packet;
                            byte[] payload = r.getPayload().getRawData();

                            Dot11MetaInformation meta = Dot11MetaInformation.parse(r.getHeader().getDataFields());

                            if (meta.isMalformed()) {
                                LOG.trace("Bad checksum. Skipping malformed packet on probe [{}].", getName());
                                notifyOfMalformedFrame(meta);
                                continue;
                            }

                            Dot11FrameType type = Dot11FrameType.getInstance(
                                    (byte) (((payload[0] << 2) & 0x30) | ((payload[0] >> 4) & 0x0F))
                            );

                            mostRecentFrameTimestamp = DateTime.now();

                            // Intercept and handle frame.
                            frameProcessor.processDot11Frame(frameFactory.build(type, payload, r.getHeader().getRawData(), meta));

                            time.stop();
                        }
                    } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException | MalformedFrameException e) {
                        LOG.debug("Illegal data received on probe [{}].", getName(), e);
                    } catch(Exception e) {
                        LOG.error("Could not process packet on probe [{}].", getName(), e);
                    }
                }
            }
        };
    }

    public ChannelDesignator getChannelDesignator() {
        return channelDesignator;
    }

    public ChannelHopper getChannelHopper() {
        return channelHopper;
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
        if (localFrameMeter != null) {
            return localFrameMeter.getCount();
        } else {
            return -1L;
        }
    }

    @Override
    @Nullable
    public DateTime getMostRecentFrameTimestamp() {
        return mostRecentFrameTimestamp;
    }

    public void onChannelSwitch(ChannelHopper.ChannelSwitchHandler handler) {
        channelHopper.onChannelSwitch(handler);
    }

    private void notifyOfMalformedFrame(Dot11MetaInformation meta) {
        int channel = 0;
        if(meta != null) {
            channel = meta.getChannel();
        }

        remote.notifyUplinks(
                new Notification("Malformed frame received.", channel)
                        .addField(FieldNames.SUBTYPE, "malformed"), meta);
    }

}
