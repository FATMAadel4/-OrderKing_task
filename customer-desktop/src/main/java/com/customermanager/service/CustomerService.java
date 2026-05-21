package com.customermanager.service;

import com.customermanager.model.Customer;
import com.customermanager.util.ApiConfig;
import com.customermanager.util.ApiResponse;
import com.customermanager.util.PagedResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * All communication with the Spring Boot REST API lives here.
 * The UI layer never calls HttpClient directly.
 */
public class CustomerService {

    private static final String CUSTOMERS_URL = ApiConfig.BASE_URL + "/customers";

    private final HttpClient httpClient;
    private final Gson       gson;

    public CustomerService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder().create();
    }

    // ── GET /customers ────────────────────────────────────────────────────────
    public ApiResponse<List<Customer>> getAllCustomers() {
        HttpRequest request = buildGet(CUSTOMERS_URL);
        return executeList(request);
    }

    // ── GET /customers/{id} ───────────────────────────────────────────────────
    public ApiResponse<Customer> getCustomerById(int id) {
        HttpRequest request = buildGet(CUSTOMERS_URL + "/" + id);
        return execute(request, Customer.class);
    }

    // ── POST /customers ───────────────────────────────────────────────────────
    public ApiResponse<Customer> createCustomer(Customer customer) {
        String json    = gson.toJson(customer);
        HttpRequest request = buildPost(CUSTOMERS_URL, json);
        return execute(request, Customer.class);
    }

    // ── PUT /customers/{id} ───────────────────────────────────────────────────
    public ApiResponse<Customer> updateCustomer(int id, Customer customer) {
        String json    = gson.toJson(customer);
        HttpRequest request = buildPut(CUSTOMERS_URL + "/" + id, json);
        return execute(request, Customer.class);
    }

    // ── DELETE /customers/{id} ────────────────────────────────────────────────
    public ApiResponse<Void> deleteCustomer(int id) {
        HttpRequest request = buildDelete(CUSTOMERS_URL + "/" + id);
        return executeNoBody(request);
    }

    // ── Search /customers?search=... ──────────────────────────────────────────
    public ApiResponse<List<Customer>> searchCustomers(String query) {
        String url = CUSTOMERS_URL + "?search=" + encodeParam(query);
        HttpRequest request = buildGet(url);
        return executeList(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private HttpRequest buildGet(String url) {
        return baseBuilder(url).GET().build();
    }

    private HttpRequest buildPost(String url, String json) {
        return baseBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private HttpRequest buildPut(String url, String json) {
        return baseBuilder(url)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private HttpRequest buildDelete(String url) {
        return baseBuilder(url).DELETE().build();
    }

    private HttpRequest.Builder baseBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (!ApiConfig.BEARER_TOKEN.isEmpty()) {
            builder.header("Authorization", "Bearer " + ApiConfig.BEARER_TOKEN);
        }
        return builder;
    }

    /** Execute a request that returns EITHER a JSON array OR a Page object. */
    private ApiResponse<List<Customer>> executeList(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                JsonElement element = JsonParser.parseString(body);

                if (element.isJsonArray()) {
                    // Plain list: [ {...}, {...} ]
                    Type listType = new TypeToken<List<Customer>>() {}.getType();
                    List<Customer> list = gson.fromJson(element, listType);
                    return ApiResponse.success(list, status);
                } else if (element.isJsonObject()) {
                    // Paginated: { "content": [...], "totalElements": N, ... }
                    PagedResponse paged = gson.fromJson(element, PagedResponse.class);
                    return ApiResponse.success(paged.getContent(), status);
                } else {
                    return ApiResponse.failure("Unexpected response format from server.", status);
                }
            } else {
                return ApiResponse.failure(extractErrorMessage(body, status), status);
            }
        } catch (IOException e) {
            return ApiResponse.failure(
                    "Cannot reach the server. Please check that the backend is running.\n(" + e.getMessage() + ")", 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.failure("Request was interrupted.", 0);
        }
    }

    /** Execute a request and deserialize the JSON body into type T. */
    private <T> ApiResponse<T> execute(HttpRequest request, Type responseType) {
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                T data = gson.fromJson(body, responseType);
                return ApiResponse.success(data, status);
            } else {
                String msg = extractErrorMessage(body, status);
                return ApiResponse.failure(msg, status);
            }
        } catch (IOException e) {
            return ApiResponse.failure(
                    "Cannot reach the server. Please check that the backend is running.\n(" + e.getMessage() + ")", 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.failure("Request was interrupted.", 0);
        }
    }

    /** Execute a request that returns no body (e.g. DELETE). */
    private ApiResponse<Void> executeNoBody(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return ApiResponse.success(null, status);
            } else {
                return ApiResponse.failure(extractErrorMessage(response.body(), status), status);
            }
        } catch (IOException e) {
            return ApiResponse.failure(
                    "Cannot reach the server. Please check that the backend is running.\n(" + e.getMessage() + ")", 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.failure("Request was interrupted.", 0);
        }
    }

    /** Try to pull a human-readable message from a JSON error body. */
    private String extractErrorMessage(String body, int status) {
        if (body == null || body.isBlank()) {
            return "Server returned HTTP " + status;
        }
        try {
            // Spring Boot error bodies usually have a "message" field
            var obj = gson.fromJson(body, java.util.Map.class);
            if (obj != null && obj.containsKey("message")) {
                return (String) obj.get("message");
            }
        } catch (Exception ignored) {}
        return "Server error (" + status + "): " + body;
    }

    private String encodeParam(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
