package com.byahero.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Safe password storage using PBKDF2 (built into Java, no extra library).
 * Every password gets its own random salt, so two admins with the same password
 * still have different hashes.
 */
public class PasswordHasher {

    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Makes a new random salt (as text that is safe to put in a CSV). */
    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Turns salt + password into the hash text we store. */
    public static String hash(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt),
                    ITERATIONS, KEY_BITS);
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available.", e);
        }
    }

    /** True if this password gives the stored hash. Compares in constant time. */
    public static boolean matches(String password, String salt, String expectedHash) {
        byte[] actual = hash(password, salt).getBytes();
        byte[] expected = expectedHash.getBytes();
        return MessageDigest.isEqual(actual, expected);
    }
}
