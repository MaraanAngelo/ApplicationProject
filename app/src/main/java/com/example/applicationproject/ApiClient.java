package com.example.applicationproject;

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
    // Android emulator address for the host computer running XAMPP.
    private static final String BASE_URL = "http://192.168.18.5/ApplicationProject/";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    interface Callback {
        void onSuccess(String response);
        void onError(Exception error);
    }

    private ApiClient() {
    }

    static void get(String endpoint, Callback callback) {
        request(endpoint, null, callback);
    }

    static void post(String endpoint, Map<String, String> parameters, Callback callback) {
        request(endpoint, parameters, callback);
    }

    private static void request(String endpoint, Map<String, String> parameters, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(BASE_URL + endpoint).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
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
                postSuccess(callback, response);
            } catch (IOException error) {
                postError(callback, error);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static String encodeParameters(Map<String, String> parameters) throws IOException {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
            body.append('=');
            body.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
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
