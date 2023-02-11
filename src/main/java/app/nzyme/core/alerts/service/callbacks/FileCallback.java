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

package app.nzyme.core.alerts.service.callbacks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auto.value.AutoValue;
import com.typesafe.config.Config;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.configuration.ConfigurationKeys;
import app.nzyme.core.configuration.ConfigurationValidator;
import app.nzyme.core.configuration.IncompleteConfigurationException;
import app.nzyme.core.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCallback implements AlertCallback {

    private final Path path;
    private Object mutex = new Object();

    public FileCallback(Configuration config) {
        path = config.path();
    }

    @Override
    public void call(Alert alert) {
        String payload;
        try {
            payload = alert.toJSONString();
        } catch(JsonProcessingException e) {
            throw new RuntimeException("Could not transform alert to JSON.", e);
        }

        synchronized (mutex) {
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                } catch (IOException e) {
                    throw new RuntimeException("Could not create alert callback file.", e);
                }
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(path.toFile(), true);
                out.write(payload.getBytes());
                out.write("\n".getBytes());
            }catch (Exception e) {
                throw new RuntimeException("Could not write to alert callback file.", e);
            } finally {
                if (out != null) {
                    try { out.close(); } catch (IOException e) { throw new RuntimeException(e); }
                }
            }
        }
    }

    private static final String WHERE = "alerting.callbacks.[file]";

    public static FileCallback.Configuration parseConfiguration(Config c) throws InvalidConfigurationException, IncompleteConfigurationException {
        ConfigurationValidator.expect(c, ConfigurationKeys.PATH, WHERE, String.class);

        Path filePath;

        try {
            filePath = new File(c.getString(ConfigurationKeys.PATH)).toPath();
        } catch (Exception e) {
            throw new InvalidConfigurationException("Could not build path to file.", e);
        }

        if (Files.exists(filePath) && !Files.isWritable(filePath)) {
            // Only check if file is writable if it exists. It could be that it simply hasn't been written yet and that's fine.
            throw new InvalidConfigurationException("File [" + filePath + "] exists but is not writable.");
        }

        return Configuration.create(filePath);
    }

    @AutoValue
    public static abstract class Configuration {

        public abstract Path path();

        public static Configuration create(Path path) {
            return builder()
                    .path(path)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_FileCallback_Configuration.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder path(Path path);

            public abstract Configuration build();
        }

    }

}
