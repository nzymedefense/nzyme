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

package horse.wtf.nzyme.ouis;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class OUIUpdater extends Periodical {

    private static final Logger LOG = LogManager.getLogger(OUIUpdater.class);

    private final NzymeLeader nzyme;

    public OUIUpdater(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            this.nzyme.getOUIManager().fetchAndUpdate();
        } catch (IOException e) {
            LOG.error("Could not fetch and update OUI list.", e);
        }
    }

    @Override
    public String getName() {
        return "OUIUpdater";
    }

}
