package com.clover.example.paywithsecurepaymentexample;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.payments.Payment;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getName();

  private Account account;
  private OrderConnector orderConnector;
  private InventoryConnector inventoryConnector;
  private Order order;
  private Button payButton;
  private static final int SECURE_PAY_REQUEST_CODE = 1;
  //This bit value is used to store selected card entry methods, which can be combined with bitwise 'or' and passed to EXTRA_CARD_ENTRY_METHODS
  private int cardEntryMethodsAllowed = Intents.CARD_ENTRY_METHOD_MAG_STRIPE | Intents.CARD_ENTRY_METHOD_ICC_CONTACT | Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS | Intents.CARD_ENTRY_METHOD_MANUAL;
  private CurrencyTextHandler amountHandler;
  private CurrencyTextHandler taxAmountHandler;
  private CurrencyTextHandler serviceAmountHandler;
  private CurrencyTextHandler tipAmountHandler;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
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

    // Create and Connect
    connect();

    payButton = (Button) findViewById(R.id.pay_button);

    CheckBox magStripeCheckBox = (CheckBox) findViewById(R.id.mag_stripe_check_box);
    CheckBox chipCardCheckBox = (CheckBox) findViewById(R.id.chip_card_check_box);
    CheckBox nfcCheckBox = (CheckBox) findViewById(R.id.nfc_check_box);
    CheckBox manualEntryCheckBox = (CheckBox) findViewById(R.id.manual_entry_check_box);
    CheckBox showAdvancedCheckbox = (CheckBox) findViewById(R.id.show_advanced_check_box);

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
    showAdvancedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        View advancedOptions = findViewById(R.id.advanced_options);
        if (isChecked) {
          advancedOptions.setVisibility(View.VISIBLE);
        } else {
          advancedOptions.setVisibility(View.GONE);
        }
      }
    });


    Button createButton = (Button)findViewById(R.id.create_order_button);
    createButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // create order
        new OrderAsyncTask().execute();
      }
    });


    payButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePaymentIntent();
      }
    });

    tipAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.tip_amount_edit_text));
    amountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.amount_edit_text));
    taxAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.tax_amount_edit_text));
    serviceAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.service_charge_amount_edit_text));

  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();

    tipAmountHandler.editText.removeTextChangedListener(tipAmountHandler);
    tipAmountHandler = null;

    amountHandler.editText.removeTextChangedListener(amountHandler);
    amountHandler = null;

    taxAmountHandler.editText.removeTextChangedListener(taxAmountHandler);
    taxAmountHandler = null;

    serviceAmountHandler.editText.removeTextChangedListener(serviceAmountHandler);
    serviceAmountHandler = null;
  }

  // Establishes a connection with the connectors
  private void connect() {
    disconnect();
    if (account != null) {
      orderConnector = new OrderConnector(this, account, null);
      orderConnector.connect();
      inventoryConnector = new InventoryConnector(this, account, null);
      inventoryConnector.connect();
    }
  }

  // Disconnects from the connectors
  private void disconnect() {
    if (orderConnector != null) {
      orderConnector.disconnect();
      orderConnector = null;
    }
    if (inventoryConnector != null) {
      inventoryConnector.disconnect();
      inventoryConnector = null;
    }
  }

  // Creates a new order w/ the first inventory item
  private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {

    @Override
    protected final Order doInBackground(Void... params) {
      Order mOrder;
      List<Item> merchantItems;
      Item mItem;
      try {
        // Create a new order
        mOrder = orderConnector.createOrder(new Order());
        // Grab the items from the merchant's inventory
        merchantItems = inventoryConnector.getItems();
        // If there are no item's in the merchant's inventory, then call a toast and return null
        if (merchantItems.isEmpty()) {
          Toast.makeText(getApplicationContext(), getString(R.string.empty_inventory), Toast.LENGTH_SHORT).show();
          finish();
          return null;
        }
        // Taking the first item from the inventory
        mItem = merchantItems.get(0);
        // Add this item to the order, must add using its PriceType
        if (mItem.getPriceType() == PriceType.FIXED) {
          orderConnector.addFixedPriceLineItem(mOrder.getId(), mItem.getId(), null, null);
        } else if (mItem.getPriceType() == PriceType.PER_UNIT) {
          orderConnector.addPerUnitLineItem(mOrder.getId(), mItem.getId(), 1, null, null);
        } else { // The item must be of a VARIABLE PriceType
          orderConnector.addVariablePriceLineItem(mOrder.getId(), mItem.getId(), 5, null, null);
        }
        // Update local representation of the order
        mOrder = orderConnector.getOrder(mOrder.getId());

        return mOrder;
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (ClientException e) {
        e.printStackTrace();
      } catch (ServiceException e) {
        e.printStackTrace();
      } catch (BindingException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected final void onPostExecute(Order order) {
      // Enables the pay buttons if the order is valid
      if (!isFinishing()) {
        MainActivity.this.order = order;
        EditText orderIdText = (EditText)findViewById(R.id.order_id_edit_text);
        orderIdText.setText(order.getId());

        EditText amountText = (EditText)findViewById(R.id.amount_edit_text);
        amountText.setText(order.getTotal() + "");
      }
    }
  }

  // Start intent to launch Clover's secure payment activity
  //NOTE: ACTION_SECURE_PAY requires that your app has "clover.permission.ACTION_PAY" in it's AndroidManifest.xml file
  private void startSecurePaymentIntent() {
    Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
    try {
      //EXTRA_AMOUNT is required for secure payment
      Long amount = amountHandler.getValue();
      if (amount != null) {
        intent.putExtra(Intents.EXTRA_AMOUNT, amount);
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.amount_required), Toast.LENGTH_LONG).show();
        return;
      }

      String orderId = getStringFromEditText(R.id.order_id_edit_text);
      if (orderId != null) {
        intent.putExtra(Intents.EXTRA_ORDER_ID, orderId);
        //If no order id were passed to EXTRA_ORDER_ID a new empty order would be generated for the payment
      }

      //Allow only selected card entry methods
      intent.putExtra(Intents.EXTRA_CARD_ENTRY_METHODS, cardEntryMethodsAllowed);

      CheckBox advancedCheckBox = (CheckBox) findViewById(R.id.show_advanced_check_box);
      if (advancedCheckBox.isChecked()) {
        boolean restartTxn = getBooleanFromCheckbox(R.id.restart_tx_when_failed_check_box);
        //for booelans, only need to set it if it does not match the default
        if (!restartTxn) {
          intent.putExtra(Intents.EXTRA_DISABLE_RESTART_TRANSACTION_WHEN_FAILED, true);
        }

        boolean remotePrint = getBooleanFromCheckbox(R.id.remote_print_check_box);
        if (remotePrint) {
          intent.putExtra(Intents.EXTRA_REMOTE_PRINT, true);
        }

        boolean cardNotPresent = getBooleanFromCheckbox(R.id.card_not_present_check_box);
        if (cardNotPresent) {
          intent.putExtra(Intents.EXTRA_CARD_NOT_PRESENT, true);
        }

        boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
        if (disableCashBack) {
          intent.putExtra(Intents.EXTRA_DISABLE_CASHBACK, true);
        }

        String transactionNumber = getStringFromEditText(R.id.transaction_no_edit_text);
        if (transactionNumber != null) {
          intent.putExtra(Intents.EXTRA_TRANSACTION_NO, transactionNumber);
        }

        String voiceAuth = getStringFromEditText(R.id.voice_auth_code_edit_text);
        if (voiceAuth != null) {
          intent.putExtra(Intents.EXTRA_VOICE_AUTH_CODE, voiceAuth);
        }

        String postalCode = getStringFromEditText(R.id.postal_code_edit_text);
        if (postalCode != null) {
          intent.putExtra(Intents.EXTRA_AVS_POSTAL_CODE, postalCode);
        }

        String externalPaymentId = getStringFromEditText(R.id.external_txn_id_edit_text);
        if (externalPaymentId != null) {
          intent.putExtra(Intents.EXTRA_EXTERNAL_PAYMENT_ID, externalPaymentId);
        }

        Long tipAmount = tipAmountHandler.getValue();
        if (tipAmount != null) {
          intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
        }

        Long taxAmount = taxAmountHandler.getValue();
        if (taxAmount != null) {
          intent.putExtra(Intents.EXTRA_TAX_AMOUNT, taxAmount);
        }

        Long svcChargeAmount = serviceAmountHandler.getValue();
        if (svcChargeAmount != null) {
          intent.putExtra(Intents.EXTRA_SERVICE_CHARGE_AMOUNT, svcChargeAmount);
        }
      }
      dumpIntent(intent);
      startActivityForResult(intent, SECURE_PAY_REQUEST_CODE);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SECURE_PAY_REQUEST_CODE){
      if (resultCode == RESULT_OK){
        //Once the secure payment activity completes the result and its extras can be worked with
        Payment payment = data.getParcelableExtra(Intents.EXTRA_PAYMENT);
        Toast.makeText(getApplicationContext(), getString(R.string.payment_successful, payment.getOrder().getId()), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
      }
    }
  }


  protected boolean getBooleanFromCheckbox(int id) {
    CheckBox cb = (CheckBox)findViewById(id);
    return cb.isChecked();
  }

  protected String getStringFromEditText(int id) {
    EditText edit = (EditText)findViewById(id);
    String text = edit.getText().toString();
    if (text != null && text.trim().length() > 0) {
      return text.trim();
    }
    return null;
  }


  /**
   * Simplistic method for handling money fields
   */
  private static class CurrencyTextHandler implements TextWatcher {

    private String current;
    private EditText editText;

    public CurrencyTextHandler(EditText editText) {
      this.editText = editText;
      current = editText.getText() != null ? editText.getText().toString() : null;
      editText.addTextChangedListener(this);
    }

    public Long getValue() throws ParseException {
      if (current != null) {
        String cleanString = current.replaceAll("[^\\d]", "");
        if (cleanString.trim().length() > 0) {
          return new Long(cleanString);
        }
      }
      return null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
      if (!s.toString().equals(current)) {
        editText.removeTextChangedListener(this);

        String cleanString = s.toString().replaceAll("[^\\d]", "");

        double parsed = Double.parseDouble(cleanString);
        String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));

        current = formatted;
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.addTextChangedListener(this);
      }

    }
  }

  /**
   * Debugs the intent we created
   * @param i - intent to debug contents of
   */
  public static void dumpIntent(Intent i){

    Bundle bundle = i.getExtras();
    if (bundle != null) {
      Set<String> keys = bundle.keySet();
      Iterator<String> it = keys.iterator();
      Log.d(TAG,"Dumping Intent start");
      while (it.hasNext()) {
        String key = it.next();
        Log.e(TAG,"[" + key + "=" + bundle.get(key)+"]");
      }
      Log.e(TAG,"Dumping Intent end");
    }
  }


}
