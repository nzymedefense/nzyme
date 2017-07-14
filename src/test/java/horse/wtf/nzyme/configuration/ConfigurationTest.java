package horse.wtf.nzyme.configuration;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConfigurationTest {

    @Test
    public void testGetGraylogAddressesWithSingleValue() throws Exception {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setParameterGraylogAddresses("localhost:12000");

        assertEquals(configuration.getGraylogAddresses().size(), 1);
    }

    @Test
    public void testGetGraylogAddressesWithMultipleValues() throws Exception {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setParameterGraylogAddresses("localhost:12000,example.org:12500");

        assertEquals(configuration.getGraylogAddresses().size(), 2);

        assertEquals(configuration.getGraylogAddresses().get(0).getHost(), "localhost");
        assertEquals(configuration.getGraylogAddresses().get(0).getPort(), 12000);

        assertEquals(configuration.getGraylogAddresses().get(1).getHost(), "example.org");
        assertEquals(configuration.getGraylogAddresses().get(1).getPort(), 12500);
    }

    private class TestableConfiguration extends Configuration {

        public void setParameterGraylogAddresses(String addresses) {
            this.graylogAddresses = addresses;
        }

    }

}