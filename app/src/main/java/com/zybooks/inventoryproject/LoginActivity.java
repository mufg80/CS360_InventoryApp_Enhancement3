/*
 * LoginActivity.java
 * This class handles user authentication and registration for the inventory management application.
 * It provides a UI for users to log in or register, validates credentials, and interacts with the
 * database via InventoryRepo to manage user data.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * LoginActivity manages the login and registration process.
 * It provides input fields for username and password, handles user registration with password
 * confirmation, and authenticates users against the database before redirecting to MainActivity.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Initializes the activity, sets up the UI, and configures click listeners for login and registration.
     *
     * @param savedInstanceState The saved instance state for activity recreation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        setContentView(R.layout.activity_login);

        // Adjust padding for system bars (e.g., status and navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        EditText userEditText = findViewById(R.id.user_name_edittext);
        TextView confirmTextView = findViewById(R.id.password_confirm_textview);
        EditText confirmEditText = findViewById(R.id.password_confirm_edittext);
        EditText passwordEditText = findViewById(R.id.password_edittext);
        Button submitRegisterButton = findViewById(R.id.button);
        TextView myAccountTextView = findViewById(R.id.account_textview);

        // Set click listener for the "Register new account" text to show registration fields
        myAccountTextView.setOnClickListener(v -> {
            submitRegisterButton.setText(R.string.register); // Change button text to "Register"
            confirmTextView.setVisibility(View.VISIBLE); // Show password confirmation label
            confirmEditText.setVisibility(View.VISIBLE); // Show password confirmation field
            myAccountTextView.setVisibility(View.GONE); // Hide the register link
        });

        // Set click listener for the submit/register button
        submitRegisterButton.setOnClickListener(v -> {
            String buttonText = submitRegisterButton.getText().toString();

            if (buttonText.equals("Register")) {
                // Handle user registration
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmEditText.getText().toString();

                // Validate that passwords match
                if (password.equals(confirmPassword)) {
                    // Create a new User object
                    User user = new User(0, userEditText.getText().toString(), password, null);

                    // Check for duplicate usernames
                    List<User> users = getUsers();
                    boolean hasUser = false;
                    for (User existingUser : users) {
                        if (Objects.equals(existingUser.getUser(), user.getUser())) {
                            hasUser = true;
                            break;
                        }
                    }

                    // If username is unique, create the user in the database
                    if (!hasUser) {
                        boolean isSuccessful = createUser(user);
                        if (isSuccessful) {
                            // Registration successful, reset UI to login mode
                            Toast.makeText(getApplication(), "Success", Toast.LENGTH_SHORT).show();
                            submitRegisterButton.setText(R.string.login);
                            userEditText.setText("");
                            passwordEditText.setText("");
                            confirmEditText.setText("");
                            confirmTextView.setVisibility(View.GONE);
                            confirmEditText.setVisibility(View.GONE);
                            myAccountTextView.setVisibility(View.VISIBLE);
                        } else {
                            // Registration failed
                            Toast.makeText(getApplication(), "Failure, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Username already exists
                        Toast.makeText(getApplication(), "Pick another Username.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Passwords do not match
                    Toast.makeText(getApplication(), "Supplied passwords not equal.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle user login
                User user = new User(0, userEditText.getText().toString(), passwordEditText.getText().toString(), null);

                // Retrieve user from database
                User dbUser = getDbUser(userEditText.getText().toString());
                // Check if user exists and credentials match (User.equals compares username and password hash)
                boolean isAuthenticated = user.equals(dbUser);
                if (isAuthenticated && dbUser != null) {
                    // Authentication successful, start MainActivity with user ID
                    int userId = dbUser.getId();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } else {
                    // Authentication failed
                    Toast.makeText(getApplication(), "Incorrect, please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Asynchronously retrieves a user from the database by username.
     *
     * @param username The username to search for
     * @return The User object if found, or null if not found or an error occurs
     */
    private User getDbUser(String username) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<User> task = () -> {
            InventoryRepo repo = new InventoryRepo(getApplication());
            repo.open();
            User user = repo.getUser(username); // Query the database for the user
            repo.close();
            return user;
        };
        Future<User> future = executor.submit(task);
        User user = null;
        try {
            user = future.get(); // Retrieve the result
        } catch (Exception e) {
            e.printStackTrace(); // Log any errors
        }
        executor.shutdown(); // Clean up executor
        return user;
    }

    /**
     * Asynchronously creates a new user in the database.
     *
     * @param user The User object to insert
     * @return True if the user was created successfully, false otherwise
     */
    private boolean createUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = () -> {
            InventoryRepo repo = new InventoryRepo(getApplication());
            repo.open();
            long result = repo.createUser(user); // Insert the user into the database
            repo.close();
            return result > 0; // Return true if insertion was successful
        };
        Future<Boolean> future = executor.submit(task);
        boolean isSuccess = false;
        try {
            isSuccess = future.get(); // Retrieve the result
        } catch (Exception e) {
            e.printStackTrace(); // Log any errors
        }
        executor.shutdown(); // Clean up executor
        return isSuccess;
    }

    /**
     * Asynchronously retrieves all users from the database.
     *
     * @return A list of User objects
     */
    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<User>> task = () -> {
            InventoryRepo repo = new InventoryRepo(getApplication());
            repo.open();
            List<User> dbUsers = repo.getUsers(); // Query the database for all users
            repo.close();
            return dbUsers;
        };
        Future<List<User>> future = executor.submit(task);
        try {
            users.addAll(future.get()); // Retrieve and add the results to the list
        } catch (Exception e) {
            e.printStackTrace(); // Log any errors
        }
        executor.shutdown(); // Clean up executor
        return users;
    }
}