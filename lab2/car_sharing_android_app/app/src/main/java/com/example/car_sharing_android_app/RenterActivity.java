package com.example.car_sharing_android_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class RenterActivity extends AppCompatActivity {

    private LinearLayout carsLayout;
    private String urlLink = "http://192.168.0.45:8056/carsharing/";
    private String renterId; // Declare renterId here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter);

        carsLayout = findViewById(R.id.carsLayout);
        Button buttonRequestCar = findViewById(R.id.buttonRequestCar);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        renterId = getIntent().getStringExtra("indentRenterId"); // Assign renterId here

        buttonRequestCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                            String jsonInputString = "{\"clientId\": \"" + renterId + "\", \"clientType\": 1, \"messageId\": 3, \"payload\": \"" + renterId + "\"}";

                            try(OutputStream os = conn.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }

                            int responseCode = conn.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                StringBuilder result = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    result.append(line);
                                }
                                reader.close();

                                // Parse the response as a JSON object
                                JSONObject responseJson = new JSONObject(result.toString());

                                // Iterate over the list of available cars
                                Iterator<String> keys = responseJson.keys();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        carsLayout.removeAllViews(); // Clear the layout before adding new buttons
                                        while (keys.hasNext()) {
                                            String carId = keys.next();
                                            Button button = new Button(RenterActivity.this);
                                            button.setText(carId);
                                            carsLayout.addView(button);
                                        }
                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.e("RenterActivity", "Error making network request", e);
                        }
                    }
                }).start();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}