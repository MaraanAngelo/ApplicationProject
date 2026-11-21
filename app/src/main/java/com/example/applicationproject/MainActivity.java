package com.example.applicationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private LinearLayout taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskList = findViewById(R.id.taskList);
        TextView categoryHeader = findViewById(R.id.tvCategoryHeader);
        categoryHeader.setText(R.string.pending_tasks);

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        Button btnHomeNav = findViewById(R.id.btnHomeNav);
        btnHomeNav.setOnClickListener(v ->
                Toast.makeText(this, R.string.home_screen, Toast.LENGTH_SHORT).show()
        );

        Button btnCompletedNav = findViewById(R.id.btnCompletedNav);
        btnCompletedNav.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CompletedActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        taskList.removeAllViews();
        showMessage(getString(R.string.loading_tasks));

        ApiClient.get("get_task.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonTasks = new JSONArray(response);
                    taskList.removeAllViews();
                    int pendingCount = 0;
                    for (int i = 0; i < jsonTasks.length(); i++) {
                        Task task = Task.fromJson(jsonTasks.getJSONObject(i));
                        if ("Pending".equalsIgnoreCase(task.status)) {
                            taskList.addView(createTaskRow(task));
                            pendingCount++;
                        }
                    }
                    if (pendingCount == 0) {
                        showMessage(getString(R.string.no_pending_tasks));
                    }
                } catch (JSONException error) {
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
                showMessage(getString(R.string.unable_to_load_tasks));
                Toast.makeText(
                        MainActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private View createTaskRow(Task task) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, dp(16));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(task.title);
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        titleRow.addView(title);

        Button edit = new Button(this);
        edit.setText(R.string.edit);
        titleRow.addView(edit, new LinearLayout.LayoutParams(-2, dp(48)));
        edit.setOnClickListener(v -> openEditTask(task));
        row.addView(titleRow);

        TextView dueDate = new TextView(this);
        dueDate.setText(getString(R.string.due_date_value, Task.toDisplayDate(task.dueDate)));
        dueDate.setTextColor(getColor(R.color.text_dark));
        dueDate.setTextSize(13);
        row.addView(dueDate);

        View divider = new View(this);
        divider.setBackgroundColor(getColor(R.color.border_dark));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        dividerParams.topMargin = dp(12);
        row.addView(divider, dividerParams);
        return row;
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
        text.setTextColor(getColor(R.color.text_dark));
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
