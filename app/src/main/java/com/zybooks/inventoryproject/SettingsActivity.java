package com.zybooks.inventoryproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Settings activity displays information for user to understand their options for enabling and receiving text messages.

public class SettingsActivity extends AppCompatActivity {
    public static final int REQUEST_SEND_SMS_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Boilerplate code for displaying view.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get reference to toolbar and set action.
        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.setting_toolbar);
        setSupportActionBar(tb);

        // Get reference to switch.
        androidx.appcompat.widget.SwitchCompat switchElement =  findViewById(R.id.enable_sms_switch);
        // Get reference to this for use in oncheckchanged function.
        SettingsActivity activityCurrent = this;

        // Check permission and set switch to true if good.
        if (ContextCompat.checkSelfPermission(activityCurrent, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            switchElement.setChecked(true);
        }

        // Setup listener to make request on SMS permission.
        switchElement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If user checks switch and sms permission is not granted offer them chance to change.
                if(isChecked){
                    // Check if the SEND_SMS permission has already been granted
                    if (ContextCompat.checkSelfPermission(activityCurrent, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                        // SMS not enabled, give user chance to enable.
                        ActivityCompat.requestPermissions(activityCurrent, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
                    }


                }
            }
        });
    }
    // Top tab bar methods.
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate menu and set to menu field.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    // Setup click event listener for top bar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // On click for top menu bar.
        if(item.getItemId() == R.id.go_main){
            // When clicked, return user to inventory page.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}