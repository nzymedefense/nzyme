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

package app.nzyme.core.ouis;

import app.nzyme.core.NzymeNode;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import app.nzyme.core.util.MetricNames;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OUIManager {

    private static final Logger LOG = LogManager.getLogger(OUIManager.class);

    private static final String OUI_SOURCE = "http://standards-oui.ieee.org/oui/oui.txt";
    private static final Pattern LINE_REGEX = Pattern.compile("^(.+)\\b.+\\(base 16\\)(.+)$");

    private ImmutableMap<String, String> ouis;

    private final NzymeNode nzyme;

    private final Timer lookupTimer;

    public OUIManager(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.ouis = ImmutableMap.<String, String>builder().build();
        this.lookupTimer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.OUI_LOOKUP_TIMING));
    }

    @Nullable
    public String lookupMac(String bssid) {
        if (this.ouis == null || this.ouis.isEmpty()) {
            LOG.debug("Internal OUI table is NULL or empty.");
            return null;
        }

        if (bssid == null || bssid.isEmpty()) {
            LOG.debug("Passed OUI is null or empty.");
            return null;
        }

        Timer.Context timer = lookupTimer.time();
        String result = ouis.get(bssid.toUpperCase().substring(0, 8).replace(":", ""));
        timer.stop();

        return result;
    }

    public void fetchAndUpdate() throws IOException {
        if (!nzyme.getConfiguration().fetchOuis()) {
            LOG.info("Fetching OUIs has been disabled in nzyme configuration. Not fetching.");
            return;
        }

        LOG.info("Fetching and updating list of OUIs from [{}]. This might take a moment.", OUI_SOURCE);

        OkHttpClient c = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .followRedirects(true)
                .build();

        Stopwatch downloadTime = Stopwatch.createStarted();
        Response response = c.newCall(new Request.Builder()
                .addHeader("User-Agent", "nzyme")
                .get()
                .url(OUI_SOURCE)
                .build())
                .execute();
        downloadTime.stop();

        if (!response.isSuccessful()) {
            throw new RuntimeException("Expected HTTP 200 but got HTTP " + response.code());
        }

        if (response.body() == null) {
            throw new RuntimeException("Empty response.");
        }

        Stopwatch parsingTime = Stopwatch.createStarted();
        Map<String, String> map = Maps.newHashMap();
        try {
            for (String line : Splitter.on("\n").trimResults().omitEmptyStrings().split(response.body().string())) {
                if (line.contains("(base 16)")) {
                    Matcher m = LINE_REGEX.matcher(line);
                    if (m.find()) {
                        map.put(m.group(1).trim(), m.group(2).trim());
                    }
                }
            }
            parsingTime.stop();
        } catch(Exception e) {
            throw new RuntimeException("OUI parsing error.", e);
        } finally {
            response.body().close();
        }

        this.ouis = ImmutableMap.copyOf(map);

        LOG.info("Done! Now <{}> OUIs in memory. Download time <{}ms>, parsing time <{}s>.",
                this.ouis.size(),
                downloadTime.elapsed(TimeUnit.MILLISECONDS),
                parsingTime.elapsed(TimeUnit.SECONDS));
    }

}
