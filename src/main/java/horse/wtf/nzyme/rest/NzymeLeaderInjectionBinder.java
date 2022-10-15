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

package horse.wtf.nzyme.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.web.AssetManifest;
import horse.wtf.nzyme.rest.web.IndexHtmlGenerator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;

public class NzymeLeaderInjectionBinder extends AbstractBinder {

    private final NzymeLeader nzyme;

    public NzymeLeaderInjectionBinder(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void configure() {
        bind(nzyme).to(NzymeLeader.class);
        bind(new MimetypesFileTypeMap()).to(MimetypesFileTypeMap.class);
        bind(nzyme.getObjectMapper()).to(ObjectMapper.class);

        try {
            bind(new IndexHtmlGenerator(nzyme.getConfiguration(), new AssetManifest())).to(IndexHtmlGenerator.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not bind IndexHtmlGenerator.", e);
        }
    }

}
