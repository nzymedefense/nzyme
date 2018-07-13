package horse.wtf.nzyme.dot11.deception.bluffs;

import horse.wtf.nzyme.configuration.Configuration;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class BluffTest {

    @Test
    public void testGetInvokedCommand() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/env python");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/env python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomPython() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/python");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffDirectory() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/python");
        configuration.setPythonScriptDirectory("/var/tmp");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /var/tmp/nzyme_MockBluff --test mock");
    }

    @Test
    public void testCustomBluffPrefix() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/python");
        configuration.setPythonScriptPrefix("nzymeTEST_");

        Bluff mock = new MockBluff(configuration, "mock");
        mock.execute();

        assertEquals(mock.getInvokedCommand(), "/usr/bin/python /tmp/nzymeTEST_MockBluff --test mock");
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testParameterValueValidation() throws Bluff.BluffExecutionException, Bluff.InsecureParametersException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/env python");

        Bluff mock = new MockBluff(configuration, "param & /usr/bin/FOOKED");
        mock.execute();
    }

    @Test(expectedExceptions = Bluff.InsecureParametersException.class)
    public void testStaticParameterValidation() throws Bluff.InsecureParametersException, Bluff.BluffExecutionException {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setPythonExecutable("/usr/bin/env python");

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

        private String pythonExecutable = "/usr/bin/python";
        private String pythonScriptDirectory = "/tmp";
        private String pythonScriptPrefix = "nzyme_";

        public TestableConfiguration() {
            super(new File("nzyme-leader.conf.example"));
        }

        @Override
        public String getPythonExecutable() {
            return pythonExecutable;
        }

        public void setPythonExecutable(String pythonExecutable) {
            this.pythonExecutable = pythonExecutable;
        }

        @Override
        public String getPythonScriptDirectory() {
            return pythonScriptDirectory;
        }

        public void setPythonScriptDirectory(String pythonScriptDirectory) {
            this.pythonScriptDirectory = pythonScriptDirectory;
        }

        @Override
        public String getPythonScriptPrefix() {
            return pythonScriptPrefix;
        }

        public void setPythonScriptPrefix(String pythonScriptPrefix) {
            this.pythonScriptPrefix = pythonScriptPrefix;
        }

    }

}