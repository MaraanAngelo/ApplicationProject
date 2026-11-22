package com.example.applicationproject;

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

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private LinearLayout notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notificationsList = findViewById(R.id.notificationsList);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        showMessage(getString(R.string.loading_notifications));
        ApiClient.get("get_task.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    notificationsList.removeAllViews();
                    ArrayList<Task> pendingTasks = new ArrayList<>();
                    for (int i = 0; i < result.length(); i++) {
                        Task task = Task.fromJson(result.getJSONObject(i));
                        if ("Pending".equalsIgnoreCase(task.status)) {
                            pendingTasks.add(task);
                        }
                    }

                    if (pendingTasks.isEmpty()) {
                        showMessage(getString(R.string.no_upcoming_notifications));
                    } else {
                        for (Task task : pendingTasks) {
                            notificationsList.addView(createNotificationItem(task));
                        }
                    }
                } catch (JSONException error) {
                    showMessage(getString(R.string.unable_to_read_tasks));
                    Toast.makeText(NotificationsActivity.this, getString(R.string.task_response_error, error.getMessage()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception error) {
                showMessage(getString(R.string.unable_to_load_tasks));
                Toast.makeText(NotificationsActivity.this, getString(R.string.connection_error, error.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private View createNotificationItem(Task task) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, dp(20));

        TextView title = new TextView(this);
        title.setText(task.title);
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title);

        TextView dateBadge = new TextView(this);
        String dueDate = Task.toDisplayDate(task.dueDate);
        String time = Task.toDisplayTime(task.dueTime);
        String dueText = time.isEmpty()
                ? getString(R.string.due_date_prefix, dueDate)
                : getString(R.string.due_prefix, dueDate + " " + time);
        dateBadge.setText(dueText);
        dateBadge.setTextColor(getColor(R.color.text_dark));
        dateBadge.setTextSize(14);
        dateBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        dateBadge.setBackgroundResource(R.drawable.bg_date_badge);
        dateBadge.setPadding(dp(8), dp(6), dp(8), dp(6));

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        badgeParams.topMargin = dp(6);
        badgeParams.bottomMargin = dp(8);
        dateBadge.setLayoutParams(badgeParams);
        card.addView(dateBadge);

        TextView message = new TextView(this);
        String desc = (task.description != null && !task.description.trim().isEmpty())
                ? task.description
                : getString(R.string.default_notification_message);
        message.setText(desc);
        message.setTextColor(getColor(R.color.text_dark));
        message.setTextSize(15);

        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.bottomMargin = dp(14);
        message.setLayoutParams(msgParams);
        card.addView(message);

        Button btnDismiss = new Button(this);
        btnDismiss.setText(R.string.dismiss);
        btnDismiss.setTextColor(getColor(R.color.text_dark));
        btnDismiss.setTextSize(16);
        btnDismiss.setTypeface(null, android.graphics.Typeface.BOLD);
        btnDismiss.setBackgroundResource(R.drawable.bg_button_white);
        btnDismiss.setBackgroundTintList(null);
        btnDismiss.setOnClickListener(v -> {
            notificationsList.removeView(card);
            if (notificationsList.getChildCount() == 0) {
                showMessage(getString(R.string.no_upcoming_notifications));
            }
            Toast.makeText(this, R.string.notification_dismissed, Toast.LENGTH_SHORT).show();
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                dp(120),
                dp(48)
        );
        btnDismiss.setLayoutParams(btnParams);
        card.addView(btnDismiss);

        return card;
    }

    private void showMessage(String msg) {
        notificationsList.removeAllViews();
        TextView text = new TextView(this);
        text.setText(msg);
        text.setTextColor(getColor(R.color.text_dark));
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        notificationsList.addView(text, new LinearLayout.LayoutParams(-1, dp(72)));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
