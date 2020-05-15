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

package horse.wtf.nzyme.util;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.dot11.interceptors.CryptoChangeInterceptorSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dot11CryptoComparator {

    private static final Logger LOG = LogManager.getLogger(CryptoChangeInterceptorSet.class);

    public static boolean compareSecurity(List<String> encountered, List<String> expected) {
        if (expected.size() != encountered.size()) {
            LOG.debug("Unexpected security settings: expected size <{}> does not match encountered size <{}>.", expected.size(), encountered.size());
            return false;
        }

        final AtomicBoolean result = new AtomicBoolean(true);
        List<String> seen = Lists.newArrayList();
        encountered.forEach((e) -> {
            if(seen.contains(e)) {
                LOG.warn("Unexpected security settings: Encountered contains duplicate security [{}]. This is not 802.11 compliant and highly unusual.", e);
                result.set(false);
            }
        });

        if(!result.get()) {
            return false;
        }

        expected.forEach((e) -> {
            if (!encountered.contains(e)) {
                LOG.debug("Unexpected security settings: Expected security [{}] not in encountered [{}].", e, encountered);
                result.set(false);
            }
        });

        return result.get();
    }

}
