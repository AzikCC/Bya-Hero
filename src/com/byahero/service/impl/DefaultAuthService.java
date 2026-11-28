package com.byahero.service.impl;

import com.byahero.model.AdminAccount;
import com.byahero.repository.ReadRepository;
import com.byahero.service.AuthService;
import com.byahero.util.PasswordHasher;
import com.byahero.util.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Checks a login. After 5 wrong passwords for one username, that username is
 * locked for 5 minutes (this slows down someone who tries to guess a password).
 * Only reads accounts (ReadRepository), it can never change them.
 */
public class DefaultAuthService implements AuthService {

    private static final int MAX_FAILS = 5;
    private static final long LOCK_MILLIS = 5 * 60 * 1000L;
    private static final String BAD_LOGIN = "Wrong username or password.";

    private final ReadRepository<AdminAccount> accounts;
    private final Map<String, int[]> fails = new HashMap<String, int[]>();      // username -> [count]
    private final Map<String, Long> lockedUntil = new HashMap<String, Long>();

    public DefaultAuthService(ReadRepository<AdminAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public synchronized String login(String username, String password) {
        String name = Validator.clean(username).toLowerCase();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Please enter your username and password.");
        }

        Long until = lockedUntil.get(name);
        if (until != null) {
            if (System.currentTimeMillis() < until) {
                throw new IllegalArgumentException("Too many wrong tries. Please wait 5 minutes and try again.");
            }
            lockedUntil.remove(name);
            fails.remove(name);
        }

        AdminAccount account = accounts.findById(name);
        if (account != null && PasswordHasher.matches(password, account.getSalt(), account.getPasswordHash())) {
            fails.remove(name);
            return account.getUsername();
        }

        // wrong login: count it (same message whether the username exists or not)
        int[] counter = fails.get(name);
        if (counter == null) {
            counter = new int[1];
            fails.put(name, counter);
        }
        counter[0]++;
        if (counter[0] >= MAX_FAILS) {
            lockedUntil.put(name, System.currentTimeMillis() + LOCK_MILLIS);
        }
        throw new IllegalArgumentException(BAD_LOGIN);
    }
}
