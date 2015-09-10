package com.example.extensibletenderexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.clover.sdk.v1.Intents;

import java.util.Currency;

/**
 * Created by mmaietta on 9/9/15.
 */
public class TenderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_tender1);

        setResult(RESULT_CANCELED);
    }

    public void setupViews(final long amount, Currency currency, String orderId, String merchantId) {
        TextView amountText = (TextView) findViewById(R.id.text_amount);
        amountText.setText(Utils.longToAmountString(currency, amount));

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
                data.putExtra(Intents.EXTRA_CLIENT_ID, Utils.nextRandomId());
                data.putExtra(Intents.EXTRA_NOTE, "Transaction Id: " + Utils.nextRandomId());

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
}
