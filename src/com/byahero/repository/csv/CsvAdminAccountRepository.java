package com.byahero.repository.csv;

import com.byahero.model.AdminAccount;

/** Saves and loads admin accounts using admins.csv. The "id" of an account is its username. */
public class CsvAdminAccountRepository extends CsvRepository<AdminAccount> {

    public static final String HEADER = "username,salt,passwordHash";

    public CsvAdminAccountRepository(String filePath) {
        super(filePath, HEADER, 3, "admin account");
    }

    @Override
    protected AdminAccount fromRow(String[] row) {
        return new AdminAccount(row[0], row[1], row[2]);
    }

    @Override
    protected String toLine(AdminAccount account) {
        return CsvUtil.clean(account.getUsername()) + "," + CsvUtil.clean(account.getSalt())
                + "," + CsvUtil.clean(account.getPasswordHash());
    }

    @Override
    protected String idOf(AdminAccount account) {
        return account.getUsername();
    }

    @Override
    protected String alreadyExistsMessage(AdminAccount account) {
        return "Admin " + account.getUsername() + " already exists.";
    }

    @Override
    protected String notFoundMessage(AdminAccount account) {
        return "No admin found with username " + account.getUsername() + ".";
    }
}
