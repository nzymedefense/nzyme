package app.nzyme.core.designation;

import java.math.BigInteger;

public class Designation {

    private static final String[] WORDS = {
        "Zero",    // 0
        "One",     // 1
        "Two",     // 2
        "Three",   // 3
        "Four",    // 4
        "Five",    // 5
        "Six",     // 6
        "Seven",   // 7
        "Eight",   // 8
        "Niner",   // 9
        "Alpha",   // 10 (A)
        "Bravo",   // 11 (B)
        "Charlie", // 12 (C)
        "Delta",   // 13 (D)
        "Echo",    // 14 (E)
        "Foxtrot", // 15 (F)
        "Golf",    // 16 (G)
        "Hotel",   // 17 (H)
        "India",   // 18 (I)
        "Juliet",  // 19 (J)
        "Kilo",    // 20 (K)
        "Lima",    // 21 (L)
        "Mike",    // 22 (M)
        "November",// 23 (N)
        "Oscar",   // 24 (O)
        "Papa",    // 25 (P)
        "Quebec",  // 26 (Q)
        "Romeo",   // 27 (R)
        "Sierra",  // 28 (S)
        "Tango",   // 29 (T)
        "Uniform", // 30 (U)
        "Victor",  // 31 (V)
        "Whiskey", // 32 (W)
        "Xray",    // 33 (X)
        "Yankee",  // 34 (Y)
        "Zulu"     // 35 (Z)
    };

    public static String fromSha256ShortDigest(String hex7) {
        if (hex7.length() != 7) {
            throw new RuntimeException("SHA256 short digest must be 7 characters.");
        }

        BigInteger value = new BigInteger(hex7, 16);

        // Extract 3 digits in base-36 from the least significant side.
        // Each digit is in range 0..35

        // 2. Extract the first digit.
        int digit1 = value.mod(BigInteger.valueOf(36)).intValue();
        value = value.divide(BigInteger.valueOf(36));

        // 3. Extract the second digit.
        int digit2 = value.mod(BigInteger.valueOf(36)).intValue();
        value = value.divide(BigInteger.valueOf(36));

        // 4. Extract the third digit.
        int digit3 = value.mod(BigInteger.valueOf(36)).intValue();

        // 5. Map each digit to word.
        String word1 = WORDS[digit1];
        String word2 = WORDS[digit2];
        String word3 = WORDS[digit3];

        return word3 + " " + word2 + " " + word1;
    }

}
