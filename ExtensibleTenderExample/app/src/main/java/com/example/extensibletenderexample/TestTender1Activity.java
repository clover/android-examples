package com.example.extensibletenderexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.extensibletenderexample.R;
import com.clover.sdk.v1.Intents;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created by mmaietta on 8/16/15.
 */
public class TestTender1Activity extends Activity {

    private long amount;
    private String orderId;
    private String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_tender1);

        setResult(RESULT_CANCELED);

        final long amount = getIntent().getLongExtra(Intents.EXTRA_AMOUNT, 0);
        final Currency currency = (Currency) getIntent().getSerializableExtra(Intents.EXTRA_CURRENCY);
        final String orderId = getIntent().getStringExtra(Intents.EXTRA_ORDER_ID);
        final String merchantId = getIntent().getStringExtra(Intents.EXTRA_MERCHANT_ID);

        TextView amountText = (TextView) findViewById(R.id.text_amount);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        amountText.setText(format.format(amount));

        TextView orderIdText = (TextView) findViewById(R.id.text_orderid);
        orderIdText.setText(orderId);
        TextView merchantIdText = (TextView) findViewById(R.id.text_merchantid);
        merchantIdText.setText(merchantId);

        Button approveButton = (Button) findViewById(R.id.acceptButton);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Intents.EXTRA_AMOUNT, amount);
                data.putExtra(Intents.EXTRA_CLIENT_ID, nextSampleId());
                data.putExtra(Intents.EXTRA_NOTE, "Thanks for using Test Tender!");

                setResult(RESULT_OK, data);
                finish();
            }
        });

        Button declineButton = (Button) findViewById(R.id.declineButton);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Intents.EXTRA_DECLINE_REASON, "You pressed the decline button");
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });
    }

    private String nextSampleId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
