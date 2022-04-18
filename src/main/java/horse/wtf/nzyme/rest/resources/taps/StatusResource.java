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

package horse.wtf.nzyme.rest.resources.taps;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.authentication.TapSecured;
import horse.wtf.nzyme.rest.resources.taps.reports.StatusReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/taps/status")
@TapSecured
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    private static final Logger LOG = LogManager.getLogger(StatusResource.class);

    @Inject
    private NzymeLeader nzyme;

    /*
     * TODO: auth system
     *       reject if tap with this name already exists
     *       reject if tap name > 50chars (do the same in tap config loading)
     */

    @POST
    public Response status(StatusReport report) {
        LOG.debug("Received status from tap [{}]: memory_total: {}, memory_free: {}, memory_used: {}, cpu: {}%, bytes processed: {}, 10s bytes processed: {}",
                report.tapName(),
                report.systemMetrics().memoryTotal(),
                report.systemMetrics().memoryFree(),
                report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree(),
                report.systemMetrics().cpuLoad(),
                report.processedBytesTotal(),
                report.processedBytesAverage()
        );



        return Response.status(Response.Status.CREATED).build();
    }

}
