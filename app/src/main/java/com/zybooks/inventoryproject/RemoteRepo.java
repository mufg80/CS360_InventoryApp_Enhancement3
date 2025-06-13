/*
 * RemoteRepo.java
 * This class provides methods to interact with a remote API for managing inventory items.
 * It handles CRUD operations (create, read, update, delete) using HTTP requests with
 * encrypted API key authentication.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * RemoteRepo handles communication with a remote API for inventory management.
 * It uses OkHttp for HTTP requests and Gson for JSON serialization/deserialization.
 * API requests are authenticated using an AES-encrypted API key.
 */
public class RemoteRepo {
    // Constants for API and encryption
    private static final String BASE_URL = "https://10.0.2.2:7113/api/Inventory/"; // Base URL for the API
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Media type for JSON requests
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // Encryption algorithm
    private static String encryptedString = ""; // Cached encrypted API key

    // Instance variables
    private final OkHttpClient client = new OkHttpClient(); // HTTP client for API requests
    private final Gson gson = new Gson(); // Gson instance for JSON processing

    /**
     * Encrypts the API key using AES encryption with a key and initialization vector
     * retrieved from a non-version-controlled file. Caches the result to avoid repeated encryption.
     *
     * @throws InvalidAlgorithmParameterException If the provided IV is invalid for AES encryption.
     * @throws InvalidKeyException If the encryption key is invalid or incorrectly formatted.
     * @throws NoSuchPaddingException If the requested padding scheme is not available.
     * @throws NoSuchAlgorithmException If the AES encryption algorithm is not available.
     * @throws UnsupportedEncodingException If the character encoding for key or API key is unsupported.
     * @throws IllegalBlockSizeException If the API key size is incorrect for AES encryption.
     * @throws BadPaddingException If an error occurs during encryption due to incorrect padding.
     *
     * @return The Base64-encoded encrypted API key.
     */

    private String getEncryption() throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        // Return cached encrypted key if available
        if (!encryptedString.isEmpty()) {
            return encryptedString;
        }
        String key = BuildConfig.KEY;
        String iv = BuildConfig.IV;
        String api_key = BuildConfig.API_KEY;

        // Set up AES encryption
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // Encrypt the API key and encode to Base64
        byte[] encrypted = cipher.doFinal(api_key.getBytes("UTF-8"));
        encryptedString = Base64.encodeToString(encrypted, Base64.NO_WRAP);

        return encryptedString;
    }

    /**
     * Creates a new inventory item via the remote API.
     *
     * @param item The InventoryItem to create
     * @return The ID of the created item, or 0 if the request fails
     * @throws IOException If a network error occurs
     */
    public int createInventoryItem(InventoryItem item) throws IOException{
        // Get encrypted API key
        String apiKey = "";

        try{
            apiKey = getEncryption();
        }catch(Exception e){
            Log.e("Encryption", "Failed on CreateInventoryItem");
            return 0;
        }

        // Serialize item to JSON
        String json = gson.toJson(item);
        RequestBody body = RequestBody.create(json, JSON);

        // Build and execute POST request
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("X-encrypted-api-key", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return Integer.parseInt(response.body().string()); // Parse returned ID
            }
        }
        return 0; // Return 0 on failure
    }

    /**
     * Updates an existing inventory item via the remote API.
     *
     * @param inventoryItem The InventoryItem to update
     * @return 1 if the update was successful, 0 otherwise
     * @throws IOException If a network error occurs
     */
    public int updateInventoryItem(InventoryItem inventoryItem) throws IOException{
        // Get encrypted API key
        String apiKey = "";

        try{
            apiKey = getEncryption();
        }catch(Exception e){
            Log.e("Encryption", "Failed on updateInventoryItem");
            return 0;
        }

        // Serialize item to JSON
        String json = gson.toJson(inventoryItem);
        RequestBody body = RequestBody.create(json, JSON);

        // Build and execute PUT request
        Request request = new Request.Builder()
                .url(BASE_URL + inventoryItem.getId())
                .addHeader("X-encrypted-api-key", apiKey)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? 1 : 0; // Return 1 on success, 0 on failure
        }
    }

    /**
     * Deletes an inventory item via the remote API.
     *
     * @param id The ID of the item to delete
     * @return 1 if the deletion was successful, 0 otherwise
     * @throws IOException If a network error occurs
     */
    public int deleteInventoryItem(int id) throws IOException {
        // Get encrypted API key
        String apiKey = "";

        try{
            apiKey = getEncryption();
        }catch(Exception e){
            Log.e("Encryption", "Failed on deleteInventoryItem");
            return 0;
        }

        // Build and execute DELETE request
        Request request = new Request.Builder()
                .url(BASE_URL + id)
                .addHeader("X-encrypted-api-key", apiKey)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? 1 : 0; // Return 1 on success, 0 on failure
        }
    }

    /**
     * Retrieves a list of inventory items for a specific user from the remote API.
     *
     * @param userId The ID of the user whose items are to be retrieved
     * @return A list of InventoryItem objects
     * @throws IOException If a network error occurs
     */
    public List<InventoryItem> getInventoryItems(int userId) throws IOException{
        // Get encrypted API key
        String apiKey = "";

        try{
            apiKey = getEncryption();
        }catch(Exception e){
            Log.e("Encryption", "Failed on getInventoryItems");
            return new ArrayList<InventoryItem>();
        }

        List<InventoryItem> items = new ArrayList<>();

        // Build and execute GET request
        Request request = new Request.Builder()
                .url(BASE_URL + "?userId=" + userId)
                .addHeader("X-encrypted-api-key", apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Deserialize JSON response to list of InventoryItem objects
                String json = response.body().string();
                items = gson.fromJson(json, new TypeToken<List<InventoryItem>>() {}.getType());
            }
        }
        return items;
    }
}