package org.finalproject.loginregisterfx.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private static final String BASE_URL = "http://127.0.0.1:5050/api"; // Change to your actual API URL
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    private static String authToken;

    public static CompletableFuture<JsonObject> login(String email, String password, String role) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        String endpoint = String.format("/auth/%s/login", role.toLowerCase());

        return makePostRequest(endpoint, credentials);
    }

    public static CompletableFuture<JsonObject> register(Map<String, Object> userData, String role) {
        String endpoint = String.format("/auth/%s/signup", role.toLowerCase());

        return makePostRequest(endpoint, userData);
    }

    public static CompletableFuture<JsonObject> getUserProfile() {
        return makeGetRequest("/auth/me");
    }

    public static CompletableFuture<JsonObject> logout() {
        return makePostRequest("/auth/logout", null);
    }

    public static CompletableFuture<JsonObject> makePostRequest(String endpoint, Object requestBody) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        try {
            String jsonBody = requestBody != null ? gson.toJson(requestBody) : "";
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(body);

            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                        // Save token if it exists in the response
                        if (jsonResponse.has("token")) {
                            authToken = jsonResponse.get("token").getAsString();
                        }

                        future.complete(jsonResponse);
                    } else {
                        future.completeExceptionally(new IOException("Request failed: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public static CompletableFuture<JsonObject> makeGetRequest(String endpoint) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .get();

            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        future.complete(jsonResponse);
                    } else {
                        future.completeExceptionally(new IOException("Request failed: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // Add PUT request support
    public static CompletableFuture<JsonObject> makePutRequest(String endpoint, Object requestBody) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        try {
            String jsonBody = requestBody != null ? gson.toJson(requestBody) : "";
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .put(body);

            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        future.complete(jsonResponse);
                    } else {
                        future.completeExceptionally(new IOException("Request failed: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    // Add DELETE request support
    public static CompletableFuture<JsonObject> makeDeleteRequest(String endpoint) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .delete();

            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        future.complete(jsonResponse);
                    } else {
                        future.completeExceptionally(new IOException("Request failed: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}