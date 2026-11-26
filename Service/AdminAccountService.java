package com.byahero.service;

import com.byahero.model.AdminAccount;

import java.util.List;

/** Admin actions for admin accounts only. */
public interface AdminAccountService {

    List<AdminAccount> getAllAccounts();

    void addAccount(String username, String password);

    /** Sets a new password for an existing account. */
    void changePassword(String username, String newPassword);

    /** Deletes an account. The last remaining account can never be deleted. */
    void deleteAccount(String username);
}
