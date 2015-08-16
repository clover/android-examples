package com.clover.example.extensibletenderexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.payments.Payment;

import java.math.BigInteger;
import java.security.SecureRandom;

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

        Intent intent = getIntent();
        amount = intent.getLongExtra(Intents.EXTRA_AMOUNT, 0l);
        orderId = intent.getStringExtra(Intents.EXTRA_ORDER_ID);
        employeeId = intent.getStringExtra(Intents.EXTRA_EMPLOYEE_ID);

        Button accept = (Button) findViewById(R.id.acceptButton);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = createPaymentSampleIntent();
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

    private Intent createPaymentSampleIntent() {

        String paymentId = nextSampleId();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrder(new Reference().setId(orderId));
        payment.setEmployee(new Reference().setId(employeeId));
        payment.setAmount(amount);

        Tender tender = getIntent().getParcelableExtra("com.clover.tender.extra.TENDER");
        payment.setTender(tender);

        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_PAYMENT_ID, payment.getId());
        data.putExtra(Intents.EXTRA_PAYMENT, payment);

        return data;
    }

    private String nextSampleId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
