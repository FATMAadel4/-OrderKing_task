package com.customermanager.util;

public class ApiConfig {

    // ── Change this if your backend runs on a different host/port ────────────
    public static final String BASE_URL = "http://localhost:8080";

    // ── Optional Bearer token (leave empty if no auth is configured) ─────────
    public static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3OTM5NDYwMywiZXhwIjoxNzc5NDgxMDAzfQ.mq2609vi03W0zSPkMLz5RuOC4iuanULhcrcjZdMTwxU";          // e.g. "my-secret-token"

    private ApiConfig() {}
}
