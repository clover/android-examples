package com.clover.example.oauthexample.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Created by zach on 6/19/14.
 */
public class WebViewActivity extends Activity {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_webview);

        // The URL that will fetch the Access Token, Merchant ID, and Employee ID
        String url = "https://clover.com/oauth/authorize" +
                "?client_id=" + Config.APP_ID +
                "&response_type=token" +
                "&redirect_uri=" + Config.APP_DOMAIN;

        // Creates the WebView
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Parses the fetched URL
                String accessTokenFragment = "#access_token=";
                String merchantIdFragment = "&merchant_id=";
                String employeeIdFragment = "&employee_id=";

                int accessTokenStart = url.indexOf(accessTokenFragment);
                int merchantIdStart = url.indexOf(merchantIdFragment);
                int employeeIdStart = url.indexOf(employeeIdFragment);
                if (accessTokenStart > -1) {
                    String accessToken = url.substring(accessTokenStart + accessTokenFragment.length(), merchantIdStart);
                    String merchantId = url.substring(merchantIdStart + merchantIdFragment.length(), employeeIdStart);
                    String employeeId = url.substring(employeeIdStart + employeeIdFragment.length(), url.length());

                    // Sends the info back to the MainActivity
                    Intent output = new Intent();
                    output.putExtra(MainActivity.ACCESS_TOKEN_KEY, accessToken);
                    output.putExtra(MainActivity.MERCHANT_ID_KEY, merchantId);
                    output.putExtra(MainActivity.EMPLOYEE_ID_KEY, employeeId);
                    setResult(RESULT_OK, output);
                    finish();
                }
            }
        });
        // Loads the WebView
        webView.loadUrl(url);
    }
}
