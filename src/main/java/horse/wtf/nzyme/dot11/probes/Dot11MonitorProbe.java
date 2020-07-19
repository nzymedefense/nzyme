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

package horse.wtf.nzyme.dot11.probes;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.UplinkHandler;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.*;
import horse.wtf.nzyme.dot11.handlers.*;
import horse.wtf.nzyme.dot11.parsers.*;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.pcap4j.core.*;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.packet.namednumber.Dot11FrameType;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11MonitorProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    private final Dot11ProbeConfiguration configuration;

    private PcapHandle pcap;
    private final ChannelHopper channelHopper;

    private final Statistics statistics;

    // Interceptors.
    private final List<Dot11FrameInterceptor> frameInterceptors;

    // Parsers.
    private final Dot11BeaconFrameParser beaconParser;
    private final Dot11AssociationRequestFrameParser associationRequestParser;
    private final Dot11AssociationResponseFrameParser associationResponseParser;
    private final Dot11ProbeRequestFrameParser probeRequestParser;
    private final Dot11ProbeResponseFrameParser probeResponseFrameParser;
    private final Dot11DisassociationFrameParser disassociationParser;
    private final Dot11AuthenticationFrameParser authenticationFrameParser;
    private final Dot11DeauthenticationFrameParser deauthenticationFrameParser;

    // Metrics
    private final Meter globalFrameMeter;
    private final Timer globalFrameTimer;
    private final Meter localFrameMeter;

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private DateTime mostRecentFrameTimestamp;

    public Dot11MonitorProbe(Dot11ProbeConfiguration configuration, MetricRegistry metrics, Statistics statistics, Anonymizer anonymizer) {
        super(configuration, statistics, metrics);

        this.statistics = statistics;

        this.configuration = configuration;
        this.frameInterceptors = Lists.newArrayList();

        // Parsers.
        beaconParser = new Dot11BeaconFrameParser(metrics, anonymizer);
        associationRequestParser = new Dot11AssociationRequestFrameParser(metrics, anonymizer);
        associationResponseParser = new Dot11AssociationResponseFrameParser(metrics, anonymizer);
        probeRequestParser = new Dot11ProbeRequestFrameParser(metrics, anonymizer);
        probeResponseFrameParser = new Dot11ProbeResponseFrameParser(metrics, anonymizer);
        disassociationParser = new Dot11DisassociationFrameParser(metrics, anonymizer);
        authenticationFrameParser = new Dot11AuthenticationFrameParser(metrics, anonymizer);
        deauthenticationFrameParser = new Dot11DeauthenticationFrameParser(metrics, anonymizer);

        // Metrics.
        this.globalFrameMeter = metrics.meter(MetricNames.FRAME_COUNT);
        this.globalFrameTimer = metrics.timer(MetricNames.FRAME_TIMER);
        this.localFrameMeter = metrics.meter(MetricRegistry.name(this.getClass(), this.getName(), "frameCount"));

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

        LOG.info("Building PCAP handle on interface [{}]", configuration.networkInterfaceName());

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
        final Dot11Probe probe = this;

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
                                LOG.trace("Bad checksum. Skipping malformed packet.");
                                statistics.tickMalformedCountAndNotify(meta);
                                continue;
                            }

                            Dot11FrameType type = Dot11FrameType.getInstance(
                                    (byte) (((payload[0] << 2) & 0x30) | ((payload[0] >> 4) & 0x0F))
                            );

                            mostRecentFrameTimestamp = DateTime.now();

                            // Intercept and handle frame. TODO this looks fucked from a logic perspective
                            for (Dot11FrameInterceptor interceptor : frameInterceptors) {
                                if (interceptor.forSubtype() == type.value()) {
                                    try {
                                        switch (type.value()) {
                                            case Dot11FrameSubtype.ASSOCIATION_REQUEST:
                                                interceptor.intercept(associationRequestParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.ASSOCIATION_RESPONSE:
                                                interceptor.intercept(associationResponseParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.PROBE_REQUEST:
                                                interceptor.intercept(probeRequestParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.PROBE_RESPONSE:
                                                interceptor.intercept(probeResponseFrameParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.BEACON:
                                                interceptor.intercept(beaconParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.DISASSOCIATION:
                                                interceptor.intercept(disassociationParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.AUTHENTICATION:
                                                interceptor.intercept(authenticationFrameParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            case Dot11FrameSubtype.DEAUTHENTICATION:
                                                interceptor.intercept(deauthenticationFrameParser.parse(payload, r.getHeader().getRawData(), meta));
                                                break;
                                            default:
                                                LOG.error("Not parsing unsupported management frame subtype [{}].", type.value());
                                        }
                                    } catch (MalformedFrameException e) {
                                        LOG.info("Skipping malformed frame.", e);
                                        getStatistics().tickMalformedCountAndNotify(meta);
                                    }
                                }
                            }

                            statistics.tickFrameCount(meta);
                            time.stop();
                        }
                    } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException | IllegalRawDataException e) {
                        LOG.debug("Illegal data received.", e);
                    } catch(Exception e) {
                        LOG.error("Could not process packet.", e);
                    }
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
        if (localFrameMeter != null) {
            return localFrameMeter.getCount();
        } else {
            return -1L;
        }
    }

    @Override
    public void addFrameInterceptor(Dot11FrameInterceptor interceptor) {
        frameInterceptors.add(interceptor);
    }

    @Override
    public List<Dot11FrameInterceptor> getInterceptors() {
        return frameInterceptors;
    }

    @Override
    @Nullable
    public DateTime getMostRecentFrameTimestamp() {
        return mostRecentFrameTimestamp;
    }

    public void onChannelSwitch(ChannelHopper.ChannelSwitchHandler handler) {
        channelHopper.onChannelSwitch(handler);
    }

    public static void configureAsBroadMonitor(final Dot11MonitorProbe probe, UplinkHandler uplink) {
        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AssociationRequestFrame>() {
            private final Dot11FrameHandler<Dot11AssociationRequestFrame> handler = new Dot11AssociationRequestFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11AssociationRequestFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_REQUEST;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AssociationResponseFrame>() {
            private final Dot11FrameHandler<Dot11AssociationResponseFrame> handler = new Dot11AssociationResponseFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11AssociationResponseFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11ProbeRequestFrame>() {
            private final Dot11FrameHandler<Dot11ProbeRequestFrame> handler = new Dot11ProbeRequestFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11ProbeRequestFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_REQUEST;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            private final Dot11FrameHandler<Dot11ProbeResponseFrame> handler = new Dot11ProbeResponseFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            private final Dot11FrameHandler<Dot11BeaconFrame> handler = new Dot11BeaconFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11BeaconFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11DisassociationFrame>() {
            private final Dot11FrameHandler<Dot11DisassociationFrame> handler = new Dot11DisassociationFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11DisassociationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DISASSOCIATION;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AuthenticationFrame>() {
            private final Dot11FrameHandler<Dot11AuthenticationFrame> handler = new Dot11AuthenticationFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11AuthenticationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.AUTHENTICATION;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11DeauthenticationFrame>() {
            private final Dot11FrameHandler<Dot11DeauthenticationFrame> handler = new Dot11DeauthenticationFrameHandler(probe, uplink);

            @Override
            public void intercept(Dot11DeauthenticationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DEAUTHENTICATION;
            }

            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });
    }

}
