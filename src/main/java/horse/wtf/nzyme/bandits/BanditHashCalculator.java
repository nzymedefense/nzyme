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

package horse.wtf.nzyme.bandits;

import com.google.common.hash.Hashing;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BanditHashCalculator {

    public static String calculate(List<Bandit> bandits) {
        StringBuilder x = new StringBuilder();

        List<Bandit> sortedBandits = new ArrayList<>(bandits);
        sortedBandits.sort(Comparator.comparing(Bandit::uuid));

        for (Bandit bandit : sortedBandits) {
            x.append(bandit.uuid().toString());

            if (bandit.identifiers() != null) {
                List<BanditIdentifier> sortedIdentifiers = new ArrayList<>(bandit.identifiers());
                sortedIdentifiers.sort(Comparator.comparing(BanditIdentifier::getUuid));

                for (BanditIdentifier identifier : sortedIdentifiers) {
                    x.append(identifier.getUuid()).append(identifier.descriptor().matches());
                }
            }
        }

        //noinspection UnstableApiUsage
        return Hashing.sha256().hashString(x.toString(), StandardCharsets.UTF_8).toString();
    }

}
