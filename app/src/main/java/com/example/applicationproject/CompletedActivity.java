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

public class CompletedActivity extends AppCompatActivity {

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

        // Top bar buttons
        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v ->
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        );

        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(CompletedActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Undo / Delete actions
        TextView tvUndo1 = findViewById(R.id.tvUndo1);
        tvUndo1.setOnClickListener(v -> Toast.makeText(this, "Task moved back to To-Do", Toast.LENGTH_SHORT).show());

        TextView tvDelete1 = findViewById(R.id.tvDelete1);
        tvDelete1.setOnClickListener(v -> Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show());

        TextView tvUndo2 = findViewById(R.id.tvUndo2);
        tvUndo2.setOnClickListener(v -> Toast.makeText(this, "Task moved back to To-Do", Toast.LENGTH_SHORT).show());

        TextView tvDelete2 = findViewById(R.id.tvDelete2);
        tvDelete2.setOnClickListener(v -> Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show());

        // Bottom Navigation: Home Button -> Switches to MainActivity
        Button btnHomeNav = findViewById(R.id.btnHomeNav);
        btnHomeNav.setOnClickListener(v -> {
            Intent intent = new Intent(CompletedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation: Completed Button
        Button btnCompletedNav = findViewById(R.id.btnCompletedNav);
        btnCompletedNav.setOnClickListener(v -> 
            Toast.makeText(this, "Already on Completed screen", Toast.LENGTH_SHORT).show()
        );
    }
}
