package com.example.car_sharing_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OwnerActivity extends AppCompatActivity {
    private Button buttonViewCars;
    private EditText editTextCarId;
    private Button buttonRegisterCar;
    private Button buttonLogout;
    private String urlLink = "http://192.168.0.45:8056/carsharing/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        editTextCarId = findViewById(R.id.editTextCarId);
        buttonRegisterCar = findViewById(R.id.buttonRegisterCar);
        buttonViewCars = findViewById(R.id.buttonViewCars);
        buttonLogout = findViewById(R.id.buttonLogout);

        String ownerId = getIntent().getStringExtra("indentOwnerId");

        buttonViewCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerActivity.this, RegisteredCarsActivity.class);
                intent.putExtra("indentOwnerId", ownerId);
                startActivity(intent);
            }
        });

        buttonRegisterCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String carId = editTextCarId.getText().toString();
                if (carId.isEmpty()) {
                    Log.d("OwnerActivity", "Please enter a car ID");
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(urlLink);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json; utf-8");
                            conn.setRequestProperty("Accept", "*/*");

                            String jsonInputString = "{\"clientId\": \"" + carId + "\", \"clientType\": 0, \"messageId\": 2, \"payload\": \"" + ownerId + "\"}";

                            // Log the JSON input string
                            Log.d("OwnerActivity", "POST request payload: " + jsonInputString);

                            try(OutputStream os = conn.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }

                            int responseCode = conn.getResponseCode();
                            Log.d("OwnerActivity", "Status code: " + responseCode);

                            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String responseLine = null;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                                Log.d("OwnerActivity", "Response body: " + response.toString());
                            }

                        } catch (Exception e) {
                            Log.e("OwnerActivity", "Error making network request", e);
                        }
                    }
                }).start();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}