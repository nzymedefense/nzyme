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

    @Test
    public void testGetChannels() throws Exception {
        TestableConfiguration configuration = new TestableConfiguration();
        configuration.setParameterChannels("1,2,3,4,5,6,7,8,9,10,11");

        assertEquals(configuration.getChannels().size(), 11);
        assertEquals(configuration.getChannels().get(0), new Integer(1));
        assertEquals(configuration.getChannels().get(4), new Integer(5));
        assertEquals(configuration.getChannels().get(10), new Integer(11));
    }

    private class TestableConfiguration extends Configuration {

        public void setParameterGraylogAddresses(String addresses) {
            this.graylogAddresses = addresses;
        }

        public void setParameterChannels(String channels) {
            this.channels = channels;
        }

    }

}