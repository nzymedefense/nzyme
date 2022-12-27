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

package app.nzyme.core.configuration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationValidator {

    private static final Logger LOG = LogManager.getLogger(ConfigurationValidator.class);


    public static void expect(Config c, String key, String where, Class clazz) throws IncompleteConfigurationException, InvalidConfigurationException {
        boolean incomplete = false;
        boolean invalid = false;

        try {
            // String.
            if (clazz.equals(String.class)) {
                incomplete = Strings.isNullOrEmpty(c.getString(key));
            }

            // Boolean.
            if (clazz.equals(Boolean.class)) {
                c.getBoolean(key);
            }

            // List
            if (clazz.equals(ImmutableList.class)) {
                c.getList(key);
            }

            if (clazz.equals(Integer.class)) {
                c.getInt(key);
            }
        } catch(ConfigException.Missing e) {
            LOG.error(e);
            incomplete = true;
        } catch(ConfigException.WrongType e) {
            LOG.error(e);
            invalid = true;
        } catch (ConfigException e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parsing error for parameter [" + key + "] in section [" + where + "].");
        }

        if (incomplete) {
            throw new IncompleteConfigurationException("Missing parameter [" + key + "] in section [" + where + "].");
        }

        if (invalid) {
            throw new InvalidConfigurationException("Invalid value for parameter [" + key + "] in section [" + where + "].");
        }
    }

    public static void expectEnum(Config c, String key, String where, Class enumClazz) throws InvalidConfigurationException, IncompleteConfigurationException {
        try {
            c.getEnum(enumClazz, key);
        } catch(ConfigException.Missing e) {
            throw new IncompleteConfigurationException("Missing parameter [" + key + "] in section [" + where + "].");
        } catch(ConfigException.BadValue e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Invalid value for parameter [" + key + "] in section [" + where + "].");
        }
    }

}
