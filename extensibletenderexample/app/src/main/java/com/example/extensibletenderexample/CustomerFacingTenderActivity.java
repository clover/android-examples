package com.example.extensibletenderexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.payments.ServiceChargeAmount;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Created by mmaietta on 8/16/15.
 */
public class CustomerFacingTenderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_tender1);

        setResult(RESULT_CANCELED);

        // Customer Facing UI
        setSystemUiVisibility();

        /**
         * @see Intents.ACTION_CUSTOMER_TENDER
         */
        final long amount = getIntent().getLongExtra(Intents.EXTRA_AMOUNT, 0);
        final Currency currency = (Currency) getIntent().getSerializableExtra(Intents.EXTRA_CURRENCY);
        final long taxAmount = getIntent().getLongExtra(Intents.EXTRA_TAX_AMOUNT, 0);
        final ArrayList<Parcelable> taxableAmounts = getIntent().getParcelableArrayListExtra(Intents.EXTRA_TAXABLE_AMOUNTS);
        final ServiceChargeAmount serviceCharge = getIntent().getParcelableExtra(Intents.EXTRA_SERVICE_CHARGE_AMOUNT);

        final String orderId = getIntent().getStringExtra(Intents.EXTRA_ORDER_ID);
        final String employeeId = getIntent().getStringExtra(Intents.EXTRA_EMPLOYEE_ID);
        final String merchantId = getIntent().getStringExtra(Intents.EXTRA_MERCHANT_ID);

        final Tender tender = getIntent().getParcelableExtra(Intents.EXTRA_TENDER);

        setupViews(amount, currency, orderId, merchantId);
    }

    private void setupViews(final long amount, Currency currency, String orderId, String merchantId) {
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

    public void setSystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
