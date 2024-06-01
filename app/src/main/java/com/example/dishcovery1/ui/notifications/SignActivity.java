package com.example.dishcovery1.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.dishcovery1.R;
import com.example.dishcovery1.MainActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonSign;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        editTextUsername = findViewById(R.id.etName);
        editTextPassword = findViewById(R.id.etPassword);
        buttonSign = findViewById(R.id.buttonSign);
        progressBar = findViewById(R.id.progressBar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lamp.ms.wits.ac.za/home/s2538889/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        buttonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validate input
                if (TextUtils.isEmpty(username)) {
                    showError("Please provide a username");
                } else if (!isValidPassword(password)) {
                    showError("Password must be at least 6 characters");
                } else {
                    // Show progress bar
                    progressBar.setVisibility(View.VISIBLE);
                    // Sign up
                    signUp(username, password);
                }
            }
        });
    }

    // Method to show error toast
    private void showError(String message) {
        Toast.makeText(SignActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Method to validate password
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    // Method to perform sign-up
    private void signUp(String username, String password) {
        Call<JsonObject> call = apiService.signUp(username, password, 0); // Setting admin parameter to 0 for non-admin user
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    JsonObject responseObject = response.body();
                    if (responseObject != null && responseObject.has("success")) {
                        boolean success = responseObject.get("success").getAsBoolean();
                        if (success) {
                            // Sign-up successful
                            Toast.makeText(SignActivity.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                            // Navigate to MainActivity
                            Intent intent = new Intent(SignActivity.this, MainActivity.class);
                            intent.putExtra("USER_NAME", username);
                            startActivity(intent);
                            // Prevent going back to SignActivity
                        } else {
                            // Sign-up failed
                            String errorMessage = "Sign-up failed";
                            if (responseObject.has("message")) {
                                errorMessage += ": " + responseObject.get("message").getAsString();
                            }
                            showError(errorMessage);
                        }
                    } else {
                        showError("Unexpected response from server");
                    }
                } else {
                    // Server response error
                    showError("Server error. Please try again later.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Network error
                showError("Network error. Please check your connection.");
            }
        });
    }
}
