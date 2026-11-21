package com.example.applicationproject;

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
import java.util.HashMap;
import java.util.Map;

public class EditTaskActivity extends AppCompatActivity {
    private final ArrayList<Category> categories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;
    private Spinner spinnerCategory;
    private Button btnSaveChanges;
    private Button btnDelete;
    private int taskId;
    private int selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskId = getIntent().getIntExtra("task_id", 0);
        selectedCategoryId = getIntent().getIntExtra("task_category_id", 0);
        ((EditText) findViewById(R.id.etTaskTitle))
                .setText(getIntent().getStringExtra("task_title"));
        ((EditText) findViewById(R.id.etDescription))
                .setText(getIntent().getStringExtra("task_description"));
        ((EditText) findViewById(R.id.etDueDate))
                .setText(Task.toFormDate(getIntent().getStringExtra("task_due_date")));

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

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void loadCategories() {
        ApiClient.get("get_categories.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    categories.clear();
                    categories.add(new Category(0, getString(R.string.no_category)));
                    int selectedPosition = 0;
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject item = result.getJSONObject(i);
                        Category category = new Category(
                                item.getInt("id"),
                                item.getString("category_name")
                        );
                        categories.add(category);
                        if (category.id == selectedCategoryId) {
                            selectedPosition = categories.size() - 1;
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                    spinnerCategory.setSelection(selectedPosition);
                } catch (JSONException error) {
                    Toast.makeText(
                            EditTaskActivity.this,
                            getString(R.string.category_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(
                        EditTaskActivity.this,
                        getString(R.string.categories_load_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void saveChanges() {
        EditText titleField = findViewById(R.id.etTaskTitle);
        EditText descriptionField = findViewById(R.id.etDescription);
        EditText dueDateField = findViewById(R.id.etDueDate);
        String title = titleField.getText().toString().trim();
        if (title.isEmpty()) {
            titleField.setError(getString(R.string.title_required));
            return;
        }
        if (taskId <= 0) {
            Toast.makeText(this, R.string.task_id_missing, Toast.LENGTH_LONG).show();
            return;
        }

        final String dueDate;
        try {
            dueDate = Task.toDatabaseDate(dueDateField.getText().toString().trim());
        } catch (IllegalArgumentException error) {
            dueDateField.setError(error.getMessage());
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(taskId));
        parameters.put("title", title);
        parameters.put("description", descriptionField.getText().toString().trim());
        parameters.put("priority", getIntent().getStringExtra("task_priority") == null
                ? "Medium" : getIntent().getStringExtra("task_priority"));
        parameters.put("due_date", dueDate);
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory != null && selectedCategory.id > 0) {
            parameters.put("category_id", String.valueOf(selectedCategory.id));
        }

        btnSaveChanges.setEnabled(false);
        ApiClient.post("update_task.php", parameters, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                btnSaveChanges.setEnabled(true);
                showMutationResult(response, R.string.task_updated);
            }

            @Override
            public void onError(Exception error) {
                btnSaveChanges.setEnabled(true);
                Toast.makeText(
                        EditTaskActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void deleteTask() {
        if (taskId <= 0) {
            Toast.makeText(this, R.string.task_id_missing, Toast.LENGTH_LONG).show();
            return;
        }

        btnDelete.setEnabled(false);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(taskId));
        ApiClient.post("delete_task.php", parameters, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                btnDelete.setEnabled(true);
                showMutationResult(response, R.string.task_deleted);
            }

            @Override
            public void onError(Exception error) {
                btnDelete.setEnabled(true);
                Toast.makeText(
                        EditTaskActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showMutationResult(String response, int successMessage) {
        try {
            JSONObject result = new JSONObject(response);
            if (result.optBoolean("success")) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(
                        this,
                        result.optString("message", getString(R.string.task_save_failed)),
                        Toast.LENGTH_LONG
                ).show();
            }
        } catch (JSONException error) {
            Toast.makeText(
                    this,
                    getString(R.string.task_response_error, error.getMessage()),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
