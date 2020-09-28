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

package horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.resources;

import horse.wtf.nzyme.util.Tools;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;

@Path("/")
public class TrackerWebHIDAssetsResource {

    // This is all insanely hardcoded because there are only a handful of files.

    @GET
    public Response getPage(@Context ContainerRequest request, @Context HttpHeaders headers) {
        try {
            return Response.ok(readFile("index.html"), "text/html").build();
        } catch(IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/assets/favicon.ico")
    public Response getFavicon() {

        try {
            return Response.ok(readFile("assets/favicon.ico"), "image/x-icon").build();
        } catch(IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/assets/{filename}.css")
    public Response getCSS(@PathParam("filename") String filename) {
        try {
            return Response.ok(readFile("assets/" + Tools.safeAlphanumericString(filename) + ".css"), "text/css").build();
        } catch(IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/assets/{filename}.js")
    public Response getFont(@PathParam("filename") String filename) {
        try {
            return Response.ok(readFile("assets/" + Tools.safeAlphanumericString(filename) + ".js"), "application/javascript").build();
        } catch(IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/assets/fonts/{filename}.woff2")
    public Response getJS(@PathParam("filename") String filename) {
        try {
            return Response.ok(readFile("assets/fonts/" + Tools.safeAlphanumericString(filename) + ".woff2"), "font/woff2").build();
        } catch(IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private byte[] readFile(String filename) throws IOException {
        //noinspection UnstableApiUsage
        InputStream resource = getClass().getClassLoader().getResourceAsStream("trackerwebhid/" + filename);
        if (resource == null) {
            throw new RuntimeException("Couldn't find asset [trackerwebhid/" + filename + "].");
        }

        //noinspection UnstableApiUsage
        return resource.readAllBytes();
    }

}
