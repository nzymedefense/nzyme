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
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.deception.bluffs.Bluff;
import horse.wtf.nzyme.dot11.deception.bluffs.ProbeRequest;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11SenderProbe extends Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11SenderProbe.class);

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private final Nzyme nzyme;
    private final Dot11ProbeConfiguration configuration;

    public Dot11SenderProbe(Nzyme nzyme, Dot11ProbeConfiguration configuration, MetricRegistry metrics) {
        super(configuration, nzyme);

        this.configuration = configuration;
        this.nzyme = nzyme;
    }

    @Override
    public Runnable loop() throws Dot11ProbeInitializationException {
        return () -> {
            while(true) {
                try {
                    inLoop.set(true);

                    Bluff probeRequest = new ProbeRequest(nzyme.getConfiguration(), configuration.networkInterfaceName(), "FOOKED", "00:c0:ca:97:12:16");
                    probeRequest.executeFailFast();
                } catch(Exception e) {
                    LOG.error("Error in [{}]", this.getClass().getCanonicalName(), e);
                } finally {
                    try {
                        Thread.sleep(1000);
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
    public void addFrameInterceptor(Dot11FrameInterceptor interceptor) {
        throw new RuntimeException("Sender probe cannot intercept frames.");
    }

    @Override
    public void scheduleAction() {

    }

}
