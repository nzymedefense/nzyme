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

package app.nzyme.core.util;

import com.google.common.base.CharMatcher;
import app.nzyme.core.configuration.InvalidConfigurationException;
import org.simplejavamail.api.email.Recipient;

import javax.mail.Message;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    private static final CharMatcher SAFE_PARAMETER = CharMatcher.javaLetterOrDigit()
            .or(CharMatcher.whitespace())
            .or(CharMatcher.anyOf("_.-/:"))
            .precomputed();

    private static final Pattern SAFE_ID = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final Pattern EMAIL_RECIPIENT_PATTERN = Pattern.compile("^(.+)<(.+)>$");

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

    public static boolean isSafeParameter(String x) {
        if (x == null) {
            return true;
        }

        return SAFE_PARAMETER.matchesAllOf(x);
    }

    public static boolean isSafeNodeName(String x) {
        if (x == null) {
            return false;
        }

        if (x.trim().isEmpty()) {
            return false;
        }

        return x.length() < 255 && SAFE_ID.matcher(x).matches();
    }

    public static Recipient parseEmailAddress(String s) throws InvalidConfigurationException {
        try {
            Matcher matcher = EMAIL_RECIPIENT_PATTERN.matcher(s);
            if (!matcher.find()) {
                throw new InvalidConfigurationException("Invalid email address: (no match) [" + s + "] (correct format: \"Some Body <somebody@example.org>\"");
            } else {
                return new Recipient(matcher.group(1).trim(), matcher.group(2).trim(), Message.RecipientType.TO); // TO even if it's FROM is a library weirdness here
            }
        } catch(Exception e){
            throw new InvalidConfigurationException("Invalid email address: [" + s + "] (correct format: \"Some Body <somebody@example.org>\"", e);
        }
    }

}
