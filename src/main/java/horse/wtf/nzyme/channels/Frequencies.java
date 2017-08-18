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

package horse.wtf.nzyme.channels;

import com.google.common.collect.ImmutableMap;

public class Frequencies {

    private static final ImmutableMap<Integer, Integer> map = new ImmutableMap.Builder<Integer, Integer>()
            // 2.4 GHz (802.11b/g/n)
            .put(2412, 1)
            .put(2417, 2)
            .put(2422, 3)
            .put(2427, 4)
            .put(2432, 5)
            .put(2437, 6)
            .put(2442, 7)
            .put(2447, 8)
            .put(2452, 9)
            .put(2457, 10)
            .put(2462, 11)
            .put(2467, 12)
            .put(2472, 13)
            .put(2484, 14)

            // 5 GHz (802.11a/h/j/n/ac)
            .put(5180, 36)
            .put(5200, 40)
            .put(5220, 44)
            .put(5240, 48)
            .put(5260, 52)
            .put(5280, 56)
            .put(5300, 60)
            .put(5320, 64)
            .put(5500, 100)
            .put(5520, 104)
            .put(5540, 108)
            .put(5560, 112)
            .put(5580, 116)
            .put(5600, 120)
            .put(5620, 124)
            .put(5640, 128)
            .put(5660, 132)
            .put(5680, 136)
            .put(5700, 140)
            .put(5745, 149)
            .put(5765, 153)
            .put(5785, 157)
            .put(5805, 161)
            .put(5825, 165)

            .build();

    public static int frequencyToChannel(int frequency) {
        if (map.containsKey(frequency)) {
            return map.get(frequency);
        } else {
            return 0;
        }
    }

}
