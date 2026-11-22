package com.example.applicationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity {
    private final ArrayList<Task> tasks = new ArrayList<>();
    private final ArrayList<Category> categories = new ArrayList<>();
    private LinearLayout taskList;
    private Spinner categoryFilter;
    private ArrayAdapter<Category> categoryAdapter;
    private TextView taskCount;
    private boolean updatingFilter;
    private boolean categoriesLoaded;
    private boolean tasksLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        ApiClient.init(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskList = findViewById(R.id.taskList);
        taskCount = findViewById(R.id.tvTaskCount);
        categoryFilter = findViewById(R.id.spinnerCategoryFilter);
        categories.add(new Category(-1, getString(R.string.all_categories)));
        categories.add(new Category(0, getString(R.string.uncategorized)));
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilter.setAdapter(categoryAdapter);
        categoryFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (!updatingFilter) {
                    renderTasks();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnServerSettings).setOnClickListener(v -> showServerSettingsDialog());
        findViewById(R.id.btnCategories).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CategoriesActivity.class))
        );
        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddTaskActivity.class))
        );
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class))
        );

        Button btnHomeNav = findViewById(R.id.btnHomeNav);
        btnHomeNav.setOnClickListener(v ->
                Toast.makeText(this, R.string.home_screen, Toast.LENGTH_SHORT).show()
        );

        Button btnCompletedNav = findViewById(R.id.btnCompletedNav);
        btnCompletedNav.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CompletedActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
        loadTasks();
    }

    private void showServerSettingsDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        layout.setPadding(padding, dp(12), padding, 0);

        TextView label = new TextView(this);
        label.setText("Current Server URL / IP:");
        label.setTextColor(getColor(R.color.text_dark));
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(label);

        EditText urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setText(ApiClient.getActiveBaseUrl(this));
        urlInput.setHint(R.string.server_url_hint);
        layout.addView(urlInput);

        LinearLayout presetRow = new LinearLayout(this);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        presetRow.setPadding(0, dp(10), 0, 0);

        Button btnLanPreset = new Button(this);
        btnLanPreset.setText("Wi-Fi IP");
        btnLanPreset.setTextSize(12);
        btnLanPreset.setOnClickListener(v -> urlInput.setText(ApiClient.LAN_URL));
        presetRow.addView(btnLanPreset, new LinearLayout.LayoutParams(0, -2, 1));

        Button btnEmuPreset = new Button(this);
        btnEmuPreset.setText("Emulator IP");
        btnEmuPreset.setTextSize(12);
        btnEmuPreset.setOnClickListener(v -> urlInput.setText(ApiClient.EMULATOR_URL));
        presetRow.addView(btnEmuPreset, new LinearLayout.LayoutParams(0, -2, 1));

        layout.addView(presetRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.server_url_title)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.test_connection, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String target = urlInput.getText().toString().trim();
            if (target.isEmpty()) {
                urlInput.setError("Enter IP address");
                return;
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            Toast.makeText(MainActivity.this, "Testing connection...", Toast.LENGTH_SHORT).show();

            ApiClient.testConnection(target, new ApiClient.Callback() {
                @Override
                public void onSuccess(String response) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    ApiClient.setCustomBaseUrl(MainActivity.this, target);
                    Toast.makeText(MainActivity.this, R.string.connection_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadCategories();
                    loadTasks();
                }

                @Override
                public void onError(Exception error) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Connection Failed")
                            .setMessage("Could not connect to " + ApiClient.formatBaseUrl(target) + ".\n\nError: " + error.getMessage() + "\n\nMake sure XAMPP Apache is running and unblocked in Windows Firewall.")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });
        }));

        dialog.show();
    }

    private void loadCategories() {
        ApiClient.get("get_categories.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    Category selected = (Category) categoryFilter.getSelectedItem();
                    int selectedId = selected == null ? -1 : selected.id;
                    categories.clear();
                    categories.add(new Category(-1, getString(R.string.all_categories)));
                    categories.add(new Category(0, getString(R.string.uncategorized)));
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject item = result.getJSONObject(i);
                        categories.add(new Category(
                                item.getInt("id"),
                                item.getString("category_name")
                        ));
                    }
                    updatingFilter = true;
                    categoryAdapter.notifyDataSetChanged();
                    int selectedPosition = 0;
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).id == selectedId) {
                            selectedPosition = i;
                            break;
                        }
                    }
                    categoryFilter.setSelection(selectedPosition);
                    updatingFilter = false;
                    categoriesLoaded = true;
                    renderTasks();
                } catch (JSONException error) {
                    updatingFilter = false;
                    categoriesLoaded = true;
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.category_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                    renderTasks();
                }
            }

            @Override
            public void onError(Exception error) {
                categoriesLoaded = true;
                Toast.makeText(
                        MainActivity.this,
                        getString(R.string.categories_load_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void loadTasks() {
        tasksLoaded = false;
        tasks.clear();
        taskCount.setText(R.string.loading_tasks);
        showMessage(getString(R.string.loading_tasks));
        ApiClient.get("get_task.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    tasks.clear();
                    for (int i = 0; i < result.length(); i++) {
                        tasks.add(Task.fromJson(result.getJSONObject(i)));
                    }
                    tasksLoaded = true;
                    renderTasks();
                } catch (JSONException error) {
                    tasksLoaded = false;
                    taskCount.setText(R.string.unable_to_read_tasks);
                    showMessage(getString(R.string.unable_to_read_tasks));
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.task_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onError(Exception error) {
                tasksLoaded = false;
                taskCount.setText(R.string.unable_to_load_tasks);
                showConnectionErrorMessage(error.getMessage());
            }
        });
    }

    private void showConnectionErrorMessage(String errorMessage) {
        taskList.removeAllViews();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView text = new TextView(this);
        text.setText("Cannot connect to server.\nTap below to configure Server IP.");
        text.setTextColor(getColor(R.color.text_dark));
        text.setTextSize(15);
        text.setGravity(Gravity.CENTER);
        layout.addView(text);

        Button btnConfig = new Button(this);
        btnConfig.setText(R.string.server_settings);
        btnConfig.setTextColor(getColor(R.color.text_dark));
        btnConfig.setTextSize(14);
        btnConfig.setTypeface(null, android.graphics.Typeface.BOLD);
        btnConfig.setBackgroundResource(R.drawable.bg_button_white);
        btnConfig.setBackgroundTintList(null);
        btnConfig.setOnClickListener(v -> showServerSettingsDialog());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(180), dp(48));
        params.topMargin = dp(12);
        btnConfig.setLayoutParams(params);
        layout.addView(btnConfig);

        taskList.addView(layout, new LinearLayout.LayoutParams(-1, -2));
    }

    private void renderTasks() {
        if (taskList == null || categoryFilter == null || !tasksLoaded || !categoriesLoaded) {
            return;
        }
        Category selected = (Category) categoryFilter.getSelectedItem();
        int selectedCategoryId = selected == null ? -1 : selected.id;
        taskList.removeAllViews();
        int visibleCount = 0;
        for (Task task : tasks) {
            if (!"Pending".equalsIgnoreCase(task.status)) {
                continue;
            }
            if (selectedCategoryId != -1 && task.categoryId != selectedCategoryId) {
                continue;
            }
            taskList.addView(createTaskRow(task));
            visibleCount++;
        }

        taskCount.setText(getString(R.string.tasks_count, visibleCount));
        if (visibleCount == 0) {
            showMessage(tasks.isEmpty()
                    ? getString(R.string.no_pending_tasks)
                    : getString(R.string.no_tasks_for_category));
        }
    }

    private View createTaskRow(Task task) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_button_white);
        row.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(task.title);
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(17);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        titleRow.addView(title);

        TextView completeBtn = new TextView(this);
        completeBtn.setText(R.string.complete_task);
        completeBtn.setTextColor(getColor(R.color.app_teal));
        completeBtn.setTextSize(14);
        completeBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        completeBtn.setGravity(Gravity.CENTER);
        completeBtn.setMinWidth(dp(64));
        completeBtn.setMinHeight(dp(44));
        completeBtn.setPadding(dp(4), 0, dp(4), 0);
        completeBtn.setOnClickListener(v -> markTaskCompleted(task));
        titleRow.addView(completeBtn);

        TextView edit = new TextView(this);
        edit.setText(R.string.edit);
        edit.setTextColor(getColor(R.color.text_dark));
        edit.setTextSize(14);
        edit.setTypeface(null, android.graphics.Typeface.BOLD);
        edit.setGravity(Gravity.CENTER);
        edit.setMinWidth(dp(48));
        edit.setMinHeight(dp(44));
        edit.setOnClickListener(v -> openEditTask(task));
        titleRow.addView(edit);
        row.addView(titleRow);

        Category category = findCategory(task.categoryId);
        String categoryName = category == null
                ? getString(R.string.uncategorized)
                : category.name;
        String dueDate = Task.toDisplayDate(task.dueDate);
        String time = Task.toDisplayTime(task.dueTime);
        String dueDetails = time.isEmpty() ? dueDate : dueDate + " " + time;

        TextView details = new TextView(this);
        details.setText(getString(
                R.string.task_details,
                dueDetails,
                categoryName,
                task.priority
        ));
        details.setTextColor(getColor(R.color.text_secondary));
        details.setTextSize(13);
        row.addView(details);

        if (task.description != null && !task.description.trim().isEmpty()) {
            TextView desc = new TextView(this);
            desc.setText(task.description);
            desc.setTextColor(getColor(R.color.text_dark));
            desc.setTextSize(14);
            desc.setPadding(0, dp(4), 0, 0);
            row.addView(desc);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        row.setLayoutParams(params);
        return row;
    }

    private void markTaskCompleted(Task task) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(task.id));
        parameters.put("title", task.title);
        parameters.put("description", task.description);
        parameters.put("priority", task.priority);
        parameters.put("due_date", task.dueDate);
        if (task.dueTime != null && !task.dueTime.isEmpty()) {
            parameters.put("due_time", task.dueTime);
        }
        if (task.categoryId > 0) {
            parameters.put("category_id", String.valueOf(task.categoryId));
        }
        parameters.put("status", "Completed");

        ApiClient.post("update_task.php", parameters, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(MainActivity.this, R.string.task_completed, Toast.LENGTH_SHORT).show();
                loadTasks();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error, error.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Category findCategory(int id) {
        for (Category category : categories) {
            if (category.id == id) {
                return category;
            }
        }
        return null;
    }

    private void openEditTask(Task task) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task_id", task.id);
        intent.putExtra("task_title", task.title);
        intent.putExtra("task_description", task.description);
        intent.putExtra("task_category_id", task.categoryId);
        intent.putExtra("task_priority", task.priority);
        intent.putExtra("task_due_date", task.dueDate);
        intent.putExtra("task_due_time", task.dueTime);
        startActivity(intent);
    }

    private void showMessage(String message) {
        taskList.removeAllViews();
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(getColor(R.color.text_secondary));
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        taskList.addView(text, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
        ));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
