package com.example.applicationproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        // Notification Bell Button -> Opens NotificationsActivity
        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Add Task Plus Button -> Opens AddTaskActivity
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Edit Task 1 -> Opens EditTaskActivity
        TextView tvEditCoding = findViewById(R.id.tvEditCoding);
        tvEditCoding.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
            intent.putExtra("task_title", "Coding Assignment");
            startActivity(intent);
        });

        // Edit Task 2 -> Opens EditTaskActivity
        TextView tvEditResearch = findViewById(R.id.tvEditResearch);
        tvEditResearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
            intent.putExtra("task_title", "Research Paper");
            startActivity(intent);
        });

        // Bottom Navigation Home
        Button btnHomeNav = findViewById(R.id.btnHomeNav);
        btnHomeNav.setOnClickListener(v -> 
            Toast.makeText(this, "Home screen", Toast.LENGTH_SHORT).show()
        );

        // Bottom Navigation Completed -> Opens CompletedActivity
        Button btnCompletedNav = findViewById(R.id.btnCompletedNav);
        btnCompletedNav.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CompletedActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
