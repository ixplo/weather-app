package com.example.weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final String KEY = "fae0f637bb0b053d7a4fa65599a9a71b";
    public static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&lang=ru&appid=%s";
    public static final String GET_COORDINATES_URL = "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s";
    private EditText userField;
    private Button mainBtn;
    private TextView resultInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userField = findViewById(R.id.user_field);
        mainBtn = findViewById(R.id.main_btn);
        resultInfo = findViewById(R.id.result_info);

        mainBtn.setOnClickListener(v -> {
            if (userField.getText().toString().trim().equals("")) {
                Toast.makeText(MainActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
            } else {
                String city = userField.getText().toString();
                String url = String.format(GET_COORDINATES_URL, city, KEY);

                new GetURLData().execute(url);
            }
        });
    }

    private class GetURLData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            resultInfo.setText(R.string.after_btn_click);
        }

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            String result = openUrl(urlString);
            double lat;
            double lon;
            try {
                JSONArray json = new JSONArray(result);
                lat = json.getJSONObject(0).getDouble("lat");
                lon = json.getJSONObject(0).getDouble("lon");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            String weatherUrl = String.format(WEATHER_URL, lat, lon, KEY);
            return openUrl(weatherUrl);
        }

        @NonNull
        private String openUrl(String urlString) {
            URL url;
            HttpURLConnection connection;
            try {
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try (AutoCloseable c = connection::disconnect;
                 InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                connection.connect();
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            double temp;
            String description;
            double windSpeed;
            try {
                JSONObject jsonObject = new JSONObject(result);
                temp = jsonObject.getJSONObject("main").getDouble("temp");
                description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            String weatherResult = String.format("%s\n%s%4.0f\n%s%4.0f", description, "Температура: ", temp, "Скорость ветра: ", windSpeed);
            resultInfo.setText(weatherResult);
        }
    }
}
