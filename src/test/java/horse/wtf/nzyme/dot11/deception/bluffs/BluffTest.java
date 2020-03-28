package horse.wtf.nzyme.dot11.deception.bluffs;

import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.ConfigurationLoader;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class BluffTest {

    private static Configuration buildConfiguration(String pythonExecutable, String pythonScriptDirectory, String pythonScriptPrefix) {
        return Configuration.create(
                false,
                false,
                Role.LEADER,
                "testng",
                "95d30169a59c418b52013315fc81bc99fdf0a7b03a116f346ab628496f349ed5",
                "nzyme-test.db",
                pythonExecutable,
                pythonScriptDirectory,
                pythonScriptPrefix,
                false,
                new File("").toPath(),
                new File("").toPath(),
                URI.create("http://localhost:23444"),
                URI.create("http://localhost:23444"),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                30,
                300,
                Collections.emptyList()
        );
    }

    private static final Configuration STANDARD_CONFIG = buildConfiguration("/usr/bin/env python", "/tmp", "nzyme_");

    @Test
    public void testGetInvokedCommand() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/env python", "/tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/env python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomPython() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python", "tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffDirectory() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python", "/var/tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /var/tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffPrefix() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python", "/tmp", "nzymeTEST_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzymeTEST_MockBluff --test mock");
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testParameterValueValidation() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(STANDARD_CONFIG, "param & /usr/bin/FOOKED");
        mock.execute();
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testStaticParameterValidation() throws Bluff.InsecureParametersException, Bluff.BluffExecutionException, ConfigurationLoader.InvalidConfigurationException, ConfigurationLoader.IncompleteConfigurationException {
        Bluff mock = new MockBluff(STANDARD_CONFIG, "mock");
        ((MockBluff) mock).setParameterKey("-i foo & /usr/bin/fooked &");
        mock.execute();
    }

    private class MockBluff extends Bluff {

        private final String testParam;

        private String parameterKey = "--test";

        public MockBluff(Configuration configuration, String testParam) {
            super(configuration);

            this.testParam = testParam;
        }

        @Override
        protected String scriptCategory() {
            return "test";
        }

        @Override
        protected String scriptName() {
            return "mock.py";
        }

        public void setParameterKey(String parameterKey) {
            this.parameterKey = parameterKey;
        }

        @Override
        protected Map<String, String> parameters() {
            return new HashMap<String, String>() {{
                put(parameterKey, testParam);
            }};
        }

    }

}