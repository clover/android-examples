package com.test.all_examples.pushnotifications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.test.all_examples.R;


public class NotificationReceiverActivity extends Activity {
  public static final String EXTRA_PAYLOAD = "payload";

  private TextView resultText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notification_receiver);

    resultText = (TextView) findViewById(R.id.result_text);
    resultText.setVisibility(View.GONE);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    resultText.setVisibility(View.VISIBLE);
    resultText.setText(getString(R.string.result, intent.getStringExtra(EXTRA_PAYLOAD)));
  }
}
