package com.byahero.service.impl;

import com.byahero.model.AdminAccount;
import com.byahero.repository.Repository;
import com.byahero.service.AdminAccountService;
import com.byahero.util.PasswordHasher;

import java.util.List;

public class DefaultAdminAccountService implements AdminAccountService {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private final Repository<AdminAccount> repo;

    public DefaultAdminAccountService(Repository<AdminAccount> repo) {
        this.repo = repo;
    }

    /**
     * First start only: if there is no admin yet, nobody could ever log in.
     * So we create "admin" with the given password. Returns true if it was created.
     */
    public boolean createFirstAdminIfNone(String username, String password) {
        if (!repo.findAll().isEmpty()) {
            return false;
        }
        addAccount(username, password);
        return true;
    }

    @Override
    public List<AdminAccount> getAllAccounts() {
        return repo.findAll();
    }

    @Override
    public void addAccount(String username, String password) {
        checkPassword(password);
        String salt = PasswordHasher.newSalt();
        repo.save(new AdminAccount(username, salt, PasswordHasher.hash(password, salt)));
    }

    @Override
    public void changePassword(String username, String newPassword) {
        checkPassword(newPassword);
        AdminAccount existing = repo.findById(username);
        if (existing == null) {
            throw new IllegalArgumentException("No admin found with username " + username + ".");
        }
        String salt = PasswordHasher.newSalt();
        repo.update(new AdminAccount(existing.getUsername(), salt, PasswordHasher.hash(newPassword, salt)));
    }

    @Override
    public void deleteAccount(String username) {
        if (repo.findAll().size() <= 1) {
            throw new IllegalArgumentException("You cannot delete the last admin account.");
        }
        if (!repo.delete(username)) {
            throw new IllegalArgumentException("No admin found with username " + username + ".");
        }
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }
}
