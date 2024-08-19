package ru.otus.java.basic.project.server;

import ru.otus.java.basic.project.server.exceptions.AuthenticationException;

public interface AuthenticationProvider {
        /**
         * Authenticates a user by a login and a password combination.
         *
         * @param login         the login
         * @param password      the password
         * @throws AuthenticationException in case of authentication failure
         */
        void authenticate(String login, String password) throws AuthenticationException;

        /**
         * Registers a user given a login and a password combination.
         *
         * @param login         the login
         * @param password      the password
         * @throws AuthenticationException in case of a registration failure
         */
        void register(String login, String password) throws AuthenticationException;
}

