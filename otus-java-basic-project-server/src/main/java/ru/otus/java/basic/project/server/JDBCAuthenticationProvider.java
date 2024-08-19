package ru.otus.java.basic.project.server;

import ru.otus.java.basic.project.server.exceptions.AuthenticationException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.Random;

public class JDBCAuthenticationProvider implements AuthenticationProvider, AutoCloseable {
    private final Connection connection;

    /**
     * Creates an authentication provider based on PostgresSQL database.
     * Establishes PostgresSQL connection based on environment variables:
     * <ul>
     *     <li>OTUS_PROJECT_DB_ADDR=address:port/database_name</li>
     *     <li>OTUS_PROJECT_DB_USER=database_user_name</li>
     *     <li>OTUS_PROJECT_DB_PWD=database_user_password</li>
     * </ul>
     */
    public JDBCAuthenticationProvider() throws AuthenticationException {
        String address = System.getenv("OTUS_PROJECT_DB_ADDR");
        String user = System.getenv("OTUS_PROJECT_DB_USER");
        String password = System.getenv("OTUS_PROJECT_DB_PWD");
        if (address == null || user == null || password == null) {
            throw new AuthenticationException("Environment variables (OTUS_PROJECT_DB_ADDR, OTUS_PROJECT_DB_USER, OTUS_PROJECT_DB_PWD) " +
                    "are not set correctly");
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://" + address, user, password);
        } catch (SQLException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Calculates SHA-256 of two concatenated strings
     *
     * @param string     a string
     * @param saltString another string
     * @return Base64 encoding of SHA-256 of the concatenation of the two stings
     */
    private String getSaltedHash(String string, String saltString) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(
                            (saltString + string).getBytes()
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authenticates a user by a login and a password combination.
     *
     * @param login    the login
     * @param password the password
     * @throws AuthenticationException in case of authentication failure
     */
    @Override
    public synchronized void authenticate(String login, String password) throws AuthenticationException {
        try {
            if (!isLoginPasswordMatch(login, password)) {
                throw new AuthenticationException("Incorrect login/password");
            }
        } catch (SQLException e) {
            throw new AuthenticationException("Database error while authenticating", e);
        }
    }

    /**
     * Checks if login and password matches some database entry
     *
     * @param login    a login
     * @param password a password
     * @return true if match is found, false otherwise
     * @throws SQLException in case of database failure
     */
    private boolean isLoginPasswordMatch(String login, String password) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT password_salted_hash, password_salt FROM authentication.user WHERE login = ?"
        )) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String password_salted_hash = result.getString(1);
                    String password_salt = result.getString(2);
                    if (password_salted_hash.equals(getSaltedHash(password, password_salt))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a login already exists in the database
     *
     * @param login a login to look up
     * @return true if already exits, false otherwise
     * @throws SQLException in case of database failure
     */
    private boolean isLoginExists(String login) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM authentication.user WHERE login = ?"
        )) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds user to the database
     *
     * @param login    a login
     * @param password a password
     * @throws SQLException in case of database failure
     */
    private void addUser(String login, String password) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO authentication.user (login, password_salted_hash, password_salt)"
                        + "VALUES (?, ?, ?)"
        )) {
            String passwordSalt = new Random()
                    .ints((int) 'A', (int) 'z')
                    .limit(16)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            statement.setString(1, login);
            statement.setString(2, getSaltedHash(password, passwordSalt));
            statement.setString(3, passwordSalt);
            statement.executeUpdate();
        }
    }

    /**
     * Registers a user given a login and a password combination.
     *
     * @param login    the login
     * @param password the password
     * @throws AuthenticationException in case of a registration failure
     */
    @Override
    public synchronized void register(String login, String password) throws AuthenticationException {
        try {
            if (login.length() < 3 || login.length() > 15 || password.length() < 6) {
                throw new AuthenticationException("Login must be 3..15 symbols, password 6+ symbols");
            }
            if (!login.chars().allMatch(Character::isLetterOrDigit)) {
                throw new AuthenticationException("Login must contain only alphanumeric characters");
            }
            if (isLoginExists(login)) {
                throw new AuthenticationException("Login is already registered");
            }
            addUser(login, password);
        } catch (SQLException e) {
            throw new AuthenticationException("Database error while registering", e);
        }
    }

    /**
     * Closes JDBC connection
     *
     * @throws SQLException in case of database failure
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
