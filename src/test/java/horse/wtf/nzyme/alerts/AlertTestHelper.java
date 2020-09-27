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
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;

import java.util.*;

public class AlertTestHelper {

    public static final String CLEAR_QUERY = "DELETE FROM alerts";

    protected static final Dot11ProbeConfiguration CONFIG_STANDARD = Dot11ProbeConfiguration.create(
            "mockProbe1",
            ImmutableList.of(),
            "test1",
            "wlan0",
            ImmutableList.of(),
            1,
            "foo",
            ImmutableList.of(),
            ImmutableList.of()
    );


}
