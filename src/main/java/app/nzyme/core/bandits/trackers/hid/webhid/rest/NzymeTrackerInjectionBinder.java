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

package app.nzyme.core.bandits.trackers.hid.webhid.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import app.nzyme.core.NzymeTracker;
import app.nzyme.core.bandits.trackers.hid.webhid.WebHID;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class NzymeTrackerInjectionBinder extends AbstractBinder  {

    private final NzymeTracker nzyme;
    private final WebHID webHID;

    public NzymeTrackerInjectionBinder(NzymeTracker nzmye, WebHID webHID) {
        this.nzyme = nzmye;
        this.webHID = webHID;
    }

    @Override
    protected void configure() {
        bind(nzyme).to(NzymeTracker.class);
        bind(webHID).to(WebHID.class);
        bind(nzyme.getObjectMapper()).to(ObjectMapper.class);
    }

}
