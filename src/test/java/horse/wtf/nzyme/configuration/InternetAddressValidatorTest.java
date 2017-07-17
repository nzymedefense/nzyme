/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

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