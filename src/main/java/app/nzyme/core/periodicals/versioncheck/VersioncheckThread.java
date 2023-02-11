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

package app.nzyme.core.periodicals.versioncheck;

import app.nzyme.core.NzymeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.net.HttpHeaders;
import app.nzyme.core.MemoryRegistry;
import app.nzyme.core.Version;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.Wall;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VersioncheckThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(VersioncheckThread.class);

    private static final String VERSIONCHECK_API = "https://versionchecks.nzyme.org/check";

    private static final String USER_AGENT = String.format(Locale.ENGLISH, "nzyme (%s, %s, %s, %s)",
            System.getProperty("java.vendor"), System.getProperty("java.version"),
            System.getProperty("os.name"), System.getProperty("os.version"));

    private final OkHttpClient httpClient;
    private final Version version;
    private final NzymeNode nzyme;
    private final ObjectMapper om;

    public VersioncheckThread(Version version, NzymeNode nzyme) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        this.version = version;
        this.nzyme = nzyme;
        this.om = new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void execute() {
        LOG.info("Starting to check for most recent nzyme version.");

        try {
            Request request = new Request.Builder()
                    .get()
                    .url(HttpUrl.parse(VERSIONCHECK_API).newBuilder()
                            .addQueryParameter("version", version.getVersionString())
                            .build())
                    .addHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                    .build();

            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
               LOG.error("Could not check for newest nzyme version. Expected HTTP <200> but received HTTP <{}>. " +
                               "Please consult the README.", response.code());
               return;
            }

            if (response.body() != null) {
                String responseString = response.body().string();
                VersionResponse versionResponse = om.readValue(responseString, VersionResponse.class);

                if (versionResponse.getVersion().greaterThan(version.getVersion())) {
                    String text = "You are running an outdated version of nzyme: v"
                            + version.getVersionString() + ". The currently available stable version is v"
                            + versionResponse.getFullVersionString() + " (released at " +
                            versionResponse.releasedAt + ").";
                    LOG.info("\n" + Wall.build("WARNING! OUTDATED VERSION!", text));

                    nzyme.getRegistry().setBool(MemoryRegistry.KEY.NEW_VERSION_AVAILABLE, true);
                }

                LOG.info("Successfully completed version check. Everything seems up to date.");
            } else {
                LOG.error("Could not check for newest nzyme version. Received empty response. Please consult the README.");
            }
        } catch(Exception e) {
            LOG.error("Could not check for newest nzyme version. Please consult the README.", e);
        }
    }

    @Override
    public String getName() {
        return "VersioncheckThread";
    }

}
