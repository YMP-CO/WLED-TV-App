package com.ymp.wled.tv;

import static com.ymp.wled.tv.MainActivity.PREFS_NAME;
import static com.ymp.wled.tv.MainActivity.WLED_IP_KEY;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.itsaky.colorpicker.ColorPickerDialog;


public class activity_custom_2 extends AppCompatActivity {
    WebView webView;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        webView = (WebView) findViewById(R.id.webView);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        webView.addJavascriptInterface(new JsInterface(), "Android");
        mContext = this;
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().getJavaScriptCanOpenWindowsAutomatically();
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/Colors.htm");   // now it will not fail here


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            Resources resources = getResources();
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle(resources.getString(R.string.Error))
                        .setMessage(resources.getString(R.string.Error_IP_text))
                        .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                        .setOnDismissListener((DialogInterface dialog) -> result.confirm())
                        .create()
                        .show();
                return true;
            }

            @Override

            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                final EditText input = new EditText(view.getContext());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(defaultValue);
                new AlertDialog.Builder(view.getContext())
                        .setTitle(resources.getString(R.string.Enter_IP))

                        .setView(input)
                        .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm(input.getText().toString()))

                        .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                        .create()
                        .show();
                return true;
            }
        });
    }

    public class JsInterface {
        @JavascriptInterface

        public void openColorPicker() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ColorPickerDialog dialog = new ColorPickerDialog(activity_custom_2.this, "#f44336");

                    dialog.withAlpha(false);
                    dialog.setTitle("🎨 Выберите цвет:");
                    SeekBar seekBarRed = dialog.findViewById(com.itsaky.colorpicker.R.id.colorpickerview_redSeek);


                        dialog.setColorPickerCallback(new ColorPickerDialog.ColorPickerCallback() {
                            @Override
                            public void onColorPicked(int color, String hexColorCode) {
                                // Передаем значение цвета обратно в WebView
                                webView.loadUrl("javascript:updateColor('" + hexColorCode + "')");
                            }
                        });
                        dialog.show();
                    }
                });
            }
        }
    }

