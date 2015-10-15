package com.example.extensibletenderexample;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.payments.ServiceChargeAmount;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Created by mmaietta on 8/16/15.
 */
public class CustomerFacingTenderActivity extends TenderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Necessary for Customer Facing user experiences
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

        // Customer Facing specific fields
        final long tipAmount = getIntent().getLongExtra(Intents.EXTRA_TIP_AMOUNT, 0);

        setupViews(amount, currency, taxAmount, taxableAmounts, serviceCharge, orderId, employeeId, merchantId, tender, tipAmount, null, null);
    }

    public void setSystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
