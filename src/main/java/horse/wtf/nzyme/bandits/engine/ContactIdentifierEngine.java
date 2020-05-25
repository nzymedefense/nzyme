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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.bandits.Identifiable;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.util.MetricNames;

import java.util.Optional;

public class ContactIdentifierEngine {

    private final Timer timing;

    public ContactIdentifierEngine(MetricRegistry metrics) {
        this.timing = metrics.timer(MetricNames.CONTACT_IDENTIFIER_TIMING);
    }

    public boolean identify(Dot11Frame frame, Identifiable bandit) {
        if (bandit.identifiers() == null || bandit.identifiers().isEmpty()) {
            return false;
        }

        Timer.Context timer = this.timing.time();
        try {
            for (BanditIdentifier identifier : bandit.identifiers()) {
                if (frame instanceof Dot11BeaconFrame) {
                    Optional<Boolean> matches = identifier.matches((Dot11BeaconFrame) frame);
                    if (matches.isPresent() && matches.get()) {
                        return true;
                    }
                }

                if (frame instanceof Dot11ProbeResponseFrame) {
                    Optional<Boolean> matches = identifier.matches((Dot11ProbeResponseFrame) frame);
                    if (matches.isPresent() && matches.get()) {
                        return true;
                    }
                }

                if (frame instanceof Dot11DeauthenticationFrame) {
                    Optional<Boolean> matches = identifier.matches((Dot11DeauthenticationFrame) frame);
                    if (matches.isPresent() && matches.get()) {
                        return true;
                    }
                }
            }
        } finally {
            timer.stop();
        }

        return false;
    }

}
