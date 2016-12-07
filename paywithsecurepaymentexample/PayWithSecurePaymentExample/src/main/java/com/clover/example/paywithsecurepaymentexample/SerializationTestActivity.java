package com.clover.example.paywithsecurepaymentexample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class SerializationTestActivity extends Activity {

  private Object data;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_serialization_test);
    data = getIntent().getParcelableExtra("data");
  }

  @Override
  public void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    Toast.makeText(this.getApplicationContext(), this.getString(R.string.data_returned,
        data), Toast.LENGTH_LONG).show();
  }
}
