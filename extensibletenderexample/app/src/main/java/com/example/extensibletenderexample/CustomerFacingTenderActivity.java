package com.example.extensibletenderexample;

import android.os.Bundle;
import android.os.Parcelable;

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
}
