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

package horse.wtf.nzyme.rest.resources.system;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.Dot11BSSIDDefinition;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.responses.assetinventory.Dot11AssetInventoryResponse;
import horse.wtf.nzyme.rest.responses.assetinventory.Dot11BSSIDAssetResponse;
import horse.wtf.nzyme.rest.responses.assetinventory.Dot11SSIDAssetResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Path("/api/asset-inventory")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class AssetInventoryResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response dot11Assets() {
        ImmutableList.Builder<Dot11SSIDAssetResponse> ssids = new ImmutableList.Builder<>();
        for (Dot11NetworkDefinition ssid : nzyme.getConfiguration().dot11Networks()) {
            ImmutableList.Builder<Dot11BSSIDAssetResponse> bssids = new ImmutableList.Builder<>();
            for (Dot11BSSIDDefinition bssid : ssid.bssids()) {
                bssids.add(Dot11BSSIDAssetResponse.create(
                        bssid.address(),
                        bssid.fingerprints()
                ));
            }

            ssids.add(Dot11SSIDAssetResponse.create(
                    ssid.ssid(),
                    bssids.build(),
                    ssid.channels(),
                    ssid.security()
            ));
        }

        return Response.ok(Dot11AssetInventoryResponse.create(ssids.build(), buildSSIDCSV(), buildBSSIDCSV())).build();
    }

    private String buildSSIDCSV() {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        csvWriter.writeNext(new String[]{"ssid","security","channels","bssids"});

        for (Dot11NetworkDefinition ssid : nzyme.getConfiguration().dot11Networks()) {
            List<String> bssids = Lists.newArrayList();
            for (Dot11BSSIDDefinition bssid : ssid.bssids()) {
                bssids.add(bssid.address());
            }

            List<String> columns = Lists.newArrayList();
            columns.add(ssid.ssid());
            columns.add(Joiner.on(",").join(ssid.security()));
            columns.add(Joiner.on(",").join(ssid.channels()));
            columns.add(Joiner.on(",").join(bssids));
            csvWriter.writeNext(columns.toArray(String[]::new));
        }

        return stringWriter.toString();
    }

    private String buildBSSIDCSV() {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        csvWriter.writeNext(new String[]{"bssid","ssid","fingerprint","security"});

        for (Dot11NetworkDefinition ssid : nzyme.getConfiguration().dot11Networks()) {
            for (Dot11BSSIDDefinition bssid : ssid.bssids()) {
                List<String> columns = new ArrayList<>();
                columns.add(bssid.address());
                columns.add(ssid.ssid());
                columns.add(Joiner.on(",").join(bssid.fingerprints()));
                columns.add(Joiner.on(",").join(ssid.security()));
                csvWriter.writeNext(columns.toArray(String[]::new));
            }
        }

        return stringWriter.toString();
    }

}
