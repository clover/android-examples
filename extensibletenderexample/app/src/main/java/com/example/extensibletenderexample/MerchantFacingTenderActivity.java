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
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.payments.ServiceChargeAmount;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Created by mmaietta on 9/3/15.
 */
public class MerchantFacingTenderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_tender1);

        setResult(RESULT_CANCELED);

        /**
         * @see Intents.ACTION_MERCHANT_TENDER
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

        // Merchant Facing specific fields
        final Order order = getIntent().getParcelableExtra("clover.intent.extra.ORDER");
        final String note = getIntent().getStringExtra(Intents.EXTRA_NOTE);

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
                // Optional fields
                data.putExtra(Intents.EXTRA_CLIENT_ID, Utils.nextRandomId());
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
}