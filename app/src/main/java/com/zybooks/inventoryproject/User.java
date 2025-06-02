/*
 * User.java
 * This class represents a user in the inventory management application.
 * It stores user information, including ID, username, password, and a hashed password,
 * and provides methods for password hashing and equality comparison.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User class encapsulates user data, including ID, username, password, and password hash.
 * It supports password hashing using SHA-256 and custom equality comparison based on
 * username and hash.
 */
public class User {
    // Instance variables
    private final int id; // Unique identifier for the user
    private final String user; // Username for login
    private final String password; // Plain-text password (used only during hashing)
    private String hash; // SHA-256 hash of the password

    /**
     * Constructor for User.
     * Initializes a user with the provided ID, username, password, and optional hash.
     * If no hash is provided, computes a new hash from the password.
     *
     * @param id       The unique ID for the user
     * @param user     The username
     * @param password The plain-text password
     * @param hash     The precomputed password hash, or null to compute a new hash
     */
    public User(int id, String user, String password, String hash) {
        this.id = id;
        this.user = user;
        this.password = password;
        // Compute hash if none provided (e.g., during user creation/login)
        if (hash == null) {
            ComputeHash();
        } else {
            this.hash = hash; // Use provided hash (e.g., from database)
        }
    }

    /**
     * Gets the user's ID.
     *
     * @return The user's unique ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the user's username.
     *
     * @return The username
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Gets the user's password hash.
     *
     * @return The SHA-256 hash of the password
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * Computes a SHA-256 hash of the password and stores it in the hash field.
     * Called automatically during construction if no hash is provided.
     */
    private void ComputeHash() {
        try {
            // Initialize SHA-256 message digest
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(this.password.getBytes()); // Add password bytes to digest

            // Compute the hash
            byte[] bytes = md.digest();

            // Convert byte array to hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            this.hash = sb.toString(); // Store the computed hash
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Log error if SHA-256 is unavailable
        }
    }

    /**
     * Compares this user to another object for equality.
     * Two users are equal if their usernames and password hashes match.
     *
     * @param obj The object to compare with
     * @return True if the users are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;

        User other = (User) obj;
        return this.user.equals(other.user) && this.hash.equals(other.hash);
    }

    /**
     * Generates a hash code for the user based on username and password hash.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        int result1 = hash != null ? hash.hashCode() : 0;
        return 31 * result + result1; // Combine hash codes for consistency
    }
}