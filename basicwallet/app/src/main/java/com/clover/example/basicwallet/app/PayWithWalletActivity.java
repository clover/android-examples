package com.clover.example.basicwallet.app;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class PayWithWalletActivity extends Activity {

  private static final int REQUEST_ACCOUNT = 1;

  private Account account;

  private long amount;
  private String orderId;
  private String merchantId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_pay_with_wallet);

    Intent intent = getIntent();
    if (intent.getAction().equals("clover.intent.action.PAY")) {
      amount = intent.getLongExtra("clover.intent.extra.AMOUNT", 0l);
      orderId = intent.getStringExtra("clover.intent.extra.ORDER_ID");
      merchantId = intent.getStringExtra("clover.intent.extra.MERCHANT_ID");
    }

    Button accept = (Button) findViewById(R.id.acceptButton);
    accept.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("clover.intent.extra.ORDER_ID", orderId);
        returnIntent.putExtra("clover.intent.extra.AMOUNT", amount);
        returnIntent.putExtra("clover.intent.extra.MERCHANT_ID", merchantId);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
      }
    });

    Button decline = (Button) findViewById(R.id.declineButton);
    decline.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
      }
    });
  }

}
