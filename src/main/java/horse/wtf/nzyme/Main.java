package horse.wtf.nzyme;

import com.beust.jcommander.JCommander;
import horse.wtf.nzyme.handlers.BeaconFrameHandler;
import horse.wtf.nzyme.handlers.DeauthenticationFrameHandler;
import horse.wtf.nzyme.handlers.ProbeRequestFrameHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.Dot11FrameType;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    // TODO fix broken strings
    // TODO aggregate beacons?
    // TODO send timestamp of frame not when we processed it
    // TODO implement assoc and disassoc
    // TODO signal strength
    // TODO test deauth handler on other platform. might have same header offset issue
    // proper return codes

    public static void main(String[] argv) {
        LOG.info("Starting up. | (⌐■_■)–︻╦╤─ – – pew pew");

        CLIArguments args = new CLIArguments();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        final Statistics stats = new Statistics();
        final Graylog graylog = new Graylog(args.getGraylogAddress().getHost(), args.getGraylogAddress().getPort(), "nzyme-lab-1");

        PcapNetworkInterface nif;
        try {
            nif = Pcaps.getDevByName(args.getNetworkInterface());
        } catch (PcapNativeException e) {
            LOG.error("Could not get network interface [[}].", args.getNetworkInterface(), e);
            return;
        }

        if (nif == null) {
            LOG.error("Could not get network interface [{}]. Does it exist and could it be that you have to be root?", args.getNetworkInterface());
            return;
        }

        LOG.info("Building PCAP handle on interface [{}]", args.getNetworkInterface());

        PcapHandle.Builder phb = new PcapHandle.Builder(nif.getName())
                .rfmon(true)
                .snaplen(65536)
                .promiscuousMode(PcapNetworkInterface.PromiscuousMode.PROMISCUOUS)
                .timeoutMillis(100)
                .bufferSize(5 * 1024 * 1024)
                .timestampPrecision(PcapHandle.TimestampPrecision.MICRO);

        PcapHandle handle;
        try {
            handle = phb.build();
            handle.setFilter(
                    "type mgt and (subtype deauth or subtype probe-req or subtype beacon)",
                    BpfProgram.BpfCompileMode.OPTIMIZE
            );
        } catch (Exception e) {
            LOG.error("Could not build PCAP handle.", e);
            return;
        }

        // Handlers
        ProbeRequestFrameHandler probeRequestHandler = new ProbeRequestFrameHandler(graylog);
        DeauthenticationFrameHandler deauthFrameHandler = new DeauthenticationFrameHandler(graylog);
        BeaconFrameHandler beaconFrameHandler = new BeaconFrameHandler(graylog);

        // Show progress on USR1 signal
        Signal.handle(new Signal("USR2"), new SignalHandler() {
            public void handle(Signal signal) {
                LOG.info("======= Statistics");
                LOG.info("Relevant 802.11 frames processed: {}", stats.getFrameCount().get());
            }
        });

        while (true) {
            Packet packet;

            try {
                packet = handle.getNextPacketEx();
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

                        stats.tickFrameCount();

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
                    LOG.trace("Illegal data received.", e);
                } catch(Exception e) {
                    LOG.error("Could not process packet.", e);
                }
            }
        }
    }

}
