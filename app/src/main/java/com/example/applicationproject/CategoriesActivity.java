package com.example.applicationproject;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class CategoriesActivity extends AppCompatActivity {
    private final ArrayList<Category> categories = new ArrayList<>();
    private LinearLayout categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryList = findViewById(R.id.categoryList);
        findViewById(R.id.btnCategoriesBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showCategoryDialog(null));
        loadCategories();
    }

    private void loadCategories() {
        showMessage(getString(R.string.loading_categories));
        ApiClient.get("get_categories.php", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray result = new JSONArray(response);
                    categories.clear();
                    categoryList.removeAllViews();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject item = result.getJSONObject(i);
                        Category category = new Category(
                                item.getInt("id"),
                                item.getString("category_name")
                        );
                        categories.add(category);
                        categoryList.addView(createCategoryRow(category));
                    }
                    if (categories.isEmpty()) {
                        showMessage(getString(R.string.no_categories));
                    }
                } catch (JSONException error) {
                    showMessage(getString(R.string.categories_load_error, error.getMessage()));
                }
            }

            @Override
            public void onError(Exception error) {
                showMessage(getString(R.string.categories_load_error, error.getMessage()));
            }
        });
    }

    private View createCategoryRow(Category category) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_button_white);
        row.setPadding(dp(16), dp(8), dp(8), dp(8));

        TextView name = new TextView(this);
        name.setText(category.name);
        name.setTextColor(getColor(R.color.text_dark));
        name.setTextSize(17);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

        TextView edit = actionText(getString(R.string.edit));
        edit.setOnClickListener(v -> showCategoryDialog(category));
        row.addView(edit);

        TextView delete = actionText(getString(R.string.delete));
        delete.setTextColor(getColor(R.color.category_delete));
        delete.setOnClickListener(v -> confirmDelete(category));
        row.addView(delete);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64)
        );
        params.bottomMargin = dp(12);
        row.setLayoutParams(params);
        return row;
    }

    private TextView actionText(String label) {
        TextView action = new TextView(this);
        action.setText(label);
        action.setTextColor(getColor(R.color.text_dark));
        action.setTextSize(14);
        action.setTypeface(null, android.graphics.Typeface.BOLD);
        action.setGravity(Gravity.CENTER);
        action.setMinWidth(dp(56));
        action.setMinHeight(dp(48));
        action.setPadding(dp(8), 0, dp(8), 0);
        action.setBackgroundResource(android.R.drawable.list_selector_background);
        action.setClickable(true);
        action.setFocusable(true);
        return action;
    }

    private void showCategoryDialog(Category category) {
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setHint(R.string.category_name_hint);
        nameInput.setText(category == null ? "" : category.name);
        int padding = dp(20);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(category == null
                        ? R.string.add_category_title
                        : R.string.edit_category_title)
                .setView(nameInput, padding, dp(8), padding, 0)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(category == null
                        ? R.string.add_category_action
                        : R.string.save_changes, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        nameInput.setError(getString(R.string.category_name_required));
                        return;
                    }

                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("category_name", name);
                    String endpoint = "add_category.php";
                    int successMessage = R.string.category_added;
                    if (category != null) {
                        endpoint = "update_category.php";
                        successMessage = R.string.category_updated;
                        parameters.put("id", String.valueOf(category.id));
                    }
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    saveCategory(endpoint, parameters, successMessage, dialog);
                }));
        dialog.show();
    }

    private void saveCategory(
            String endpoint,
            Map<String, String> parameters,
            int successMessage,
            AlertDialog dialog
    ) {
        ApiClient.post(endpoint, parameters, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                handleMutationResponse(response, successMessage, dialog);
            }

            @Override
            public void onError(Exception error) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(
                        CategoriesActivity.this,
                        getString(R.string.connection_error, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_category_title)
                .setMessage(getString(R.string.delete_category_confirmation, category.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("id", String.valueOf(category.id));
                    ApiClient.post("delete_category.php", parameters, new ApiClient.Callback() {
                        @Override
                        public void onSuccess(String response) {
                            handleMutationResponse(response, R.string.category_deleted, null);
                        }

                        @Override
                        public void onError(Exception error) {
                            Toast.makeText(
                                    CategoriesActivity.this,
                                    getString(R.string.connection_error, error.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                })
                .show();
    }

    private void handleMutationResponse(String response, int successMessage, AlertDialog dialog) {
        try {
            JSONObject result = new JSONObject(response);
            if (result.optBoolean("success")) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                String message = result.optString("message", getString(R.string.category_action_failed));
                if (dialog != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException error) {
            if (dialog != null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
            Toast.makeText(
                    this,
                    getString(R.string.category_response_error, error.getMessage()),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showMessage(String message) {
        categoryList.removeAllViews();
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(getColor(R.color.text_dark));
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        categoryList.addView(text, new LinearLayout.LayoutParams(-1, dp(72)));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
