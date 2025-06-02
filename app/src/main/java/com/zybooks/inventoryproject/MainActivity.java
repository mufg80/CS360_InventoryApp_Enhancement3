/*
 * MainActivity.java
 * This class serves as the main interface for the inventory management application.
 * It displays a list of inventory items in a RecyclerView, allows adding, updating, and deleting
 * items, and supports toggling between local and remote database modes.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * MainActivity displays and manages the inventory list for a specific user.
 * It implements InventoryItemAdapter.OnButtonClickListener for handling item actions
 * and InventoryItem.QtyZeroListener for notifying when an item's quantity reaches zero.
 */
public class MainActivity extends AppCompatActivity implements InventoryItemAdapter.OnButtonClickListener, InventoryItem.QtyZeroListener {
    // Instance variables
    private Menu menu; // Reference to the toolbar menu
    private Context context; // Application context
    private int userId; // ID of the logged-in user
    private RecyclerView recyclerView; // RecyclerView for displaying inventory items
    private InventoryItemAdapter adapter; // Adapter for the RecyclerView
    private List<InventoryItem> items; // List of inventory items
    private MenuItem toggleItem; // Menu item for toggling database mode
    private boolean isRemote = false; // Flag for local (false) or remote (true) database mode

    /**
     * Initializes the activity, sets up the UI, and retrieves the user ID from the intent.
     *
     * @param savedInstanceState The saved instance state for activity recreation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {



       context = this;
        items = new ArrayList<>(); // Initialize the inventory items list

        // Set up the activity layout
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
       setContentView(R.layout.activity_main);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve user ID from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_ID")) {
            this.userId = intent.getIntExtra("USER_ID", -1);
        }
//
//        // Fetch initial inventory items and set up UI interactions
        getDatabaseItems();
        setupClicks();
    }

    /**
     * Sets up the RecyclerView, its adapter, and click listeners for the floating action button
     * and submit button.
     */
    private void setupClicks() {
        // Configure RecyclerView
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true); // Optimize for fixed-size items
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize and set the adapter
        adapter = new InventoryItemAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnButtonClickListener(this); // Set this activity as the button click listener

        // Add dividers between RecyclerView items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Set up floating action button to show item creation form
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> recyclerView.setVisibility(View.GONE)); // Hide RecyclerView to show form

        // Set up submit button for adding new items
        Button subButton = findViewById(R.id.submit_button);
        subButton.setOnClickListener(v -> {
            // Retrieve input fields
            EditText titleText = findViewById(R.id.title_edittext);
            EditText descText = findViewById(R.id.desc_edittext);
            EditText qtyText = findViewById(R.id.qty_edittext);

            // Validate quantity input, default to 1 if invalid
            int quantity = 1;
            try {
                quantity = Integer.parseInt(qtyText.getText().toString());
            } catch (NumberFormatException ignored) {
                // Default to 1 if parsing fails
            }

            // Create new inventory item
            InventoryItem item = new InventoryItem(0, titleText.getText().toString(), descText.getText().toString(), quantity, userId);
            boolean success = onCreateNewItem(item);

            // Show feedback to user
            Toast.makeText(getApplicationContext(), success ? "Success" : "There was a problem, please try again.", Toast.LENGTH_SHORT).show();

            // Refresh item list and reset UI
            getDatabaseItems();
            titleText.setText("");
            descText.setText("");
            qtyText.setText("");
            recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView again
        });

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Creates a new inventory item in the database, either locally or remotely.
     *
     * @param item The InventoryItem to create
     * @return True if the creation was successful, false otherwise
     */
    private boolean onCreateNewItem(InventoryItem item) {
        if (isRemote) {
            // Handle remote database creation
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Boolean> task = () -> {
                RemoteRepo repo = new RemoteRepo(context);
                try {
                    return repo.createInventoryItem(item) == 1; // Check if insertion was successful
                } catch (Exception e) {
                    return false;
                }
            };
            Future<Boolean> future = executor.submit(task);
            boolean isSuccess = false;
            try {
                isSuccess = future.get();
            } catch (Exception e) {
                // Suppress exceptions, return false
            }
            executor.shutdown();
            return isSuccess;
        } else {
            // Handle local database creation
            InventoryRepo repo = new InventoryRepo(getApplicationContext());
            repo.open();
            long result = repo.createInventoryItem(item);
            repo.close();
            return result > 0; // Return true if insertion was successful
        }
    }

    /**
     * Inflates the toolbar menu.
     *
     * @param menu The menu to inflate
     * @return True to display the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.app_menu, menu);
        toggleItem = menu.findItem(R.id.toggle_remote_local);
        updateToggleTitle(); // Set initial toggle title
        return true;
    }

    /**
     * Updates the title of the toggle menu item based on the database mode.
     */
    private void updateToggleTitle() {
        if (toggleItem != null) {
            toggleItem.setTitle(isRemote ? "Database is Remote" : "Database is Local");
        }
    }

    /**
     * Handles menu item selections, specifically the toggle between local and remote database modes.
     *
     * @param item The selected menu item
     * @return True if the item was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toggle_remote_local) {
            isRemote = !isRemote; // Toggle database mode
            item.setChecked(isRemote);
            updateToggleTitle();
            onToggleChanged(isRemote); // Refresh data based on new mode
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Refreshes the item list when the database mode changes.
     *
     * @param isRemote True for remote mode, false for local mode
     */
    private void onToggleChanged(boolean isRemote) {
        getDatabaseItems(); // Refresh the item list
    }

    /**
     * Updates an inventory item’s quantity (increment or decrement) in the database.
     *
     * @param toIncrement True to increment, false to decrement
     * @param position    The position of the item in the list
     */
    private void onUpdateItem(boolean toIncrement, int position) {
        InventoryItem item = items.get(position);
        InventoryItem copy = new InventoryItem(item.getId(), item.getTitle(), item.getDescription(), item.getQuantity(), item.getUserId());

        if (isRemote) {
            // Handle remote database update
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Boolean> task = () -> {
                RemoteRepo repo = new RemoteRepo(context);
                try {
                    if (toIncrement) {
                        copy.incrementQty();
                    } else {
                        copy.decrementQty();
                    }
                    return repo.updateInventoryItem(copy) == 1; // Check if update was successful
                } catch (Exception e) {
                    return false;
                }
            };
            Future<Boolean> future = executor.submit(task);
            boolean isSuccess = false;
            try {
                isSuccess = future.get();
            } catch (Exception e) {
                // Suppress exceptions
            }
            executor.shutdown();
            if (isSuccess) {
                if (toIncrement) {
                    item.incrementQty();
                } else {
                    item.decrementQty();
                }
                adapter.notifyItemChanged(position); // Update UI
            } else {
                Toast.makeText(getApplicationContext(), "There was a problem, please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle local database update
            InventoryRepo repo = new InventoryRepo(getApplicationContext());
            repo.open();
            if (toIncrement) {
                item.incrementQty();
            } else {
                item.decrementQty();
            }
            repo.updateInventoryItem(item);
            repo.close();
            adapter.notifyItemChanged(position); // Update UI
        }
    }

    /**
     * Handles decrement button clicks from the RecyclerView adapter.
     *
     * @param position The position of the item in the list
     */
    @Override
    public void onDecButtonClick(int position) {
        onUpdateItem(false, position);
    }

    /**
     * Handles increment button clicks from the RecyclerView adapter.
     *
     * @param position The position of the item in the list
     */
    @Override
    public void onIncButtonClick(int position) {
        onUpdateItem(true, position);
    }

    /**
     * Handles delete button clicks from the RecyclerView adapter.
     *
     * @param position The position of the item in the list
     */
    @Override
    public void onDelButtonClick(int position) {
        InventoryItem item = items.get(position);
        if (isRemote) {
            // Handle remote database deletion
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Boolean> task = () -> {
                RemoteRepo repo = new RemoteRepo(context);
                try {
                    return repo.deleteInventoryItem(item.getId()) == 1; // Check if deletion was successful
                } catch (Exception e) {
                    return false;
                }
            };
            Future<Boolean> future = executor.submit(task);
            boolean isSuccess = false;
            try {
                isSuccess = future.get();
            } catch (Exception e) {
                // Suppress exceptions
            }
            executor.shutdown();
            if (isSuccess) {
                items.remove(position);
                adapter.notifyItemRemoved(position); // Update UI
            } else {
                Toast.makeText(getApplicationContext(), "There was a problem, please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle local database deletion
            InventoryRepo repo = new InventoryRepo(getApplicationContext());
            repo.open();
            repo.deleteInventoryItem(item.getId());
            repo.close();
            items.remove(position);
            adapter.notifyItemRemoved(position); // Update UI
        }
    }

    /**
     * Retrieves inventory items from the database (local or remote) and updates the RecyclerView.
     */
    private void getDatabaseItems() {
        // Remove existing quantity zero listeners to prevent memory leaks
        for (InventoryItem item : items) {
            item.unsetQtyZeroListener();
        }

        if (isRemote) {
            // Handle remote database fetch
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<List<InventoryItem>> task = () -> {
                RemoteRepo repo = new RemoteRepo(context);
                try {
                    return repo.getInventoryItems(userId);
                } catch (Exception e) {
                    throw new IOException("Failed to get database items");
                }
            };
            Future<List<InventoryItem>> future = executor.submit(task);
            List<InventoryItem> dbItems = new ArrayList<>();
            try {
                dbItems = future.get();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Cannot connect to remote database.", Toast.LENGTH_SHORT).show();
            }
            items.clear();
            items.addAll(dbItems);
            setupClicks(); // Refresh RecyclerView
            executor.shutdown();
        } else {
            // Handle local database fetch
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<List<InventoryItem>> task = () -> {
                InventoryRepo repo = new InventoryRepo(getApplicationContext());
                repo.open();
                List<InventoryItem> dbItems = repo.getInventoryItems(userId);
                repo.close();
                return dbItems;
            };
            Future<List<InventoryItem>> future = executor.submit(task);
            try {
                items.clear();
                items.addAll(future.get());
                setupClicks(); // Refresh RecyclerView
            } catch (Exception e) {
                e.printStackTrace(); // Log errors
            }
            executor.shutdown();
        }

         //Attach quantity zero listeners to items
        for (InventoryItem item : items) {
            item.setQtyZeroListener(this);
        }
    }

    /**
     * Handles the event when an item's quantity reaches zero, displaying a notification.
     *
     * @param item The InventoryItem whose quantity reached zero
     */
    @Override
    public void onQuantityZero(InventoryItem item) {
        String formattedString = String.format("You are out of: %s", item.getTitle());
        Toast.makeText(getApplicationContext(), formattedString, Toast.LENGTH_SHORT).show();
    }
}