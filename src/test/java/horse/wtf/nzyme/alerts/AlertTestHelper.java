/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.alerts;

import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.configuration.BanditFingerprintDefinition;
import horse.wtf.nzyme.configuration.Dot11BSSIDDefinition;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.statistics.Statistics;

import java.util.*;


public class AlertTestHelper {

    public static final String CLEAR_QUERY = "DELETE FROM alerts";

    protected static final Map<String, BanditFingerprintDefinition> BANDITS_STANDARD = new HashMap<String, BanditFingerprintDefinition>(){{
        put("ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                BanditFingerprintDefinition.create(
                        "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                        new ArrayList<String>(){{
                            add("WiFi Pineapple Nano or Tetra (PineAP)");
                            add("spacehuhn/esp8266_deauther (attack frames)");
                        }}));
        put("535afea1f1656375a991e28ce919d412fd9863a01f1b0b94fcff8a83ed8fcb83",
                BanditFingerprintDefinition.create(
                        "535afea1f1656375a991e28ce919d412fd9863a01f1b0b94fcff8a83ed8fcb83",
                        new ArrayList<String>(){{
                            add("WiFi Pineapple Nano or Tetra (PineAP)");
                        }}));
    }};

    protected Dot11MockProbe buildMockProbe(Map<String, BanditFingerprintDefinition> bandits, MockNzyme nzyme) {
        return new Dot11MockProbe(nzyme, Dot11ProbeConfiguration.create(
                "test-probe-1",
                Collections.emptyList(),
                "nzyme-testng-1",
                "foo",
                Collections.emptyList(),
                0,
                "foo",
                new ArrayList<Dot11NetworkDefinition>(){{
                    add(Dot11NetworkDefinition.create(
                            "WTF",
                            new ArrayList<Dot11BSSIDDefinition>(){{
                                add(Dot11BSSIDDefinition.create("00:c0:ca:95:68:3b", ImmutableList.of("foo")));
                            }},
                            new ArrayList<Integer>() {{
                                add(1);
                                add(6);
                                add(11);
                            }},
                            new ArrayList<String>() {{
                                add("WPA1-EAM-PSK-CCMP");
                                add("WPA2-EAM-PSK-CCMP");
                            }}, 0)
                    );
                }},
                Collections.emptyList(),
                BANDITS_STANDARD
        ), new Statistics(nzyme));
    }

    protected static final Dot11ProbeConfiguration CONFIG_STANDARD = Dot11ProbeConfiguration.create(
            "mockProbe1",
            Collections.emptyList(),
            "test1",
            "wlan0",
            Collections.emptyList(),
            1,
            "foo",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyMap()
    );


}
