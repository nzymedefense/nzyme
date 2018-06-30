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

package horse.wtf.nzyme.probes.dot11;

import com.google.common.base.Joiner;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.handlers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.packet.namednumber.Dot11FrameType;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11MonitorProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    private final Nzyme nzyme;
    private final Dot11ProbeConfiguration configuration;

    private final ChannelHopper channelHopper;
    private final PcapHandle pcap;

    // Frame handlers.
    private final ProbeRequestFrameHandler probeRequestHandler;
    private final ProbeResponseFrameHandler probeResponseHandler;
    private final DeauthenticationFrameHandler deauthFrameHandler;
    private final BeaconFrameHandler beaconFrameHandler;
    private final AssociationRequestFrameHandler associationRequestFrameHandler;
    private final AssociationResponseFrameHandler associationResponseFrameHandler;
    private final DisassociationFrameHandler disassociationFrameHandler;
    private final AuthenticationFrameHandler authenticationFrameHandler;

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    public Dot11MonitorProbe(Nzyme nzyme, Dot11ProbeConfiguration configuration) throws Dot11ProbeInitializationException {
        super(configuration, nzyme.getStatistics());

        this.nzyme = nzyme;
        this.configuration = configuration;

        // Initialize channel hopper.
        this.channelHopper = new ChannelHopper(this, configuration);
        this.channelHopper.initialize();

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

        this.probeRequestHandler = new ProbeRequestFrameHandler(this);
        this.probeResponseHandler = new ProbeResponseFrameHandler(this);
        this.deauthFrameHandler = new DeauthenticationFrameHandler(this);
        this.beaconFrameHandler = new BeaconFrameHandler(this);
        this.associationRequestFrameHandler = new AssociationRequestFrameHandler(this);
        this.associationResponseFrameHandler = new AssociationResponseFrameHandler(this);
        this.disassociationFrameHandler = new DisassociationFrameHandler(this);
        this.authenticationFrameHandler = new AuthenticationFrameHandler(this);
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
                            this.nzyme.getStatistics().tickFrameCount(meta);

                            Dot11FrameType type = Dot11FrameType.getInstance(
                                    (byte) (((payload[0] << 2) & 0x30) | ((payload[0] >> 4) & 0x0F))
                            );

                            // Determine type and handler.
                            switch (type.value()) {
                                case 0: // assoc-req
                                    associationRequestFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 1: // assoc-resp
                                    associationResponseFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 4: // probe-req
                                    probeRequestHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 5: // probe-resp
                                    probeResponseHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 8: // beacon
                                    beaconFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 10: // disaasoc
                                    disassociationFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 11: // auth
                                    authenticationFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                case 12: // deauth
                                    deauthFrameHandler.handle(payload, r.getHeader().getRawData(), meta);
                                    break;
                                default:
                                    LOG.warn("Not handling frame type [{}].", type.value());
                            }

                            if(this.nzyme.getConfiguration().isPrintPacketInfo()) {
                                LOG.info("Type: {}, Header: {} bytes, Payload: {} bytes", type.value(), r.getHeader().getRawData().length, payload.length);
                            }
                        }
                    } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException | IllegalRawDataException e) {
                        this.nzyme.getStatistics().tickMalformedCountAndNotify(probe, null);
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

}
