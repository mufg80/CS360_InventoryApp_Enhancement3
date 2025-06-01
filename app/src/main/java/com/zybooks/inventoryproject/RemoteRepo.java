package com.zybooks.inventoryproject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteRepo {
    private static final String BASE_URL = "https://10.0.0.2:7113/api/Inventory/"; // Update with your API URL
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public int createInventoryItem(InventoryItem item) throws IOException {
        String json = gson.toJson(item);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return Integer.parseInt(response.body().string()); // API returns new ID
            }
            return 0; // Failure
        } catch (NumberFormatException e) {
            return 0; // Invalid ID format
        }
    }

    public int updateInventoryItem(InventoryItem inventoryItem) throws IOException {
        String json = gson.toJson(inventoryItem);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + inventoryItem.getId())
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? 1 : 0;
        }
    }

    public int deleteInventoryItem(int id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + id)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? 1 : 0;
        }
    }

    public List<InventoryItem> getInventoryItems(int userId) throws IOException {
        List<InventoryItem> items = new ArrayList<>();
        Request request = new Request.Builder()
                .url(BASE_URL + "?userId=" + userId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                items = gson.fromJson(json, new TypeToken<List<InventoryItem>>(){}.getType());
            }
        }
        return items;
    }
}