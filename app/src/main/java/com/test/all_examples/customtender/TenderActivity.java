package com.test.all_examples.customtender;

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
import com.test.all_examples.R;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Created by mmaietta on 9/9/15.
 */
public class TenderActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_tender);
  }

  public void setupViews(final long totalAmount, Currency currency, long taxAmount, ArrayList<Parcelable> taxableAmounts,
                         ServiceChargeAmount serviceCharge, final String orderId, String employeeId, String merchantId,
                         final Tender tender, long tipAmount, final Order order, String note) {
    final TextView amountText = (TextView) findViewById(R.id.text_amount);
    setTextOrNull(amountText, Utils.longToAmountString(currency, totalAmount));

    final TextView orderIdText = (TextView) findViewById(R.id.text_orderid);
    setTextOrNull(orderIdText, orderId);
    final TextView merchantIdText = (TextView) findViewById(R.id.text_merchantId);
    setTextOrNull(merchantIdText, merchantId);

    final TextView employeeIdText = (TextView) findViewById(R.id.text_employeeId);
    setTextOrNull(employeeIdText, employeeId);

    final TextView currencyText = (TextView) findViewById(R.id.text_currency);
    setTextOrNull(currencyText, currency);

    final TextView noteText = (TextView) findViewById(R.id.text_note);
    setTextOrNull(noteText, note);

    final TextView taxText = (TextView) findViewById(R.id.text_tax);
    setTextOrNull(taxText, String.valueOf(taxAmount));

    final TextView tenderText = (TextView) findViewById(R.id.text_tenderType);
    setTextOrNull(tenderText, tender);

    final TextView tipText = (TextView) findViewById(R.id.text_tip);
    setTextOrNull(tipText, String.valueOf(tipAmount));

    final TextView serviceChargeText = (TextView) findViewById(R.id.text_serviceCharge);
    setTextOrNull(serviceChargeText, serviceCharge);

    final TextView taxableAmountsText = (TextView) findViewById(R.id.text_taxableAmounts);
    setTextOrNull(taxableAmountsText, taxableAmounts);

    final TextView orderText = (TextView) findViewById(R.id.text_order);
    setTextOrNull(orderText, order);

    Button approveButton = (Button) findViewById(R.id.acceptButton);
    approveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finishPayment(totalAmount);
      }
    });

    Button declineButton = (Button) findViewById(R.id.declineButton);
    declineButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cancelPayment();
      }
    });
  }

  private void cancelPayment() {
    Intent data = new Intent();
    data.putExtra(Intents.EXTRA_DECLINE_REASON, "You pressed the decline button");

    setResult(RESULT_CANCELED, data);
    finish();
  }

  private void finishPayment(long totalAmount) {
    Intent data = new Intent();
    data.putExtra(Intents.EXTRA_AMOUNT, totalAmount);
    data.putExtra(Intents.EXTRA_CLIENT_ID, Utils.nextRandomId());
    data.putExtra(Intents.EXTRA_NOTE, "Auth Id: " + Utils.nextRandomId());

    setResult(RESULT_OK, data);

    finish();
  }

  public void setTextOrNull(TextView textView, Object object) {
    String text = "null";
    if (object != null) {
      text = object.toString();
    }
    textView.setText(text);
  }
}

