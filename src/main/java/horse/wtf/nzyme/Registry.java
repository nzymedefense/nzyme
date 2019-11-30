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

package horse.wtf.nzyme;

import com.google.common.collect.Maps;

import java.util.Map;

public class Registry {

    public enum KEY {
        NEW_VERSION_AVAILABLE
    }

    private final Map<KEY, Boolean> booleans;

    public Registry() {
        this.booleans = Maps.newConcurrentMap();
    }

    public boolean getBool(KEY key) {
        return booleans.getOrDefault(key, false);
    }

    public void setBool(KEY key, boolean value) {
        booleans.put(key, value);
    }

}
