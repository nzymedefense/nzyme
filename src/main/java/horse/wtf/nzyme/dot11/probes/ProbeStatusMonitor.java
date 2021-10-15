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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.events.BrokenProbeEvent;
import horse.wtf.nzyme.periodicals.Periodical;

import java.util.List;

public class ProbeStatusMonitor extends Periodical {

    private final NzymeLeader nzyme;

    public ProbeStatusMonitor(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        for (Dot11Probe probe : nzyme.getProbes()) {
            if (!probe.isActive() || !probe.isInLoop()) {
                List<String> errors = Lists.newArrayList();

                if (!probe.isActive()) {
                    errors.add("not sending or receiving frames");
                }

                if (!probe.isInLoop()) {
                    errors.add("not in send/receive loop");
                }

                nzyme.getEventService().recordEvent(new BrokenProbeEvent(probe.getName(), Joiner.on(", ").join(errors)));
            }
        }

    }

    @Override
    public String getName() {
        return "ProbeStatusMonitor";
    }

}
