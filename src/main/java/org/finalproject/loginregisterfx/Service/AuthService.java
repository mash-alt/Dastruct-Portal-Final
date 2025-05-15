package org.finalproject.loginregisterfx.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class AuthService {    //private static final String BASE_URL = "https://sturdy-space-pancake-v6wj54rgr57726xjr-5050.app.github.dev/api"; 
    // Change to your actual API URL
    private static final String BASE_URL = "http://localhost:5050/api";  // Removed /api suffix since endpoints already include it
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static String authToken;
    
    /**
     * Set the authentication token for API requests
     * 
     * @param token The JWT token to use for authenticated requests
     */
    public static void setAuthToken(String token) {
        authToken = token;
        System.out.println("Auth token set for API requests");
    }
    
    /**
     * Get the current authentication token
     * 
     * @return The current JWT token
     */    public static String getAuthToken() {
        return authToken;
    }
    
    public static CompletableFuture<JsonObject> login(String email, String password, String role) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        String endpoint = String.format("/auth/%s/login", role.toLowerCase());

        return makePostRequest(endpoint, credentials);
    }
    
    public static CompletableFuture<JsonObject> register(Map<String, Object> userData, String role) {
        String endpoint = String.format("/auth/%s/signup", role.toLowerCase());

        return makePostRequest(endpoint, userData);    }
    
    public static CompletableFuture<JsonObject> getUserProfile() {
        return makeGetRequest("/auth/me");
    }
      /**
     * Get a specific student's profile by ID
     * 
     * @param studentId The ID of the student
     * @return CompletableFuture with API response containing student profile
     */    public static CompletableFuture<JsonObject> getStudentProfile(String studentId) {
        System.out.println("Fetching profile data for student ID: " + studentId);
        // Use the standard format expected by the backend
        String url = "/student/" + studentId;
        System.out.println("Full URL: " + BASE_URL + url);
        return makeGetRequest(url);
    }
    
    /**
     * Get a student's study load (list of enrolled subjects)
     * 
     * @param studentId The ID of the student (in the format ucb-XXXXX)
     * @return CompletableFuture with API response containing the student's study load
     */    public static CompletableFuture<JsonObject> getStudentStudyLoad(String studentId) {        // This matches the backend route: '/student/:studentId/studyload'
        System.out.println("Fetching study load for student ID: " + studentId);
        String url = "/student/" + studentId + "/studyload";
        System.out.println("Full URL: " + BASE_URL + url);
        return makeGetRequest(url);
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
                    System.err.println("API request failed: " + endpoint + " - Error: " + e.getMessage());
                    future.completeExceptionally(e);
                }                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    System.out.println("API response received for: " + endpoint);
                    System.out.println("Status code: " + response.code());
                    System.out.println("Response body: " + responseBody);
                    System.out.println("Request URL: " + call.request().url());
                    System.out.println("Authorization header: " + (call.request().header("Authorization") != null ? "present" : "missing"));
                    
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                        // Save token if it exists in the response
                        if (jsonResponse.has("token")) {
                            authToken = jsonResponse.get("token").getAsString();
                            System.out.println("Auth token updated from response");
                        }

                        future.complete(jsonResponse);
                    } else {
                        // Try to parse error response as JSON first
                        try {
                            JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
                            // If the response has an error field, use it as the message
                            if (errorResponse.has("error")) {
                                future.completeExceptionally(new IOException("Request failed: " + errorResponse.get("error").getAsString()));
                            } else {
                                // Otherwise use the whole response body
                                future.completeExceptionally(new IOException("Request failed: " + responseBody));
                            }
                        } catch (Exception e) {
                            // If we can't parse as JSON, use the raw response
                            future.completeExceptionally(new IOException("Request failed: " + responseBody));
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up API request: " + e.getMessage());
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
                    System.err.println("GET request failed: " + endpoint + " - Error: " + e.getMessage());
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    System.out.println("GET response for: " + endpoint);
                    System.out.println("Status code: " + response.code());
                    System.out.println("Response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        future.complete(jsonResponse);
                    } else {
                        // Try to parse error response as JSON first
                        try {
                            JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
                            // If the response has an error field, use it as the message
                            if (errorResponse.has("error")) {
                                future.completeExceptionally(new IOException("Request failed: " + errorResponse.get("error").getAsString()));
                            } else {
                                // Otherwise use the whole response body
                                future.completeExceptionally(new IOException("Request failed: " + responseBody));
                            }
                        } catch (Exception e) {
                            // If we can't parse as JSON, use the raw response
                            future.completeExceptionally(new IOException("Request failed: " + responseBody));
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up GET request: " + e.getMessage());
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
    }    /**
     * Get admin dashboard data
     * This accesses the new admin dashboard endpoint you created
     * 
     * @return CompletableFuture with API response containing dashboard data
     */
    public static CompletableFuture<JsonObject> getAdminDashboard() {
        System.out.println("Accessing admin dashboard");
        return makeGetRequest("/admin/dashboard");
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