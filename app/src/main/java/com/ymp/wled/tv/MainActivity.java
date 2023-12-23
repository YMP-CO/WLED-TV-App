package com.ymp.wled.tv;

import static android.app.PendingIntent.getActivity;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "WLED_PREFS";
    public static final String WLED_IP_KEY = "WLED_IP";
    private static final String FIRST_RUN_KEY = "FIRST_RUN";

    private boolean isWLEDOn = false;
    private String wledIP;
    private void openWebViewActivity(String url) {
        Intent intent = new Intent(this, BrightnessSpeedCustomColor.class);
        intent.putExtra(BrightnessSpeedCustomColor.EXTRA_URL, url);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(FIRST_RUN_KEY, true);





        if (isFirstRun) {

        }

        Button wledButton = findViewById(R.id.wledButton);

        wledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, activity_custom_power.class);
                startActivity(intent);
            }
        });

        Button DonateButton = findViewById(R.id.donateButton);

        DonateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomAlertDialog();
            }
        });


        Button effectsButton = findViewById(R.id.EffectsButton);

        effectsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Передаем IP в activity_custom
                launchCustomActivity();
            }
        });

        Button colorsButton = findViewById(R.id.ColorsButton);

        colorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь запускается CustomActivity2
                Intent intent = new Intent(MainActivity.this, activity_custom_2.class);
                startActivity(intent);
            }
        });

        Button settingsButton;
        settingsButton = findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь запускается SettingsActivity
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });



        Button PalettesButton = findViewById(R.id.PalettesButton);

        PalettesButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Здесь запускается CustomActivity2
            Intent intent = new Intent(MainActivity.this, activity_custom_3.class);
            startActivity(intent);
        }
    });
}
    private void showCustomAlertDialog() {
        // Создаем макет для диалога
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_donate, null);

        // Создаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действие при нажатии на кнопку "OK"
                        dialog.dismiss();
                    }
                });

        // Показываем AlertDialog
        builder.create().show();
    }
    private String getWLEDState(String wledIP) {
        String state = "OFF"; // По умолчанию считаем, что WLED выключен

        try {
            URL url = new URL("http://" + wledIP + "/json/state");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                StringBuilder jsonStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonStringBuilder.append(line).append('\n');
                }

                JSONObject jsonObject = new JSONObject(jsonStringBuilder.toString());
                if (jsonObject.has("state")) {
                    state = jsonObject.getString("state");
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return state;
    }

    private void changeIpAddress() {
        // Очищаем данные приложения
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Перезапускаем приложение
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    private void launchCustomActivity() {
        // Передаем IP в activity_custom
        Intent intent = new Intent(MainActivity.this, activity_custom.class);
        intent.putExtra("WLED_IP", wledIP);
        startActivity(intent);
    }

    private static class WLEDTask extends AsyncTask<Void, Void, Void> {
        private final String wledIP;
        private final boolean isWLEDOn;

        public WLEDTask(String wledIP, boolean isWLEDOn) {
            this.wledIP = wledIP;
            this.isWLEDOn = isWLEDOn;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://" + wledIP + "/json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                String jsonInputString = String.format("{ \"on\": %b }", isWLEDOn);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = new byte[0];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    }
                    os.write(input, 0, input.length);
                }

                int responseCode = urlConnection.getResponseCode();
                Log.d("WLEDTask", "Response Code: " + responseCode);

                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("WLEDTask", "Error: " + e.getMessage());
            }

            return null;
        }

    }


    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void restartApplication() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent pendingIntent = getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        }

        // Terminate the app
        System.exit(0);
    }

}
