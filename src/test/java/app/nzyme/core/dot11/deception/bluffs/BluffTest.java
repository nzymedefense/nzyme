package app.nzyme.core.dot11.deception.bluffs;

import app.nzyme.core.configuration.node.NodeConfiguration;
import com.google.common.collect.ImmutableList;
import app.nzyme.core.Role;
import app.nzyme.core.configuration.DeauthenticationMonitorConfiguration;
import app.nzyme.core.configuration.IncompleteConfigurationException;
import app.nzyme.core.configuration.InvalidConfigurationException;
import org.testng.annotations.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class BluffTest {

    private static NodeConfiguration buildConfiguration(String pythonExecutable, String pythonScriptDirectory, String pythonScriptPrefix) {
        return NodeConfiguration.create(
                false,
                false,
                Role.NODE,
                "95d30169a59c418b52013315fc81bc99fdf0a7b03a116f346ab628496f349ed5",
                "nzyme-test.db",
                pythonExecutable,
                pythonScriptDirectory,
                pythonScriptPrefix,
                URI.create("http://localhost:23444"),
                URI.create("http://localhost:23444"),
                "plugin",
                "crypto_test",
                "pool.ntp.org",
                new InetSocketAddress("0.0.0.0", 9001),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                30,
                ImmutableList.of(),
                ImmutableList.of(),
                null,
                null,
                DeauthenticationMonitorConfiguration.create(10)
        );
    }

    private static final NodeConfiguration STANDARD_CONFIG = buildConfiguration("/usr/bin/python2.7", "/tmp", "nzyme_");


    @Test
    public void testGetInvokedCommand() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python2.7", "/tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python2.7 /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomPython() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python2.7", "tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python2.7 /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffDirectory() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python2.7", "/var/tmp", "nzyme_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python2.7 /var/tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffPrefix() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        Bluff mock = new MockBluff(buildConfiguration("/usr/bin/python2.7", "/tmp", "nzymeTEST_"), "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python2.7 /tmp/nzymeTEST_MockBluff --test mock");
    }

    public void testParameterValueValidation() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        try {
            Bluff mock = new MockBluff(STANDARD_CONFIG, "param & /usr/bin/FOOKED");
            mock.execute();
        } catch(Bluff.InsecureParametersException e) {
            return;
        }

        fail("Did not throw expected exception.");
    }

    public void testStaticParameterValidation() throws Bluff.InsecureParametersException, Bluff.BluffExecutionException, InvalidConfigurationException, IncompleteConfigurationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Not running bluff tests on Windows.");
            return;
        }

        try {
            Bluff mock = new MockBluff(STANDARD_CONFIG, "mock");
            ((MockBluff) mock).setParameterKey("-i foo & /usr/bin/fooked &");
            mock.execute();
        } catch(Bluff.InsecureParametersException e) {
            return;
        }

        fail("Did not throw expected exception.");
    }

    private class MockBluff extends Bluff {

        private final String testParam;

        private String parameterKey = "--test";

        public MockBluff(NodeConfiguration configuration, String testParam) {
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