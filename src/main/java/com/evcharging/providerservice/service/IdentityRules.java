package com.evcharging.providerservice.service;

import java.util.regex.Pattern;

/** Format rules and masking for the identity documents a provider can register with. */
final class IdentityRules {

    private static final Pattern PAN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern AADHAAR = Pattern.compile("^[2-9][0-9]{11}$");

    // Verhoeff checksum tables -- the check digit scheme UIDAI uses for the 12th Aadhaar digit.
    private static final int[][] D = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 2, 3, 4, 0, 6, 7, 8, 9, 5}, {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
            {3, 4, 0, 1, 2, 8, 9, 5, 6, 7}, {4, 0, 1, 2, 3, 9, 5, 6, 7, 8}, {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
            {6, 5, 9, 8, 7, 1, 0, 4, 3, 2}, {7, 6, 5, 9, 8, 2, 1, 0, 4, 3}, {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
            {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}};
    private static final int[][] P = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 5, 7, 6, 2, 8, 3, 0, 9, 4}, {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
            {8, 9, 1, 6, 0, 4, 3, 5, 2, 7}, {9, 4, 5, 3, 1, 2, 6, 8, 7, 0}, {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
            {2, 7, 9, 3, 8, 0, 6, 4, 1, 5}, {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}};

    private IdentityRules() {
    }

    /** Returns the normalised (upper-case, no spaces) PAN, or throws if it isn't a well-formed PAN. */
    static String normalisePan(String raw) {
        String pan = raw.replaceAll("\\s", "").toUpperCase();
        if (!PAN.matcher(pan).matches()) {
            throw new IllegalArgumentException("Enter a valid PAN, like ABCDE1234F");
        }
        return pan;
    }

    /** Returns the 12 Aadhaar digits, or throws if the number is malformed or fails the checksum. */
    static String normaliseAadhaar(String raw) {
        String digits = raw.replaceAll("[\\s-]", "");
        if (!AADHAAR.matcher(digits).matches() || !verhoeffValid(digits)) {
            throw new IllegalArgumentException("Enter a valid 12-digit Aadhaar number");
        }
        return digits;
    }

    static String maskPan(String pan) {
        return "XXXXXX" + pan.substring(6);
    }

    static String maskAadhaar(String aadhaar) {
        return "XXXX XXXX " + aadhaar.substring(8);
    }

    private static boolean verhoeffValid(String num) {
        int c = 0;
        for (int i = 0; i < num.length(); i++) {
            int digit = num.charAt(num.length() - 1 - i) - '0';
            c = D[c][P[i % 8][digit]];
        }
        return c == 0;
    }
}
