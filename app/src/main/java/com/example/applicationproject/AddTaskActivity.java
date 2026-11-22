package com.example.applicationproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    private final ArrayList<Category> categories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;
    private Spinner spinnerCategory;
    private Spinner spinnerPriority;
    private EditText etDueDate;
    private EditText etDueTime;
    private Button btnSaveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerCategory = findViewById(R.id.spinnerCategory);
        categories.add(new Category(0, getString(R.string.no_category)));
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        loadCategories();

        spinnerPriority = findViewById(R.id.spinnerPriority);
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Low", "Medium", "High"}
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1);

        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);

        setupDateAndTimePickers();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> finish());

        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void setupDateAndTimePickers() {
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
        etDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddTaskActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formatted = String.format(Locale.US, "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                        etDueDate.setText(formatted);
                        etDueDate.setError(null);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        etDueTime.setFocusable(false);
        etDueTime.setClickable(true);
        etDueTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddTaskActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        String formatted = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                        etDueTime.setText(formatted);
                        etDueTime.setError(null);
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });
    }

    private void loadCategories() {
        ApiClient.get("get_categories.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    categories.clear();
                    categories.add(new Category(0, getString(R.string.no_category)));
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject item = result.getJSONObject(i);
                        categories.add(new Category(
                                item.getInt("id"),
                                item.getString("category_name")
                        ));
                    }
                    categoryAdapter.notifyDataSetChanged();
                } catch (JSONException error) {
                    Toast.makeText(
                            AddTaskActivity.this,
                            getString(R.string.category_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(
                        AddTaskActivity.this,
                        getString(R.string.categories_load_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void saveTask() {
        EditText titleField = findViewById(R.id.etTaskTitle);
        EditText descriptionField = findViewById(R.id.etDescription);
        String title = titleField.getText().toString().trim();
        if (title.isEmpty()) {
            titleField.setError(getString(R.string.title_required));
            return;
        }

        String rawDate = etDueDate.getText().toString().trim();
        if (rawDate.isEmpty()) {
            etDueDate.setError(getString(R.string.title_required));
            Toast.makeText(this, "Please select a due date.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String dueDate;
        try {
            dueDate = Task.toDatabaseDate(rawDate);
        } catch (IllegalArgumentException error) {
            etDueDate.setError(error.getMessage());
            return;
        }

        final String dueTime;
        try {
            dueTime = Task.toDatabaseTime(etDueTime.getText().toString());
        } catch (IllegalArgumentException error) {
            etDueTime.setError(error.getMessage());
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("description", descriptionField.getText().toString().trim());
        parameters.put("priority", spinnerPriority.getSelectedItem().toString());
        parameters.put("due_date", dueDate);
        if (!dueTime.isEmpty()) {
            parameters.put("due_time", dueTime);
        }
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory != null && selectedCategory.id > 0) {
            parameters.put("category_id", String.valueOf(selectedCategory.id));
        }

        btnSaveTask.setEnabled(false);
        ApiClient.post("add_task.php", parameters, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                btnSaveTask.setEnabled(true);
                try {
                    JSONObject result = new JSONObject(response);
                    if (result.optBoolean("success")) {
                        Toast.makeText(
                                AddTaskActivity.this,
                                R.string.task_added,
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    } else {
                        Toast.makeText(
                                AddTaskActivity.this,
                                result.optString("message", getString(R.string.task_save_failed)),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                } catch (JSONException error) {
                    Toast.makeText(
                            AddTaskActivity.this,
                            getString(R.string.task_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onError(Exception error) {
                btnSaveTask.setEnabled(true);
                Toast.makeText(
                        AddTaskActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
