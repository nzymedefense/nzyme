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

import com.beust.jcommander.internal.Lists;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.*;
import horse.wtf.nzyme.dot11.handlers.*;
import horse.wtf.nzyme.dot11.parsers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.packet.namednumber.Dot11FrameType;

import java.io.EOFException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11MonitorProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    private final Nzyme nzyme;
    private final Dot11ProbeConfiguration configuration;

    private final PcapHandle pcap;
    private final ChannelHopper channelHopper;

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

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    public Dot11MonitorProbe(Nzyme nzyme, Dot11ProbeConfiguration configuration, MetricRegistry metrics) throws Dot11ProbeInitializationException {
        super(configuration, nzyme);

        this.nzyme = nzyme;
        this.configuration = configuration;
        this.frameInterceptors = Lists.newArrayList();

        // Parsers.
        beaconParser = new Dot11BeaconFrameParser(metrics);
        associationRequestParser = new Dot11AssociationRequestFrameParser(metrics);
        associationResponseParser = new Dot11AssociationResponseFrameParser(metrics);
        probeRequestParser = new Dot11ProbeRequestFrameParser(metrics);
        probeResponseFrameParser = new Dot11ProbeResponseFrameParser(metrics);
        disassociationParser = new Dot11DisassociationFrameParser(metrics);
        authenticationFrameParser = new Dot11AuthenticationFrameParser(metrics);
        deauthenticationFrameParser = new Dot11DeauthenticationFrameParser(metrics);

        // Initialize channel hopper.
        channelHopper = new ChannelHopper(this, configuration);
        channelHopper.initialize();

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

        LOG.info("PCAP handle for [{}] acquired. Cycling through channels <{}>.", configuration.networkInterfaceName(), Joiner.on(",").join(configuration.channels()));
    }

    @Override
    public Runnable loop() {
        final Dot11Probe probe = this;

        return () -> {
            LOG.info("Commencing 802.11 frame processing on [{}] ... (⌐■_■)–︻╦╤─ – – pew pew", configuration.networkInterfaceName());

            inLoop.set(true);
            while (true) {

                Packet packet;

                try {
                    packet = pcap.getNextPacketEx();
                } catch (NotOpenException | PcapNativeException | EOFException e) {
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
                    try {
                        if (packet instanceof RadiotapPacket) {
                            RadiotapPacket r = (RadiotapPacket) packet;
                            byte[] payload = r.getPayload().getRawData();

                            Dot11MetaInformation meta = Dot11MetaInformation.parse(r.getHeader().getDataFields());

                            if (meta.isMalformed()) {
                                LOG.trace("Bad checksum. Skipping malformed packet.");
                                this.nzyme.getStatistics().tickMalformedCountAndNotify(probe, meta);
                                continue;
                            }

                            Dot11FrameType type = Dot11FrameType.getInstance(
                                    (byte) (((payload[0] << 2) & 0x30) | ((payload[0] >> 4) & 0x0F))
                            );

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
                                        LOG.trace("Skipping malformed frame.");
                                        getStatistics().tickMalformedCountAndNotify(this, meta);
                                    }
                                }
                            }

                            if(this.nzyme.getConfiguration().isPrintPacketInfo()) {
                                LOG.info("Type: {}, Header: {} bytes, Payload: {} bytes", type.value(), r.getHeader().getRawData().length, payload.length);
                            }

                            this.nzyme.getStatistics().tickFrameCount(meta);
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
    public void addFrameInterceptor(Dot11FrameInterceptor interceptor) {
        frameInterceptors.add(interceptor);
    }

    @Override
    public void scheduleAction() {
        throw new RuntimeException("Monitor probe cannot schedule actions.");
    }

    public static void configureAsBroadMonitor(final Dot11MonitorProbe probe) {
        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AssociationRequestFrame>() {
            private final Dot11FrameHandler<Dot11AssociationRequestFrame> handler = new Dot11AssociationRequestFrameHandler(probe);

            @Override
            public void intercept(Dot11AssociationRequestFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_REQUEST;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AssociationResponseFrame>() {
            private final Dot11FrameHandler<Dot11AssociationResponseFrame> handler = new Dot11AssociationResponseFrameHandler(probe);

            @Override
            public void intercept(Dot11AssociationResponseFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_RESPONSE;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11ProbeRequestFrame>() {
            private final Dot11FrameHandler<Dot11ProbeRequestFrame> handler = new Dot11ProbeRequestFrameHandler(probe);

            @Override
            public void intercept(Dot11ProbeRequestFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_REQUEST;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            private final Dot11FrameHandler<Dot11ProbeResponseFrame> handler = new Dot11ProbeResponseFrameHandler(probe);

            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            private final Dot11FrameHandler<Dot11BeaconFrame> handler = new Dot11BeaconFrameHandler(probe);

            @Override
            public void intercept(Dot11BeaconFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11DisassociationFrame>() {
            private final Dot11FrameHandler<Dot11DisassociationFrame> handler = new Dot11DisassociationFrameHandler(probe);

            @Override
            public void intercept(Dot11DisassociationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DISASSOCIATION;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11AuthenticationFrame>() {
            private final Dot11FrameHandler<Dot11AuthenticationFrame> handler = new Dot11AuthenticationFrameHandler(probe);

            @Override
            public void intercept(Dot11AuthenticationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.AUTHENTICATION;
            }
        });

        probe.addFrameInterceptor(new Dot11FrameInterceptor<Dot11DeauthenticationFrame>() {
            private final Dot11FrameHandler<Dot11DeauthenticationFrame> handler = new Dot11DeauthenticationFrameHandler(probe);

            @Override
            public void intercept(Dot11DeauthenticationFrame frame) {
                handler.handle(frame);
            }

            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DEAUTHENTICATION;
            }
        });
    }

}
