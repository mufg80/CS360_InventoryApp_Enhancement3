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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Boilerplate code to set up view.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get references to various views.
        EditText userEditText = findViewById(R.id.user_name_edittext);
        TextView confirmTextView = findViewById(R.id.password_confirm_textview);
        EditText confirmEditText = findViewById(R.id.password_confirm_edittext);
        EditText passwordEditText = findViewById(R.id.password_edittext);
        Button submit_register_button = findViewById(R.id.button);
        TextView myaccountTextview = findViewById(R.id.account_textview);

        // Add onclick listener for the register new account hypertext.
        myaccountTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This click simply shows fields for registering user and changes text.
                submit_register_button.setText(R.string.register);
                confirmTextView.setVisibility(View.VISIBLE);
                confirmEditText.setVisibility(View.VISIBLE);
                myaccountTextview.setVisibility(View.GONE);
            }
        });

        // Add onclick listener for submit/register button.
        submit_register_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Get text of button to decide which logic to run.
                String text = submit_register_button.getText().toString();

                // If equal to register, this button should create a new user in database.
                if(submit_register_button.getText().toString().equals("Register")){

                    // Check if passwords supplied match each other, if not let user know and abort.
                    if(passwordEditText.getText().toString().equals(confirmEditText.getText().toString())){

                        // Passwords match, begin creating information. Create user object.
                        User u = new User(0, userEditText.getText().toString(), passwordEditText.getText().toString(),null);

                        // Get list of users, so that duplicate usernames are not created.
                        List<User> users = getUsers();
                        boolean hasUser = false;
                        // Check if identical user name exists in database.
                        for(User s: users){
                            if (Objects.equals(s.getUser(), u.getUser())) {
                                hasUser = true;
                                break;
                            }
                        }

                        // No user exists, Add to database, let user know via text set fields appropriately.
                        if(!hasUser){
                            boolean isSuccessful = createUser(u);
                            if(isSuccessful){
                                Toast.makeText(getApplication(), "Success", Toast.LENGTH_SHORT).show();
                                submit_register_button.setText(R.string.login);
                                userEditText.setText("");
                                passwordEditText.setText("");
                                confirmEditText.setText("");
                                confirmTextView.setVisibility(View.GONE);
                                confirmEditText.setVisibility(View.GONE);
                                myaccountTextview.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(getApplication(), "Failure, please try again.", Toast.LENGTH_SHORT).show();
                            }


                        }else{
                            // User exists, let user know.
                            Toast.makeText(getApplication(), "Pick another Username.", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        // Passwords not equal to one another, let user know.
                        Toast.makeText(getApplication(), "Supplied passwords not equal.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    // Button text is not equal to register, lets try to log user in.
                    // Create user object using information supplied. (user class constructor hashes password.
                    User u = new User(0, userEditText.getText().toString(), passwordEditText.getText().toString(), null);


                    // Check for username in database. If nothing found will return null.
                    User dbuser = getDbUser(userEditText.getText().toString());
                    // Overridden equals for user class checks username and hash. If b is true, user has been authenticated.
                    boolean b = u.equals(dbuser);
                    if(b){
                        // User good, send them to main activity.
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        // User/password combo was not correct, let user know.
                        Toast.makeText(getApplication(), "Incorrect, please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    // Function to get db user asynchronously.
    private User getDbUser(String s) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<User> task = (new Callable<User>(){
            @Override
            public User call() throws Exception {
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                // Get list from database.
                User user = repo.getUser(s);
                // Close database.
                repo.close();
                return user;
            }
        });
        Future<User> fut = executor.submit(task);
        User u = null;
        // Get result.
        try{
            u = fut.get();
        }catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdown();
        return u;
    }

    // Function to create db user asynchronously.
    private boolean createUser(User u) {
        boolean isSuccess = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = (new Callable<Boolean>(){
            @Override
            public Boolean call() throws Exception {
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                // Get list from database.
                long l = repo.createUser(u);
                // Close database.
                repo.close();
                return l > 0;
            }
        });
        Future<Boolean> fut = executor.submit(task);

        // Get result.
        try{
            isSuccess = fut.get();
        }catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdown();
        return isSuccess;

    }

    // Function to get list of db users asynchronously.
    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<User>> task = (new Callable<List<User>>(){
            @Override
            public List<User> call() throws Exception {
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                // Get list from database.
                List<User> dbusers = repo.getUsers();
                // Close database.
                repo.close();
                return dbusers;
            }
        });
        Future<List<User>> fut = executor.submit(task);

        // Get results
        try{
            users.addAll(fut.get());
        }catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdown();
        return users;
    }
}