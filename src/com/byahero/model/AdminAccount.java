package com.byahero.model;

import com.byahero.util.Validator;

/**
 * One person who may open the Admin page (one row of admins.csv).
 * The real password is NEVER stored: only a random "salt" and the "hash" made from
 * salt + password. (Hashing is one-way, so the password cannot be read back.)
 */
public class AdminAccount {

    private String username;
    private final String salt;
    private final String passwordHash;

    public AdminAccount(String username, String salt, String passwordHash) {
        setUsername(username);
        if (Validator.isBlank(salt) || Validator.isBlank(passwordHash)) {
            throw new IllegalArgumentException("Admin account needs a salt and a password hash.");
        }
        this.salt = salt.trim();
        this.passwordHash = passwordHash.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (Validator.isBlank(username)) {
            throw new IllegalArgumentException("Username must not be blank.");
        }
        String clean = username.trim();
        if (!clean.matches("[A-Za-z0-9._-]{3,30}")) {
            throw new IllegalArgumentException("Username must be 3-30 letters, numbers, dots, dashes or underscores.");
        }
        this.username = clean;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
