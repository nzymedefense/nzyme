/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.configuration.CLIArguments;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogAddress;
import horse.wtf.nzyme.graylog.GraylogUplink;
import horse.wtf.nzyme.graylog.Notification;
import horse.wtf.nzyme.handlers.*;
import horse.wtf.nzyme.statistics.Statistics;
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

public class NzymeImpl implements Nzyme {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    private final String nzymeId;

    private final CLIArguments cliArguments;
    private final Configuration configuration;
    private final String networkInterfaceName;

    private final Statistics statistics;
    private final List<GraylogUplink> graylogUplinks;
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

    public NzymeImpl(String interfaceName, ImmutableList<Integer> channels, CLIArguments cliArguments,
                     Configuration configuration, Statistics statistics) throws NzymeInitializationException {
        this.cliArguments = cliArguments;
        this.configuration = configuration;
        this.statistics = statistics;
        this.networkInterfaceName = interfaceName;

        this.nzymeId = getConfiguration().getNzymeId();

        // Initialize channel hopper.
        this.channelHopper = new ChannelHopper(this, channels);
        this.channelHopper.initialize();

        // Graylog GELF sender.
        this.graylogUplinks = Lists.newArrayList();
        for (GraylogAddress address : this.configuration.getGraylogAddresses()) {
            this.graylogUplinks.add(new GraylogUplink(address.getHost(), address.getPort(), this.nzymeId));
        }

        // Get network interface for PCAP.
        PcapNetworkInterface networkInterface;
        try {
            networkInterface = Pcaps.getDevByName(interfaceName);
        } catch (PcapNativeException e) {
            throw new NzymeInitializationException("Could not get network interface [" + interfaceName + "].", e);
        }

        if (networkInterface == null) {
            throw new NzymeInitializationException("Could not get network interface [" + interfaceName + "]. Does it exist and could it be that you have to be root?");
        }

        LOG.info("Building PCAP handle on interface [{}]", interfaceName);

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
            throw new NzymeInitializationException("Could not build PCAP handle.", e);
        }

        LOG.info("PCAP handle for [{}] acquired. Cycling through channels <{}>.", interfaceName, Joiner.on(",").join(channels));

        this.probeRequestHandler = new ProbeRequestFrameHandler(this);
        this.probeResponseHandler = new ProbeResponseFrameHandler(this);
        this.deauthFrameHandler = new DeauthenticationFrameHandler(this);
        this.beaconFrameHandler = new BeaconFrameHandler(this);
        this.associationRequestFrameHandler = new AssociationRequestFrameHandler(this);
        this.associationResponseFrameHandler = new AssociationResponseFrameHandler(this);
        this.disassociationFrameHandler = new DisassociationFrameHandler(this);
        this.authenticationFrameHandler = new AuthenticationFrameHandler(this);
    }

    public Runnable loop() {
        final Nzyme nzyme = this;

        return new Runnable() {
            @Override
            public void run() {
                LOG.info("Commencing 802.11 frame processing on [{}] ... (⌐■_■)–︻╦╤─ – – pew pew", getNetworkInterface());

                inLoop.set(true);
                while (true) {
                    // Park in case there is a channel switch happening.
                    try {
                        channelHopper.getLock().await();
                    } catch (InterruptedException e) {
                        LOG.warn("Channel hopper mutex acquisition interrupted. Skipping this frame.");
                        continue;
                    }

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
                                getStatistics().tickFrameCount(getChannelHopper().getCurrentChannel());

                                RadiotapPacket r = (RadiotapPacket) packet;
                                byte[] payload = r.getPayload().getRawData();

                                Dot11MetaInformation meta = Dot11MetaInformation.parse(r.getHeader().getDataFields());

                                if (meta.isMalformed()) {
                                    LOG.trace("Bad checksum. Skipping malformed packet.");
                                    statistics.tickMalformedCountAndNotify(nzyme, getChannelHopper().getCurrentChannel());
                                    continue;
                                }

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
                            }
                        } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException | IllegalRawDataException e) {
                            statistics.tickMalformedCountAndNotify(nzyme, getChannelHopper().getCurrentChannel());
                            LOG.debug("Illegal data received.", e);
                        } catch(Exception e) {
                            LOG.error("Could not process packet.", e);
                        }
                    }
                }
            }
        };
    }

    public void notify(Notification notification, Dot11MetaInformation meta) {
        for (GraylogUplink uplink : getGraylogUplinks()) {
            uplink.notify(notification, meta);
        }
    }

    public String getNzymeId() {
        return nzymeId;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String getNetworkInterface() {
        return this.networkInterfaceName;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public List<GraylogUplink> getGraylogUplinks() {
        return graylogUplinks;
    }

    public ChannelHopper getChannelHopper() {
        return channelHopper;
    }

    public boolean isInLoop() {
        return inLoop.get();
    }

}
