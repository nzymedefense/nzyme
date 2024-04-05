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

package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.branding.BrandingRegistryKeys;
import app.nzyme.core.rest.requests.UpdateSidebarTitleRequest;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.rest.responses.system.SidebarTitleResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;
import app.nzyme.plugin.rest.configuration.ConstraintValidationResult;
import app.nzyme.plugin.rest.configuration.ConstraintValidator;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.core.MemoryRegistry;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.system.VersionResponse;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Path("/api/system")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    private static final Logger LOG = LogManager.getLogger(SystemResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/status")
    public Response getStatus() {
        return Response.ok().build();
    }

    @GET
    @Path("/version")
    public Response getVersion() {
        return Response.ok(VersionResponse.create(
                nzyme.getVersion().getVersionString(),
                nzyme.getRegistry().getBool(MemoryRegistry.KEY.NEW_VERSION_AVAILABLE),
                nzyme.getConfiguration().versionchecksEnabled()
        )).build();
    }

    @GET
    @Path("/lookandfeel/sidebartitle")
    public Response getSidebarTitle() {
        //noinspection OptionalGetWithoutIsPresent
        String title = nzyme.getDatabaseCoreRegistry()
                .getValue(BrandingRegistryKeys.SIDEBAR_TITLE_TEXT.key())
                .orElse(BrandingRegistryKeys.SIDEBAR_TITLE_TEXT.defaultValue().get());
        String subtitle = nzyme.getDatabaseCoreRegistry()
                .getValueOrNull(BrandingRegistryKeys.SIDEBAR_SUBTITLE_TEXT.key());

        return Response.ok(SidebarTitleResponse.create(title, subtitle)).build();
    }

    @PUT
    @Path("/lookandfeel/sidebartitle")
    public Response setSidebarTitle(UpdateSidebarTitleRequest req) {
        for (ConfigurationEntryConstraint c : BrandingRegistryKeys.SIDEBAR_TITLE_TEXT.constraints().get()) {
            ConstraintValidationResult result = ConstraintValidator.validate(req.title(), c);
            if (!result.isOk()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.create(result.getReason()))
                        .build();
            }
        }

        if (!Strings.isNullOrEmpty(req.subtitle())) {
            for (ConfigurationEntryConstraint c : BrandingRegistryKeys.SIDEBAR_SUBTITLE_TEXT.constraints().get()) {
                ConstraintValidationResult result = ConstraintValidator.validate(req.subtitle(), c);
                if (!result.isOk()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.create(result.getReason()))
                            .build();
                }
            }
        }

        nzyme.getDatabaseCoreRegistry().setValue(BrandingRegistryKeys.SIDEBAR_TITLE_TEXT.key(), req.title());

        if (!Strings.isNullOrEmpty(req.subtitle())) {
            nzyme.getDatabaseCoreRegistry().setValue(BrandingRegistryKeys.SIDEBAR_SUBTITLE_TEXT.key(), req.subtitle());
        } else {
            nzyme.getDatabaseCoreRegistry().deleteValue(BrandingRegistryKeys.SIDEBAR_SUBTITLE_TEXT.key());
        }

        return Response.ok().build();
    }

    @POST
    @Path("/lookandfeel/loginimage")
    public Response uploadLoginImage(@FormDataParam("image") InputStream imageFile) {
        byte[] imageBytes;
        try {
            /*
             * Set the limit to 1MB plus 1 byte. If it fills this entirely, a file too large was uploaded.
             * There is also a larger limit in the HTTP server itself.
             */
            imageBytes = ByteStreams.limit(imageFile, 1048576).readAllBytes();
        } catch (IOException e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (imageBytes.length == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Uploaded file is empty."))
                    .build();
        }

        if (imageBytes.length == 5242881) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Uploaded  file is too large. Maximum file size is 1MB."))
                    .build();
        }

        try {
            // New input stream because the previous one had been consumed when checking the length.
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.create("Could not read image file. Make sure it is a JPG or PNG file."))
                        .build();
            }

            if (image.getHeight() != 600 && image.getWidth() != 700) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.create("Image must be 700x600px."))
                        .build();
            }

            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOut);


            // Convert to Base64 and store in registry.
            nzyme.getDatabaseCoreRegistry().setValue(
                    BrandingRegistryKeys.LOGIN_IMAGE.key(),
                    BaseEncoding.base64().encode(pngOut.toByteArray())
            );
        } catch (Exception e) {
            LOG.warn("Could not process uploaded file.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Could not process uploaded file. Make sure it is a JPG or " +
                            "PNG file."))
                    .build();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/lookandfeel/loginimage")
    public Response resetLoginImage() {
        nzyme.getDatabaseCoreRegistry().deleteValue(BrandingRegistryKeys.LOGIN_IMAGE.key());
        return Response.ok().build();
    }

}
