//ДЛЯ БУДУЩИХ ВЕРСИЙ

package com.ymp.wled.tv;

import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SetEffectTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        if (params.length < 1) {
            return null;
        }

        try {
            String apiUrl = "http://192.168.0.100/json/state"; // Замените на актуальный URL вашего WLED устройства
            URL url = new URL(apiUrl);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // Создаем JSON-строку для отправки
            String jsonInputString = "{\"seg\":[{\"fx\":" + params[0] + "}]}";

            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Получаем ответ от сервера (если это нужно)
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Успешно
            } else {
                // Ошибка
            }

            urlConnection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}