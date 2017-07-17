package horse.wtf.nzyme.configuration;

import com.github.joschi.jadconfig.ValidationException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class InternetAddressValidatorTest {

    @Test
    public void testValidateSingleAddress() throws Exception {
        try {
            InternetAddressValidator v = new InternetAddressValidator();
            v.validate("graylog_addresses", "127.0.0.1:12000");
        } catch(ValidationException e) {
            fail("Validation failed.", e);
        }
    }

    @Test
    public void testValidateMultipleAddresses() throws Exception {
        try {
            InternetAddressValidator v = new InternetAddressValidator();
            v.validate("graylog_addresses", "127.0.0.1:12000,foo.example.org:12500");
        } catch(ValidationException e) {
            fail("Validation failed.", e);
        }
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForEmptyAddress() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForCompletelyMalformedAddress() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "gfdgfdgfd");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithMissingPort() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithMissingPort2() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org:");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithInvalidPort() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org:what");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithTooHighPort() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org:999999999999");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithTooLowPort() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org:-9001");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidadtionFailsForAddressWithZeroPort() throws Exception {
        InternetAddressValidator v = new InternetAddressValidator();
        v.validate("graylog_addresses", "example.org:0");
    }

}