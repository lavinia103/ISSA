package com.example.car_sharing_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private Button loginButton;
    private String urlLink = "http://192.168.0.45:8056/carsharing/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = editText.getText().toString();
                if (userId.isEmpty()) {
                    textView.setText("Please enter your ID");
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String clientId = userId;
                            int clientType = userId.startsWith("0") ? 0 : 1;
                            int messageId = userId.startsWith("0") ? 1 : 0;
                            String payload = "irrelevant for now";

                            URL url = new URL(urlLink);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json; utf-8");
                            conn.setRequestProperty("Accept", "*/*");

                            String jsonInputString = "{\"clientId\": \"" + clientId + "\", \"clientType\": " + clientType + ", \"messageId\": " + messageId + ", \"payload\": \"" + payload + "\"}";

                            // Log the JSON input string
                            Log.d("MainActivity", "POST request payload: " + jsonInputString);

                            try(OutputStream os = conn.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }

                            int responseCode = conn.getResponseCode();
                            Log.d("MainActivity", "Status code: " + responseCode);

                            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                                // User created successfully
                                Log.d("MainActivity", "User created successfully");
                            } else if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                                // User already registered
                                Log.d("MainActivity", "User already registered");
                            } else {
                                // Error handling
                                Log.d("MainActivity", "Error handling");
                            }

                            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String responseLine = null;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                                Log.d("MainActivity", "Response body: " + response.toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(response.toString());
                                        if(clientType == 0) {
                                            Intent intent = new Intent(MainActivity.this, OwnerActivity.class);
                                            intent.putExtra("indentOwnerId", clientId);
                                            startActivity(intent);
                                        } else if (clientType == 1){
                                            Intent intent = new Intent(MainActivity.this, RenterActivity.class);
                                            intent.putExtra("indentRenterId", clientId);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.e("MainActivity", "Error making network request", e);
                        }
                    }
                }).start();
            }
        });
    }
}