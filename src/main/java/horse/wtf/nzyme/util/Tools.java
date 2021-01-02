/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.util;

import com.google.common.base.CharMatcher;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Pattern;

public class Tools {

    private static final CharMatcher SAFE_PARAMETER = CharMatcher.javaLetterOrDigit()
            .or(CharMatcher.whitespace())
            .or(CharMatcher.anyOf("_.-/:"))
            .precomputed();

    private static final Pattern SAFE_ID = Pattern.compile("^[a-zA-Z0-9-_]+$");

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

    public static boolean isHumanlyReadable(String string) {
        int length = string.length();

        // Check if it only consists of control chars or whitespaces.
        int controlChars = 0;
        int whitespaces = 0;
        for (char c : string.toCharArray()) {
            if (Character.isISOControl(c)) {
                controlChars++;
            }

            if (Character.isISOControl(c)) {
                whitespaces++;
            }
        }

        if (length == controlChars || length == whitespaces) {
            return false;
        }

        return true;
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

    public static boolean isSafeID(String x) {
        return SAFE_ID.matcher(x).matches();
    }

    public static String byteArrayToHexPrettyPrint(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b)).append(" ");
        return sb.toString();
    }

    public static String safeAlphanumericString(String x) {
        return x.replaceAll("[^A-Za-z0-9]", "");
    }

    public static Integer getInteger(Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        throw new RuntimeException("Cannot cast object of type [" + value.getClass().getCanonicalName() + "] to Integer.");
    }

}
