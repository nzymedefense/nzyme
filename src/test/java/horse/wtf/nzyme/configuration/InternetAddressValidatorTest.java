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

}