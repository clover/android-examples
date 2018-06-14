package com.example.clover.cloudconnecttest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

  private WebView mWebview;
  private TextView textView;
  private String webUrl;
  ServerSocket serverSocket;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    textView = (TextView) findViewById(R.id.text_view);
    mWebview  = (WebView) findViewById(R.id.webview);

    mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

    final Activity activity = this;

    mWebview.setWebViewClient(new WebViewClient() {
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
      }

//      @Override
//      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//        webUrl=view.getUrl();
//        Log.d("SocketServer", "webview url: " + webUrl);
//        return true;
//      }
    });

    mWebview.loadUrl("https://dev1.dev.clover.com/oauth/authorize?response_type=token&client_id=HBK8YZG9EQNJG");
//    Thread socketServerThread = new Thread(new SocketServerThread());
//    socketServerThread.start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private class SocketServerThread extends Thread {

    static final String TAG = "SocketServerThread";
    static final int SocketServerPORT = 3000;

    @Override
    public void run() {
      try {
        serverSocket = new ServerSocket(SocketServerPORT);
        MainActivity.this.runOnUiThread(new Runnable() {

          @Override
          public void run() {
            Log.d(TAG, "I'm waiting here: "+ serverSocket.getLocalPort());
          }
        });

        while (true) {
          Socket socket = serverSocket.accept();
          Log.d(TAG, "local: "+socket.getLocalAddress());
          BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          String line = null;
          while((line = in.readLine()) != null){
            Log.d(TAG, "message Received: " + line);
          }


          MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
              String url = mWebview.getUrl();
              Log.d(TAG, "url: " + url);
//              mWebview.setVisibility(View.GONE);
//              textView.setVisibility(View.VISIBLE);
            }
          });


        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

}
