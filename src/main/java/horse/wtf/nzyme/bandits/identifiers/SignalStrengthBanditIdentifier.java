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

package horse.wtf.nzyme.bandits.identifiers;

import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;

import java.util.Optional;

public class SignalStrengthBanditIdentifier implements BanditIdentifier {

    private final int from;
    private final int to;

    public SignalStrengthBanditIdentifier(int from, int to) {
        if (from > 0 || to < -100 || from <= to) {
            throw new IllegalArgumentException();
        }

        this.from = from;
        this.to = to;
    }

    @Override
    public Descriptor descriptor() {
        return Descriptor.create(
                TYPE.SIGNAL_STRENGTH,
                "Matches if the frame signal strength is within expected range.",
                "(frame.signal_quality >= " + to + " AND frame.signal_quality <= " + from + ")"
        );
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    private Optional<Boolean> match(int signalStrength) {
        return Optional.of(signalStrength >= to && signalStrength <= from);
    }

}
