package com.example.applicationproject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class Task {
    final int id;
    final String title;
    final String description;
    final int categoryId;
    final String priority;
    final String dueDate;
    final String dueTime;
    final String status;

    Task(
            int id,
            String title,
            String description,
            int categoryId,
            String priority,
            String dueDate,
            String dueTime,
            String status
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.priority = priority;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.status = status;
    }

    static Task fromJson(JSONObject json) throws JSONException {
        int categoryId = json.isNull("category_id") ? 0 : json.getInt("category_id");
        return new Task(
                json.getInt("id"),
                json.optString("title", ""),
                json.optString("description", ""),
                categoryId,
                json.optString("priority", "Medium"),
                json.optString("due_date", ""),
                json.optString("due_time", ""),
                json.optString("status", "Pending")
        );
    }

    static String toDatabaseDate(String input) {
        Date date = parseDate(input, "M/d/yyyy", "MM/dd/yyyy", "yyyy-MM-dd");
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    static String toFormDate(String databaseDate) {
        if (databaseDate == null || databaseDate.isEmpty()) {
            return "";
        }
        try {
            Date date = parseDate(databaseDate, "yyyy-MM-dd");
            return new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        } catch (IllegalArgumentException error) {
            return databaseDate;
        }
    }

    static String toDisplayDate(String databaseDate) {
        if (databaseDate == null || databaseDate.isEmpty()) {
            return "";
        }
        try {
            Date date = parseDate(databaseDate, "yyyy-MM-dd");
            return new SimpleDateFormat("MM/dd/yy", Locale.US).format(date);
        } catch (IllegalArgumentException error) {
            return databaseDate;
        }
    }

    private static Date parseDate(String input, String... patterns) {
        for (String pattern : patterns) {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
            formatter.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            Date date = formatter.parse(input, position);
            if (date != null && position.getIndex() == input.length()) {
                return date;
            }
        }
        throw new IllegalArgumentException("Enter a valid date as MM/DD/YYYY.");
    }
}
