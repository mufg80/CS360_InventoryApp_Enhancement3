package com.zybooks.inventoryproject;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.pm.PackageManager;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Main activity, implementing interface in adapter class for use with button on Clicks.
public class MainActivity extends AppCompatActivity implements InventoryItemAdapter.OnButtonClickListener, InventoryItem.QtyZeroListener {
    // Private fields holding various information.
    private Menu menu;
    private RecyclerView recyclerView;
    private InventoryItemAdapter adapter;
    private List<InventoryItem> items;


    // oncreate method sets up view.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instantiate list to hold inventory items.
        items = new ArrayList<InventoryItem>();

        // Boilerplate code for launching view.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up recyclerview properties.
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Now that recyclerview is setup, call method to load it with items.
        GetDatabaseitems();

        // Set up adapter using subclass.
        adapter = new InventoryItemAdapter(items);
        recyclerView.setAdapter(adapter);

        // Tie the interface used in adapter class to this main activity.
        adapter.setOnButtonClickListener(this);

        // Create decorator for lines between each row.
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);



        // Add onclick for floating button.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            // On click, simply erase recycler so fields for adding item come into view.
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
            }
        });

        // Get submit button for adding item.
        Button subButton = findViewById(R.id.submit_button);

        // Set onclick for this button to add item to database.
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get references to needed edit texts.
                EditText titleText = findViewById(R.id.title_edittext);
                EditText descText = findViewById(R.id.desc_edittext);
                EditText qtyText = findViewById(R.id.qty_edittext);

                // Check for bad number, although should not be allowed by view.
                int i = 0;
                try {
                    i = Integer.parseInt(qtyText.getText().toString());
                }catch (NumberFormatException ignored){
                    i = 1;
                }

                // Create item with constructor.
                InventoryItem item = new InventoryItem(0, titleText.getText().toString(), descText.getText().toString(), Integer.toString(i));

                // Create repo item for database operation.
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                long ret = repo.createInventoryItem(item);
                if(ret > 0){
                    // Let user know of success.
                    Toast.makeText(getApplication(), "Success", Toast.LENGTH_SHORT).show();
                    // Since success, erase text from views for users convenience.
                    titleText.setText("");
                    descText.setText("");
                    qtyText.setText("");
                    // Call get items again to make sure this new item is included in list.
                    GetDatabaseitems();
                }else{
                    // Some problem occurred, let user know.
                    Toast.makeText(getApplication(), "There was a problem, please try again.", Toast.LENGTH_SHORT).show();
                }
                // Close Database.
                repo.close();
                // Open recycler for listview again.
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        // Set tool bar for getting to sms screen.
        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

    }

    // Function to get database items for recycler view.
    private void GetDatabaseitems() {
        // Unhook listeners everytime as good practice on memory leaks.
        for(InventoryItem item: items){
            item.unsetQtyZeroListener();
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<InventoryItem>> task = (new Callable<List<InventoryItem>>(){
            @Override
            public List<InventoryItem> call() throws Exception {
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                // Get list from database.
                List<InventoryItem> dbitems = repo.getInventoryItems();
                // Close database.
                repo.close();
                return dbitems;
            }
        });
        Future<List<InventoryItem>> fut = executor.submit(task);

        try{
            // Clear list then refill with new query results.
            items.clear();
            items.addAll(fut.get());
        }catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdown();


        // Reapply listeners for 0 qty event.
        for(InventoryItem item: items){
            item.setQtyZeroListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Create top bar menu.
        getMenuInflater().inflate(R.menu.app_menu, menu);
        menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Menu onclick for going to settings intent.
        if(item.getItemId() == R.id.go_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to send sms when inventory item reaches 0. Only called from
    // decrement method so that it doesn't spam messages when read from database
    // and item is already at 0.
    private void sendSMS(String message) {
        String phone = "8675309"; // Remember the song!

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){

                try{
                    SmsManager manager = SmsManager.getDefault();
                    manager.sendTextMessage(phone,null, message, null, null);
                    Toast.makeText(this, "SMS Sent: " + phone + " With message: " + message, Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    Toast.makeText(this, "SMS Failed to Send", Toast.LENGTH_SHORT).show();
                }
            }

    }


    // Since main activity implements interface from adapter class these are overridden functions.
    @Override
    public void onDecButtonClick(int position) {
        // Decrement item, get item by position in list.
        InventoryItem inventoryItem = items.get(position);
        // Use object method to safely decrement qty.
        inventoryItem.decrementQty();
        // Notify adapter or change won't be displayed.
        adapter.notifyItemChanged(position);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // Create repo object for database operations.
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();


                // Use repo update method to update row data.
                repo.updateInventoryItem(inventoryItem);
                // Close database.
                repo.close();
            }
        });
        executorService.shutdown();

    }

    @Override
    public void onIncButtonClick(int position) {
        // Increment item, get item by position in list.
        InventoryItem inventoryItem = items.get(position);
        // Use object method to safely decrement qty.
        inventoryItem.incrementQty();
        // Notify adapter or change won't be displayed.
        adapter.notifyItemChanged(position);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // Create repo object for database operations.
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();

                repo.updateInventoryItem(inventoryItem);
                // Close database.
                repo.close();
            }
        });
        executorService.shutdown();

    }

    @Override
    public void onDelButtonClick(int position) {
        // Delete item, get item by position in list.
        InventoryItem inventoryItem = items.get(position);

        items.remove(position);
        // Notify adapter or change won't be displayed.
        adapter.notifyItemRemoved(position);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // Create repo object for database operations.
                InventoryRepo repo = new InventoryRepo(getApplication());
                repo.open();
                // Delete item by items row ID.
                repo.deleteInventoryItem(inventoryItem.getId());
                // Close database.
                repo.close();
            }
        });
        executorService.shutdown();

    }

    // Event listener, get string of item and pass to send sms method.
    @Override
    public void onQuantityZero(InventoryItem item) {
        String s = item.toString();
        sendSMS(s);
    }
}