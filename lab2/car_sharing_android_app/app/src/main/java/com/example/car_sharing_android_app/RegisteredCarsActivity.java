package com.example.car_sharing_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class RegisteredCarsActivity extends AppCompatActivity {

    private Button buttonAddMoreCars;
    private LinearLayout carsLayout;
    private String urlLink = "http://192.168.0.45:8056/carsharing/";
    private String ownerId; // Declare ownerId here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_cars);

        buttonAddMoreCars = findViewById(R.id.buttonAddMoreCars);
        carsLayout = findViewById(R.id.carsLayout);

        ownerId = getIntent().getStringExtra("indentOwnerId"); // Assign ownerId here

        buttonAddMoreCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisteredCarsActivity.this, OwnerActivity.class);
                intent.putExtra("indentOwnerId", ownerId);
                startActivity(intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlLink + "?ownerId=" + ownerId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = new JSONObject(result.toString());
                        JSONObject carsObject = jsonObject.getJSONObject("cars");
                        Iterator<String> keys = carsObject.keys();
                        while (keys.hasNext()) {
                            String carId = keys.next();
                            String carOwnerId = carsObject.getString(carId);
                            if (carOwnerId.equals(ownerId)) { // Only add the car if the ownerId matches
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Button button = new Button(RegisteredCarsActivity.this);
                                        button.setText(carId);
                                        carsLayout.addView(button);
                                    }
                                });
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}