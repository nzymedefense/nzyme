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

package horse.wtf.nzyme.bandits.engine;

import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.FingerprintBanditIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DefaultBandits {

    private static final Logger LOG = LogManager.getLogger(DefaultBandits.class);

    /*
     * Using this instead of Liquibase migrations because we might need to get fancy (as in, using code) in the future
     * when it comes to maintaining these.
     *
     * It is important to keep the UUIDs static and never change them, so we can always refer back to a specific entry.
     *
     * This will probably bite me later but appears to be the best approach for now.
     *
     */

    public static final List<Bandit> BANDITS = new ArrayList<Bandit>(){{
        add(Bandit.create(null, UUID.fromString("d754ab90-67f9-43bc-b5df-bd815112e55b"),
                "WiFi Pineapple Nano or Tetra (PineAP), esp8266_deauther (attack frames)",
                "[Built-in bandit definition]\n\nDetects WiFi Pineapple PineAP frames and esp8266_deauther frames, " +
                        "which appear to be cloned from Pineapple frames because they have the same fingerprint.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                            null,
                            UUID.fromString("150d5828-b7a6-4796-add3-3266da396853")));
                }})
        );
    }};

    public static void seed(ContactIdentifier contactIdentifier) {
        for (Bandit bandit : BANDITS) {
            Optional<Bandit> x = contactIdentifier.findBanditByUUID(bandit.uuid());
            if (!x.isPresent()) {
                LOG.info("Registering missing default bandit: {}", bandit);
                contactIdentifier.registerBandit(bandit);
            }
        }

    }

}
