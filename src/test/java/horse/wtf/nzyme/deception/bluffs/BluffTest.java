package horse.wtf.nzyme.deception.bluffs;

import horse.wtf.nzyme.configuration.Configuration;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class BluffTest {

    @Test
    public void testGetInvokedCommand() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/env python");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/env python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomPython() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/python");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffDirectory() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/python");
        configuration.setBluffDirectory("/var/tmp");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /var/tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffPrefix() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/python");
        configuration.setBluffPrefix("nzymeTEST_");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzymeTEST_MockBluff --test mock");
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testParameterValueValidation() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/env python");

        Bluff mock = new MockBluff(configuration, "param & /usr/bin/FOOKED");
        mock.execute();
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testStaticParameterValidation() throws Bluff.InsecureParametersException, Bluff.BluffExecutionException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPython("/usr/bin/env python");

        Bluff mock = new MockBluff(configuration, "mock");
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

    private class TestableConfiguration extends Configuration {

        public void setPython(String python) {
            this.python = python;
        }

        public void setBluffDirectory(String bluffDirectory) {
            this.bluffDirectory = bluffDirectory;
        }

        public void setBluffPrefix(String bluffPrefix) {
            this.bluffPrefix = bluffPrefix;
        }

    }

}