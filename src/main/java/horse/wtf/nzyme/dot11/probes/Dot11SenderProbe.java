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

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.deception.traps.Trap;
import horse.wtf.nzyme.statistics.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11SenderProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11SenderProbe.class);

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private PcapHandle pcap;
    private final Dot11ProbeConfiguration configuration;

    private final ChannelHopper channelHopper;
    private final Trap trap;

    private long totalFrames;

    public Dot11SenderProbe(Dot11ProbeConfiguration configuration, Trap trap, Statistics statistics, MetricRegistry metrics) {
        super(configuration, statistics, metrics);

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
                    trap.run();
                    totalFrames += trap.framesPerExecution();

                    inLoop.set(true);
                } catch(Exception e) {
                    inLoop.set(false);
                    LOG.error("Could not set trap [{}].", this.trap.getClass().getCanonicalName(), e);
                } finally {
                    try {
                        Thread.sleep(trap.getDelaySeconds()*1000);
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

    @Override
    public void addFrameInterceptor(Dot11FrameInterceptor interceptor) {
        throw new RuntimeException("Sender probe cannot intercept frames.");
    }

    @Override
    public List<Dot11FrameInterceptor> getInterceptors() {
        return Collections.emptyList();
    }

}
