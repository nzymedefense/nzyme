package horse.wtf.nzyme;

import com.beust.jcommander.JCommander;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.configuration.CLIArguments;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.graylog.GraylogUplink;
import horse.wtf.nzyme.handlers.BeaconFrameHandler;
import horse.wtf.nzyme.handlers.DeauthenticationFrameHandler;
import horse.wtf.nzyme.handlers.ProbeRequestFrameHandler;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.statistics.StatisticsPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.packet.namednumber.Dot11FrameType;

import java.io.EOFException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Nzyme {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    public static final int STATS_INTERVAL = 60;

    private final String nzymeId;

    private final CLIArguments cliArguments;
    private final Configuration configuration;
    private final Statistics statistics;
    private final GraylogUplink graylogUplink;
    private final ChannelHopper channelHopper;

    private final PcapHandle pcap;

    // Frame handlers.
    private final ProbeRequestFrameHandler probeRequestHandler;
    private final DeauthenticationFrameHandler deauthFrameHandler;
    private final BeaconFrameHandler beaconFrameHandler;

    private boolean inLoop = false;

    public Nzyme(String[] argv) throws InitializationException {
        // Parse CLI arguments.
        this.cliArguments = new CLIArguments();
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Parse configuration.
        this.configuration = new Configuration();
        try {
            new JadConfig(new PropertiesRepository(this.cliArguments.getConfigFilePath()), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            throw new InitializationException("Could not read config.", e);
        }

        this.nzymeId = getConfiguration().getNzymeId();

        // Initialize channel hopper.
        this.channelHopper = new ChannelHopper(this, this.configuration.getChannels());
        this.channelHopper.initialize();

        // Set up statistics printer.
        this.statistics = new Statistics();
        final StatisticsPrinter statisticsPrinter = new StatisticsPrinter(this.statistics);
        LOG.info("Printing statistics every {} seconds. Logs are in [logs/] and will be automatically rotated.", STATS_INTERVAL);
        // Statistics printer.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("statistics-%d")
                .build()
        ).scheduleAtFixedRate(() -> {
            LOG.info(statisticsPrinter.print());
            statistics.resetAccumulativeTicks();
        }, STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);

        // Graylog GELF sender.
        this.graylogUplink = new GraylogUplink(
                this.configuration.getGraylogAddress().getHost(),
                this.configuration.getGraylogAddress().getPort(),
                this.nzymeId
        );

        // Get network interface for PCAP.
        PcapNetworkInterface networkInterface;
        try {
            networkInterface = Pcaps.getDevByName(this.configuration.getNetworkInterface());
        } catch (PcapNativeException e) {
            throw new InitializationException("Could not get network interface [" + this.configuration.getNetworkInterface() + "].", e);
        }

        if (networkInterface == null) {
            throw new InitializationException("Could not get network interface [" + this.configuration.getNetworkInterface() + "]. Does it exist and could it be that you have to be root?");
        }

        LOG.info("Building PCAP handle on interface [{}]", this.configuration.getNetworkInterface());

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
                    "type mgt and (subtype deauth or subtype probe-req or subtype beacon)",
                    BpfProgram.BpfCompileMode.OPTIMIZE
            );
        } catch (Exception e) {
            throw new InitializationException("Could not build PCAP handle.", e);
        }

        LOG.info("PCAP handle acquired. Cycling through channels <{}>.", Joiner.on(",").join(this.configuration.getChannels()));

        this.probeRequestHandler = new ProbeRequestFrameHandler(this);
        this.deauthFrameHandler = new DeauthenticationFrameHandler(this);
        this.beaconFrameHandler = new BeaconFrameHandler(this);
    }

    public void loop() {
        LOG.info("Commencing 802.11 frame processing ... (⌐■_■)–︻╦╤─ – – pew pew");

        this.inLoop = true;
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
                // This is a sympton of malformed data.
                LOG.trace(e);
                continue;
            }

            if (packet != null) {
                try {
                    if (packet instanceof RadiotapPacket) {
                        getStatistics().tickFrameCount(this.getChannelHopper().getCurrentChannel());

                        RadiotapPacket r = (RadiotapPacket) packet;
                        byte[] payload = r.getPayload().getRawData();

                        Dot11FrameType type = Dot11FrameType.getInstance(
                                (byte) (((payload[0] << 2) & 0x30) | ((payload[0] >> 4) & 0x0F))
                        );

                        // Determine type and handler.
                        switch (type.value()) {
                            case 4: // probe-req
                                probeRequestHandler.handle(payload, r.getHeader());
                                break;
                            case 8: // beacon
                                beaconFrameHandler.handle(payload, r.getHeader());
                                break;
                            case 12: // deauth
                                deauthFrameHandler.handle(payload, r.getHeader());
                                break;
                            // association, disassociation
                            default:
                                LOG.warn("Not handling frame type [{}].", type.value());
                        }
                    }
                } catch(IllegalArgumentException | IllegalRawDataException e) {
                    this.getStatistics().tickMalformedCount(this.getChannelHopper().getCurrentChannel());
                    LOG.trace("Illegal data received.", e);
                } catch(Exception e) {
                    LOG.error("Could not process packet.", e);
                }
            }
        }
    }

    public String getNzymeId() {
        return nzymeId;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public GraylogUplink getGraylogUplink() {
        return graylogUplink;
    }

    public ChannelHopper getChannelHopper() {
        return channelHopper;
    }

    public boolean isInLoop() {
        return inLoop;
    }

    class InitializationException extends Exception {

        InitializationException(String msg) {
            super(msg);
        }

        InitializationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

}
