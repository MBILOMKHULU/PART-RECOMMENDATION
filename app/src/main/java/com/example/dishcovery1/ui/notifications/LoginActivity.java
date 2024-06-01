package com.example.dishcovery1.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextName, editTextPassword;
    private Button buttonLogin, buttonSign;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSign = findViewById(R.id.buttonSign);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lamp.ms.wits.ac.za/home/s2538889/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = editTextName.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validate input
                if (TextUtils.isEmpty(userName)) {
                    editTextName.setError("Please enter your username");
                } else if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("Please enter your password");
                } else {
                    logIn(userName, password);
                }
            }
        });

        buttonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void logIn(String username, String password) {
        Call<JsonObject> call = apiService.logIn(username, password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseObject = response.body();
                    if (responseObject != null && responseObject.get("success").getAsBoolean()) {
                        // Login successful
                        Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_NAME", username);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Server response error
                    Toast.makeText(getApplicationContext(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Network error
                Toast.makeText(getApplicationContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
