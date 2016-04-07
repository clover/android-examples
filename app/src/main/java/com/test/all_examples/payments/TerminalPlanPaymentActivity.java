package com.test.all_examples.payments;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.payments.Payment;
import com.test.all_examples.R;


public class TerminalPlanPaymentActivity extends Activity {

  private static final int SECURE_PAY_REQUEST_CODE = 1;
  EditText paymentAmountEditText;
  private Account account;
  private Button payButton;
  //This bit value is used to store selected card entry methods, which can be combined with bitwise 'or' and passed to EXTRA_CARD_ENTRY_METHODS
  private int cardEntryMethodsAllowed = Intents.CARD_ENTRY_METHOD_MAG_STRIPE | Intents.CARD_ENTRY_METHOD_ICC_CONTACT | Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS | Intents.CARD_ENTRY_METHOD_MANUAL;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_terminal_plan_payment);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Retrieve the Clover account
    if (account == null) {
      account = CloverAccount.getAccount(this);

      // If an account can't be acquired, exit the app
      if (account == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }


    CheckBox magStripeCheckBox = (CheckBox) findViewById(R.id.mag_stripe_check_box);
    CheckBox chipCardCheckBox = (CheckBox) findViewById(R.id.chip_card_check_box);
    CheckBox nfcCheckBox = (CheckBox) findViewById(R.id.nfc_check_box);
    CheckBox manualEntryCheckBox = (CheckBox) findViewById(R.id.manual_entry_check_box);

    //These methods toggle the bitvalue storing which card entry methods are allowed
    magStripeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_MAG_STRIPE;
      }
    });
    chipCardCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_ICC_CONTACT;
      }
    });
    nfcCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
      }
    });
    manualEntryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_MANUAL;
      }
    });

    paymentAmountEditText = (EditText) findViewById(R.id.payment_amount_edit_text);

    payButton = (Button) findViewById(R.id.pay_button);
    payButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePaymentIntent();
      }
    });

  }


  // Start intent to launch Clover's secure payment activity
  //NOTE: ACTION_SECURE_PAY requires that your app has "clover.permission.ACTION_PAY" in it's AndroidManifest.xml file
  private void startSecurePaymentIntent() {
    Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
    Long amount = Long.parseLong(paymentAmountEditText.getText().toString(), 10);

    //EXTRA_AMOUNT is required for secure payment
    intent.putExtra(Intents.EXTRA_AMOUNT, amount);

    //Allow only selected card entry methods
    intent.putExtra(Intents.EXTRA_CARD_ENTRY_METHODS, cardEntryMethodsAllowed);

    //Because no order id is passed to EXTRA_ORDER_ID a new empty order will be generated for the payment
    startActivityForResult(intent, SECURE_PAY_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SECURE_PAY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        //Once the secure payment activity completes the result and its extras can be worked with
        Payment payment = data.getParcelableExtra(Intents.EXTRA_PAYMENT);
        String amountString = String.format("%.2f", ((Double) (0.01 * payment.getAmount())));
        Toast.makeText(getApplicationContext(), getString(R.string.payment_successful, amountString), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
      }
    }
  }
}
