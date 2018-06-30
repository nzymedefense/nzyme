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

import com.google.common.base.CharMatcher;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Tools {

    private static final CharMatcher SAFE_PARAMETER = CharMatcher.javaLetterOrDigit()
            .or(CharMatcher.whitespace())
            .or(CharMatcher.anyOf("_.-/:"))
            .precomputed();

    public static boolean isValidUTF8( byte[] input ) {
        CharsetDecoder cs = Charset.forName("UTF-8").newDecoder();

        try {
            cs.decode(ByteBuffer.wrap(input));
            return true;
        }
        catch(CharacterCodingException e){
            return false;
        }
    }

    public static int calculateSignalQuality(int antennaSignal) {
        if(antennaSignal >= -50) {
            return 100;
        }

        if(antennaSignal <= -100) {
            return 0;
        }

        return 2*(antennaSignal+100);
    }

    public static boolean isSafeParameter(String x) {
        if (x == null) {
            return true;
        }

        return SAFE_PARAMETER.matchesAllOf(x);
    }

    public static String safeAlphanumericString(String x) {
        return x.replaceAll("[^A-Za-z0-9]", "");
    }

}
