package com.example.applicationproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ApiClient {
    public static final String LAN_URL = "http://192.168.18.5/ApplicationProject/";
    public static final String EMULATOR_URL = "http://10.0.2.2/ApplicationProject/";
    private static final String PREF_NAME = "api_client_prefs";
    private static final String KEY_BASE_URL = "custom_base_url";

    private static String customBaseUrl = null;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    interface Callback {
        void onSuccess(String response);
        void onError(Exception error);
    }

    private ApiClient() {
    }

    static void init(Context context) {
        if (customBaseUrl == null && context != null) {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String saved = prefs.getString(KEY_BASE_URL, null);
            if (saved != null && !saved.trim().isEmpty()) {
                customBaseUrl = formatBaseUrl(saved);
            }
        }
    }

    static String getActiveBaseUrl(Context context) {
        init(context);
        if (customBaseUrl != null && !customBaseUrl.isEmpty()) {
            return customBaseUrl;
        }
        return isEmulator() ? EMULATOR_URL : LAN_URL;
    }

    static void setCustomBaseUrl(Context context, String url) {
        String formatted = formatBaseUrl(url);
        customBaseUrl = formatted;
        if (context != null) {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_BASE_URL, formatted).apply();
        }
    }

    static String formatBaseUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return LAN_URL;
        }
        String trimmed = input.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://" + trimmed;
        }
        if (!trimmed.endsWith("/")) {
            trimmed = trimmed + "/";
        }
        if (!trimmed.contains("/ApplicationProject/")) {
            if (trimmed.endsWith("/")) {
                trimmed = trimmed + "ApplicationProject/";
            } else {
                trimmed = trimmed + "/ApplicationProject/";
            }
        }
        return trimmed;
    }

    static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    static void get(String endpoint, Callback callback) {
        request(endpoint, null, callback);
    }

    static void post(String endpoint, Map<String, String> parameters, Callback callback) {
        request(endpoint, parameters, callback);
    }

    static void testConnection(String testBaseUrl, Callback callback) {
        EXECUTOR.execute(() -> {
            try {
                String formatted = formatBaseUrl(testBaseUrl);
                String response = executeHttpCall(formatted + "get_categories.php", null, 4000);
                postSuccess(callback, response);
            } catch (IOException error) {
                postError(callback, error);
            }
        });
    }

    private static void request(String endpoint, Map<String, String> parameters, Callback callback) {
        EXECUTOR.execute(() -> {
            String primaryUrl = customBaseUrl != null ? customBaseUrl : (isEmulator() ? EMULATOR_URL : LAN_URL);
            String fallbackUrl = primaryUrl.equalsIgnoreCase(LAN_URL) ? EMULATOR_URL : LAN_URL;

            try {
                String response = executeHttpCall(primaryUrl + endpoint, parameters, 6000);
                postSuccess(callback, response);
            } catch (IOException primaryError) {
                if (customBaseUrl == null && !primaryUrl.equalsIgnoreCase(fallbackUrl)) {
                    try {
                        String response = executeHttpCall(fallbackUrl + endpoint, parameters, 6000);
                        postSuccess(callback, response);
                        return;
                    } catch (IOException fallbackError) {
                        postError(callback, new IOException(
                                "Failed to connect to " + primaryUrl + " and " + fallbackUrl + ".\nMake sure XAMPP Apache is running and unblocked in Windows Firewall."
                        ));
                        return;
                    }
                }
                postError(callback, new IOException(
                        "Cannot connect to " + primaryUrl + " (" + primaryError.getMessage() + "). Check server IP & Windows Firewall."
                ));
            }
        });
    }

    private static String executeHttpCall(String fullUrl, Map<String, String> parameters, int timeoutMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
        try {
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setRequestProperty("Accept", "application/json");

            if (parameters != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );
                try (OutputStream output = connection.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(
                             new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                    writer.write(encodeParameters(parameters));
                    writer.flush();
                }
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String response = readResponse(stream);
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("HTTP " + statusCode + ": " + response);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static String encodeParameters(Map<String, String> parameters) throws IOException {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8.name()));
            body.append('=');
            String value = parameter.getValue() == null ? "" : parameter.getValue();
            body.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        }
        return body.toString();
    }

    private static String readResponse(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static void postSuccess(Callback callback, String response) {
        MAIN_HANDLER.post(() -> callback.onSuccess(response));
    }

    private static void postError(Callback callback, Exception error) {
        MAIN_HANDLER.post(() -> callback.onError(error));
    }
}
