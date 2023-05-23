package app.nzyme.core.security.authentication;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/*
 * This code is copied from the dev.samstevens.totp repository, which is also bundled in nzyme. This copy
 * is used to change the CODE_LENGTH parameter to increase entropy of the generated codes.
 *
 * This code is licensed under the MIT License, just like the original code.
 *
 * Copyright (c) 2019 Sam Stevens, 2023 nzyme LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

public class RecoveryCodes {

    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int CODE_LENGTH = 24;
    private static final int GROUPS_NBR = 6;

    private Random random = new SecureRandom();

    public String[] generateCodes(int amount) {
        // Must generate at least one code
        if (amount < 1) {
            throw new InvalidParameterException("Amount must be at least 1.");
        }

        // Create an array and fill with generated codes
        String[] codes = new String[amount];
        Arrays.setAll(codes, i -> generateCode());

        return codes;
    }

    private String generateCode() {
        final StringBuilder code = new StringBuilder(CODE_LENGTH + (CODE_LENGTH/GROUPS_NBR) - 1);

        for (int i = 0; i < CODE_LENGTH; i++) {
            // Append random character from authorized ones
            code.append(CHARACTERS[random.nextInt(CHARACTERS.length)]);

            // Split code into groups for increased readability
            if ((i+1) % GROUPS_NBR == 0 && (i+1) != CODE_LENGTH) {
                code.append("-");
            }
        }

        return code.toString();
    }

}
