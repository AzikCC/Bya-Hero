package com.byahero.service;

/** The login rule only: "are this username and password correct?" */
public interface AuthService {

    /** Returns the real (correctly spelled) username if the login is right. Throws if it is not. */
    String login(String username, String password);
}
