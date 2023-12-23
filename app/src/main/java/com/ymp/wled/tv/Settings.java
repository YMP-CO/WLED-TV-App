package com.ymp.wled.tv;

import static com.ymp.wled.tv.MainActivity.PREFS_NAME;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.internal.ContextUtils;

import org.json.JSONArray;
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
import java.util.ArrayList;

public class Settings extends AppCompatActivity {



WebView webViewBrightness;
    WebView webViewSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Выполняем запрос в фоновом потоке
        Resources resources = getResources();

        // Получите ссылку на ListView
        ListView listView = findViewById(R.id.listView);

        // Создайте адаптер для привязки массива к ListView
        String[] settingsItems = { resources.getString(R.string.Brightness), resources.getString(R.string.Speed_effects), resources.getString(R.string.Effect_intensity),resources.getString(R.string.about), resources.getString(R.string.Delete_all_data)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, settingsItems);

        // Привяжите адаптер к ListView
        listView.setAdapter(adapter);

        // Установите слушатель щелчка на элемент списка
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Обработайте щелчок на элементе списка
                handleItemClick(position);
            }
        });
    }




    private void openWebViewActivity(String url) {
        Intent intent = new Intent(this, BrightnessSpeedCustomColor.class);
            intent.putExtra(BrightnessSpeedCustomColor.EXTRA_URL, url);
        startActivity(intent);
    }
    private void handleItemClick(int position) {
        Resources resources = getResources();
        // В зависимости от позиции выполните нужное действие
        switch (position) {
            case 0:
                openWebViewActivity("file:///android_asset/Brightness.htm");
                break;
            case 1:
                openWebViewActivity("file:///android_asset/SpeedEffect.htm");
                break;
            case 2:
                openWebViewActivity("file:///android_asset/SizeEffect.htm");
                break;
            case 3:

                showInfoAlertDialog();
                break;

            case 4:
                new AlertDialog.Builder(this)
                        .setTitle(resources.getString(R.string.Warning))
                        .setMessage(R.string.Text_delete_data)

                        .setPositiveButton(resources.getString(R.string.YES),
                                new DialogInterface.OnClickListener() {
                                    @TargetApi(11)
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Очищаем данные приложения
                                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.clear();
                                        editor.apply();
clearAppData();
clearApplicationData();
                                        // Перезапускаем приложение
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton(resources.getString(R.string.NO), new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        }).show();

                break;
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
    private void showInfoAlertDialog() {
        // Создаем макет для диалога
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_info, null);


        // Находим элементы в макете
        TextView infoTextView = view.findViewById(R.id.infoTextView);

        // Устанавливаем текст
        String infoText = "<big><b>Version:</b></big><br/>" +
                "1.0<br/>" +
                "<big><b>App Developer:</b></big><br/>" +
                "YMP Yuri<br/>" +
                "<big><b>Projects used:</big></b><br/>" +
                "<b>WLED by AirCookie and WLED community</b><br/>" +
                "(https://github.com/Aircoookie/WLED)<br/>" +
                "<b>MaterialColorPicker by Itsaky</b><br/>" +
                "(https://github.com/itsaky/MaterialColorPicker)";

        infoTextView.setText(Html.fromHtml(infoText));

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
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        }

        // Terminate the app
        System.exit(0);
    }


}
