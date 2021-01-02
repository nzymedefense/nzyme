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

package horse.wtf.nzyme.dot11.anonymization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Anonymizer {

    private static final Logger LOG = LogManager.getLogger(Anonymizer.class);

    private static final String ANONFILE_NAME = "anonfile";

    private final boolean isEnabled;
    private final String nzymeDataDirectoryPath;
    private final ObjectMapper om;

    private Map<String, String> ssids;
    private Map<String, String> bssids;

    public Anonymizer(boolean isEnabled, String nzymeDataDirectoryPath) {
        this.isEnabled = isEnabled;
        this.nzymeDataDirectoryPath = nzymeDataDirectoryPath;
        this.om = new ObjectMapper();

        this.ssids = Maps.newHashMap();
        this.bssids = Maps.newHashMap();

        if (isEnabled) {
            loadFromFile();

            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("anonfile-writer-%d")
                    .build())
                    .scheduleWithFixedDelay(this::writeToFile, 1, 1, TimeUnit.MINUTES);
        }
    }

    private void loadFromFile() {
        if (!isEnabled) {
            return;
        }

        File file = new File(nzymeDataDirectoryPath + "/" + ANONFILE_NAME);

        if (!file.exists()) {
            LOG.error("Anonfile not found.");
            return;
        }

        try {
            //noinspection UnstableApiUsage
            String content = new String(Files.toByteArray(file), Charsets.UTF_8);

            Anonfile parsed = om.readValue(content, Anonfile.class);
            LOG.info("Replaying anonfile.");

            this.ssids = parsed.ssids();
            this.bssids = parsed.bssids();
        } catch (Exception e) {
            LOG.error("Anonfile exists but failed to parse.", e);
            return;
        }
    }

    private void writeToFile() {
        try {
            //noinspection UnstableApiUsage
            Files.write(
                    om.writeValueAsBytes(Anonfile.create(this.ssids, this.bssids)),
                    new File(nzymeDataDirectoryPath + "/" + ANONFILE_NAME)
            );
        } catch(Exception e) {
            LOG.error("Could not write anonfile.", e);
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String anonymizeSSID(String ssid) {
        if (!isEnabled || Strings.isNullOrEmpty(ssid)) {
            return ssid;
        }

        String anonymized = this.ssids.get(ssid);
        if (anonymized != null) {
            return anonymized;
        } else {
            // This SSID has not been anonymized yet. Pick a random SSID.
            String random = RandomSSID.POPULATION.get(new Random().nextInt(RandomSSID.POPULATION.size()));
            if (this.ssids.containsKey(random)) {
                // random string
                random = "ssid-" + new Random().nextInt(999999);
            }

            ssids.put(ssid, random);
            return random;
        }
    }

    public String anonymizeBSSID(String bssid) {
        if (!isEnabled || Strings.isNullOrEmpty(bssid)) {
            return bssid;
        }

        bssid = bssid.toLowerCase();

        String anonymized = this.bssids.get(bssid);
        if (anonymized != null) {
            return anonymized;
        } else {
            // This BSSID has not been anonymized yet. Generate a random BSSID
            Random random = new Random();
            String generated = String.format(bssid.substring(0, 8) + ":%02X:%02X:%02X", random.nextInt(255), random.nextInt(255),  random.nextInt(255)).toLowerCase();

            bssids.put(bssid, generated);
            return generated;
        }
    }

}
