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

public class CompletedActivity extends AppCompatActivity {
    private LinearLayout taskList;
    private TextView taskCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completed_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskList = findViewById(R.id.completedTaskList);
        taskCount = findViewById(R.id.tvCompletedCount);

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class))
        );
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddTaskActivity.class))
        );

        findViewById(R.id.btnHomeNav).setOnClickListener(v ->
                finish()
        );
        findViewById(R.id.btnCompletedNav).setOnClickListener(v ->
                Toast.makeText(this, R.string.already_on_completed, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        taskList.removeAllViews();
        taskCount.setText(R.string.loading_tasks);
        showMessage(getString(R.string.loading_tasks));
        ApiClient.get("get_task.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    taskList.removeAllViews();
                    int count = 0;
                    for (int i = 0; i < result.length(); i++) {
                        Task task = Task.fromJson(result.getJSONObject(i));
                        if ("Completed".equalsIgnoreCase(task.status)) {
                            taskList.addView(createTaskRow(task));
                            count++;
                        }
                    }
                    taskCount.setText(getString(R.string.tasks_count, count));
                    if (count == 0) {
                        showMessage(getString(R.string.no_completed_tasks));
                    }
                } catch (JSONException error) {
                    taskCount.setText(R.string.unable_to_read_tasks);
                    showMessage(getString(R.string.unable_to_read_tasks));
                    Toast.makeText(
                            CompletedActivity.this,
                            getString(R.string.task_response_error, error.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onError(Exception error) {
                taskCount.setText(R.string.unable_to_load_tasks);
                showMessage(getString(R.string.unable_to_load_tasks));
                Toast.makeText(
                        CompletedActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
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

        TextView edit = new TextView(this);
        edit.setText(R.string.edit);
        edit.setTextColor(getColor(R.color.app_teal));
        edit.setTextSize(14);
        edit.setTypeface(null, android.graphics.Typeface.BOLD);
        edit.setGravity(Gravity.CENTER);
        edit.setMinWidth(dp(52));
        edit.setMinHeight(dp(44));
        edit.setOnClickListener(v -> openEditTask(task));
        titleRow.addView(edit);
        row.addView(titleRow);

        TextView details = new TextView(this);
        String dueDate = Task.toDisplayDate(task.dueDate);
        String time = Task.toDisplayTime(task.dueTime);
        details.setText(getString(
                R.string.completed_task_details,
                time.isEmpty() ? dueDate : dueDate + " " + time,
                task.priority
        ));
        details.setTextColor(getColor(R.color.text_secondary));
        details.setTextSize(13);
        row.addView(details);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        row.setLayoutParams(params);
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
        text.setTextColor(getColor(R.color.text_secondary));
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        taskList.addView(text, new LinearLayout.LayoutParams(-1, dp(72)));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
