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

import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.ResourcesAccessingTest;
import app.nzyme.core.configuration.node.NodeConfigurationLoader;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import static org.testng.Assert.*;

public class NzymeNodeConfigurationLoaderTest extends ResourcesAccessingTest {

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testGetConfigWithNonExistentFile() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        new NodeConfigurationLoader(new File("idontexist.conf"), false).get();
    }

    @Test
    public void testGetValidConfig() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        String configFile = "nzyme-test-complete-valid.conf.test";
        if (System.getProperty("os.name").startsWith("Windows")) {
            configFile = "nzyme-test-complete-valid-windows.conf.test";
            System.out.println("loading Windows nzyme configuration file");
        }

        NodeConfiguration c = new NodeConfigurationLoader(loadFromResourceFile(configFile), false).get();

        assertFalse(c.databasePath().isEmpty()); // This one is different based on ENV vars
        assertTrue(c.fetchOuis());
        assertTrue(c.versionchecksEnabled());
        assertEquals(c.restListenUri(), URI.create("https://127.0.0.1:23900/"));
    }

    @Test(expectedExceptions = IncompleteConfigurationException.class)
    public void testGetInvalidConfigIncomplete() throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        new NodeConfigurationLoader(loadFromResourceFile("nzyme-test-incomplete.conf.test"), false).get();
    }

}