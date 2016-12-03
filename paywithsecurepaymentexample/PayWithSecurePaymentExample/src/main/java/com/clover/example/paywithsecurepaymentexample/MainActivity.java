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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.connector.sdk.v3.PaymentV3Connector;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.TransactionSettings;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;




public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getName();

  boolean updatingSwitches = false;

  final PaymentV3Connector.PaymentServiceListener paymentConnectorListener = new PaymentV3Connector.PaymentServiceListener() {
    @Override
    public void onPreAuthResponse(PreAuthResponse preAuthResponse) {

    }

    @Override
    public void onAuthResponse(AuthResponse authResponse) {

    }

    @Override
    public void onTipAdjustAuthResponse(TipAdjustAuthResponse tipAdjustAuthResponse) {

    }

    @Override
    public void onCapturePreAuthResponse(CapturePreAuthResponse capturePreAuthResponse) {

    }

    @Override
    public void onVerifySignatureRequest(VerifySignatureRequest verifySignatureRequest) {
    }

    @Override
    public void onConfirmPaymentRequest(ConfirmPaymentRequest confirmPaymentRequest) {
    }

    @Override
    public void onSaleResponse(SaleResponse saleResponse) {
      Log.d(this.getClass().getSimpleName(), "onSaleResponse " + saleResponse);

      if (saleResponse != null && saleResponse.isNotNullPayment()) {
        Payment payment = saleResponse.getPayment();
        Toast.makeText(MainActivity.this.getApplicationContext(), MainActivity.this.getString(R.string.payment_ext_id,
            payment.getExternalPaymentId()), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MainActivity.this, SerializationTestActivity.class);
        intent.putExtra("saleResponse", saleResponse);
        startActivity(intent);
      } else {
        Toast.makeText(MainActivity.this.getApplicationContext(), MainActivity.this.getString(R.string.payment_null), Toast.LENGTH_LONG).show();
      }
    }

    @Override
    public void onManualRefundResponse(ManualRefundResponse manualRefundResponse) {

    }

    @Override
    public void onRefundPaymentResponse(RefundPaymentResponse refundPaymentResponse) {

    }

    public void onTipAdded(TipAdded tipAdded) {
      Log.d("PaymentConnectorExample", "onTipAdded " + tipAdded.getTipAmount());
    }

    @Override
    public void onVoidPaymentResponse(VoidPaymentResponse voidPaymentResponse) {

    }

    @Override
    public void onVaultCardResponse(VaultCardResponse vaultCardResponse) {

    }

    public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentsResponse) {
    }

    public void onReadCardDataResponse(ReadCardDataResponse readCardDataResponse) {
    }
  };

  private Account account;
  private PaymentConnector paymentConnector;
  private OrderConnector orderConnector;
  private InventoryConnector inventoryConnector;
  private Order order;
  private Button payButton;
  private Button payConnectorButton;
  private Boolean approveOfflinePaymentWithoutPrompt;
  private Boolean allowOfflinePayment;
  private Boolean enableCloverHandlesReceipts;
  private Long signatureThreshold;
  private DataEntryLocation signatureEntryLocation;
  private TipMode tipMode;
  private Boolean disableReceiptOptions;
  private Boolean disableDuplicateChecking;
  private Boolean autoAcceptPaymentConfirmations;
  private Boolean autoAcceptSignature;
  private static final int SECURE_PAY_REQUEST_CODE = 1;
  //This bit value is used to store selected card entry methods, which can be combined with bitwise 'or' and passed to EXTRA_CARD_ENTRY_METHODS
  private int cardEntryMethodsAllowed = Intents.CARD_ENTRY_METHOD_MAG_STRIPE | Intents.CARD_ENTRY_METHOD_ICC_CONTACT | Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS | Intents.CARD_ENTRY_METHOD_MANUAL;
  private CurrencyTextHandler amountHandler;
  private CurrencyTextHandler taxAmountHandler;
  private CurrencyTextHandler tipAmountHandler;
  private RadioGroup allowOfflineRG;
  private RadioGroup approveOfflineNoPromptRG;
  private Switch printingSwitch;
  private Spinner tipModeSpinner;
  private RadioGroup signatureEntryLocationRG;
  private Switch disableReceiptOptionsSwitch;
  private CurrencyTextHandler sigatureThresholdHandler;
  private Switch disableDuplicateCheckSwitch;
  private Switch autoAcceptPaymentConfirmationsSwitch;
  private Switch autoAcceptSignatureSwitch;


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
    payConnectorButton = (Button) findViewById(R.id.pay_connector_button);

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
        startSecurePayment(false);
      }
    });

    payConnectorButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePayment(true);
      }
    });

    tipAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.tip_amount_edit_text));
    sigatureThresholdHandler = new CurrencyTextHandler((EditText)findViewById(R.id.signatureThreshold));
    amountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.amount_edit_text));
    taxAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.tax_amount_edit_text));
    allowOfflineRG = (RadioGroup) findViewById(R.id.AcceptOfflinePaymentRG);
    approveOfflineNoPromptRG = (RadioGroup) findViewById(R.id.ApproveOfflineWithoutPromptRG);
    tipModeSpinner = ((Spinner) findViewById(R.id.TipModeSpinner));
    disableReceiptOptionsSwitch = ((Switch) findViewById(R.id.DisableReceiptOptionsSwitch));
    disableDuplicateCheckSwitch = ((Switch) findViewById(R.id.DisableDuplicateCheckSwitch));
    signatureEntryLocationRG = ((RadioGroup) findViewById(R.id.SigEntryLocationRG));
    printingSwitch = ((Switch) findViewById(R.id.PrintingSwitch));
    autoAcceptPaymentConfirmationsSwitch = ((Switch) findViewById(R.id.AutoAcceptPaymentConfirmationsSwitch));
    autoAcceptSignatureSwitch = ((Switch) findViewById(R.id.AutoAcceptSignatureSwitch));

    ArrayList<String> values = new ArrayList();

    int i = 0;
    for (TipMode tipMode: TipMode.values()) {
      values.add(i, tipMode.toString());
      i++;
    }

    ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(),
        R.layout.spinner_item, values);
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    tipModeSpinner.setAdapter(adapter);
    tipModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tipMode = getSelectedTipMode(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        tipMode = null;
      }
    });

    RadioGroup.OnCheckedChangeListener radioGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(!updatingSwitches) {
          if (group == allowOfflineRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            switch (checkedRadioButtonId) {
              case R.id.acceptOfflineDefault :  { allowOfflinePayment = null; break; }
              case R.id.acceptOfflineFalse : { allowOfflinePayment = false; break; }
              case R.id.acceptOfflineTrue : { allowOfflinePayment = true; break; }
            }
          } else if (group == approveOfflineNoPromptRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            switch (checkedRadioButtonId) {
              case R.id.approveOfflineWithoutPromptDefault:  { approveOfflinePaymentWithoutPrompt = null; break; }
              case R.id.approveOfflineWithoutPromptFalse: { approveOfflinePaymentWithoutPrompt = false; break; }
              case R.id.approveOfflineWithoutPromptTrue: { approveOfflinePaymentWithoutPrompt = true; break; }
            }
          } else if (group == signatureEntryLocationRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            switch (checkedRadioButtonId) {
              case R.id.sigEntryLocationNone:  { signatureEntryLocation = DataEntryLocation.NONE; break; }
              case R.id.sigEntryLocationOnScreen: { signatureEntryLocation = DataEntryLocation.ON_SCREEN; break; }
              case R.id.sigEntryLocationOnPaper: { signatureEntryLocation = DataEntryLocation.ON_PAPER; break; }
            }
          }
        }
      }
    };

    allowOfflineRG.setOnCheckedChangeListener(radioGroupChangeListener);
    approveOfflineNoPromptRG.setOnCheckedChangeListener(radioGroupChangeListener);
    signatureEntryLocationRG.setOnCheckedChangeListener(radioGroupChangeListener);
    disableReceiptOptionsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!updatingSwitches) {
          disableReceiptOptions = isChecked;
        }
      }
    });

    disableDuplicateCheckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!updatingSwitches) {
          disableDuplicateChecking = isChecked;
        }
      }
    });

    autoAcceptPaymentConfirmationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!updatingSwitches) {
          autoAcceptPaymentConfirmations = isChecked;
        }
      }
    });

    autoAcceptSignatureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!updatingSwitches) {
          autoAcceptSignature = isChecked;
        }
      }
    });

    printingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!updatingSwitches) {
          enableCloverHandlesReceipts = isChecked;
        }
      }
    });
  }

  private TipMode getSelectedTipMode(int position) {
    String tipModeString = tipModeSpinner.getItemAtPosition(position).toString();
    return getTipModeFromString(tipModeString);
  }

  private TipMode getTipModeFromString(String tipModeString) {
    for (TipMode tipMode: TipMode.values()) {
      if(tipMode.toString().equals(tipModeString)) {
        return tipMode;
      }
    }
    return null;
  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();

    tipAmountHandler.editText.removeTextChangedListener(tipAmountHandler);
    tipAmountHandler = null;

    sigatureThresholdHandler.editText.removeTextChangedListener(sigatureThresholdHandler);
    sigatureThresholdHandler = null;

    amountHandler.editText.removeTextChangedListener(amountHandler);
    amountHandler = null;

    taxAmountHandler.editText.removeTextChangedListener(taxAmountHandler);
    taxAmountHandler = null;
  }

  // Establishes a connection with the connectors
  private void connect() {
    disconnect();
    if (account != null) {
      orderConnector = new OrderConnector(this, account, null);
      orderConnector.connect();
      inventoryConnector = new InventoryConnector(this, account, null);
      inventoryConnector.connect();

      this.paymentConnector = new PaymentConnector(MainActivity.this, account,
          new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector connector) {
              Log.d(this.getClass().getSimpleName(), "onServiceConnected " + connector);
            }
            @Override
            public void onServiceDisconnected(ServiceConnector connector) {
              Log.d(this.getClass().getSimpleName(), "onServiceDisconnected " + connector);
            }
          }
      );
      this.paymentConnector.connect();

      this.paymentConnector.addPaymentServiceListener(this.paymentConnectorListener);
    }
  }

  // Disconnects from the connectors
  private void disconnect() {
    if (this.paymentConnector != null) {
      this.paymentConnector = null;
    }
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
  private void startSecurePayment(boolean useConnector) {
    SaleRequest saleRequest = new SaleRequest();

    Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
    try {
      //EXTRA_AMOUNT is required for secure payment
      Long amount = amountHandler.getValue();
      if (amount != null) {
        intent.putExtra(Intents.EXTRA_AMOUNT, amount);
        saleRequest.setAmount(amount);
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.amount_required), Toast.LENGTH_LONG).show();
        return;
      }

      String orderId = getStringFromEditText(R.id.order_id_edit_text);
      if (orderId != null) {
        intent.putExtra(Intents.EXTRA_ORDER_ID, orderId);
        //If no order id were passed to EXTRA_ORDER_ID a new empty order would be generated for the payment
      }

      CheckBox advancedCheckBox = (CheckBox) findViewById(R.id.show_advanced_check_box);
      if (advancedCheckBox.isChecked()) {
        TransactionSettings transactionSettings = new TransactionSettings();
        boolean restartTxn = getBooleanFromCheckbox(R.id.restart_tx_when_failed_check_box);
        //for booleans, only need to set it if it does not match the default
        if (!restartTxn) {
          transactionSettings.setDisableRestartTransactionOnFailure(true);
          saleRequest.setDisableRestartTransactionOnFail(true);
        }

        if (enableCloverHandlesReceipts != null && !enableCloverHandlesReceipts) {
          transactionSettings.setCloverShouldHandleReceipts(enableCloverHandlesReceipts);
          saleRequest.setCloverShouldHandleReceipts(enableCloverHandlesReceipts);
        }

        boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
        if (disableCashBack) {
          transactionSettings.setDisableCashBack(true);
          saleRequest.setDisableCashback(true);
        }

        if (allowOfflinePayment != null) {
          transactionSettings.setAllowOfflinePayment(allowOfflinePayment);
          saleRequest.setAllowOfflinePayment(allowOfflinePayment);
        }

        if (autoAcceptPaymentConfirmations != null) {
          transactionSettings.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
          // TODO: this is not there...?
          // saleRequest.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
        }

        if (autoAcceptSignature != null) {
          transactionSettings.setAutoAcceptSignature(autoAcceptSignature);
          // TODO: this is not there...?
          // saleRequest.setAutoAcceptSignature(autoAcceptSignature);
        }

        if (approveOfflinePaymentWithoutPrompt != null) {
          transactionSettings.setApproveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt);
          saleRequest.setApproveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt);
        }

        if (tipMode != null) {
          transactionSettings.setTipMode(tipMode);
          // TODO: this is not there...?
          // saleRequest.setTipMode(tipMode);
        }

        if (signatureEntryLocation != null) {
          transactionSettings.setSignatureEntryLocation(signatureEntryLocation);
          saleRequest.setSignatureEntryLocation(signatureEntryLocation);
        }

        signatureThreshold = sigatureThresholdHandler.getValue();
        if (signatureThreshold != null) {
          transactionSettings.setSignatureThreshold(signatureThreshold);
          saleRequest.setSignatureThreshold(signatureThreshold);
        }

        if (disableDuplicateChecking != null && disableDuplicateChecking) {
          transactionSettings.setDisableDuplicateCheck(true);
          saleRequest.setDisableDuplicateChecking(true);
        }

        if (disableReceiptOptions != null && disableReceiptOptions) {
          transactionSettings.setDisableReceiptSelection(true);
          saleRequest.setDisableReceiptSelection(true);
        }

        //Allow only selected card entry methods
        transactionSettings.setCardEntryMethods(cardEntryMethodsAllowed);
        saleRequest.setCardEntryMethods(cardEntryMethodsAllowed);

        //  Add transaction settings to the Intent
        intent.putExtra(Intents.EXTRA_TRANSACTION_SETTINGS, transactionSettings);

        boolean cardNotPresent = getBooleanFromCheckbox(R.id.card_not_present_check_box);
        if (cardNotPresent) {
          intent.putExtra(Intents.EXTRA_CARD_NOT_PRESENT, true);
          // TODO: ???
        }

        String transactionNumber = getStringFromEditText(R.id.transaction_no_edit_text);
        if (transactionNumber != null) {
          intent.putExtra(Intents.EXTRA_TRANSACTION_NO, transactionNumber);
          // TODO: ???
        }

        String voiceAuth = getStringFromEditText(R.id.voice_auth_code_edit_text);
        if (voiceAuth != null) {
          intent.putExtra(Intents.EXTRA_VOICE_AUTH_CODE, voiceAuth);
          // TODO: ???
        }

        String postalCode = getStringFromEditText(R.id.postal_code_edit_text);
        if (postalCode != null) {
          intent.putExtra(Intents.EXTRA_AVS_POSTAL_CODE, postalCode);
          // TODO: ???
        }

        String externalPaymentId = getStringFromEditText(R.id.external_txn_id_edit_text);
        if (externalPaymentId != null) {
          intent.putExtra(Intents.EXTRA_EXTERNAL_PAYMENT_ID, externalPaymentId);
          saleRequest.setExternalId(externalPaymentId);
        }

        Long tipAmount = tipAmountHandler.getValue();
        if (tipAmount != null) {
          intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
          saleRequest.setTipAmount(tipAmount);
        }

        Long taxAmount = taxAmountHandler.getValue();
        if (taxAmount != null) {
          intent.putExtra(Intents.EXTRA_TAX_AMOUNT, taxAmount);
          saleRequest.setTaxAmount(taxAmount);
        }
      }

      if(useConnector) {
        try {
          saleRequest.validate();
          if(this.paymentConnector != null) {
            if (this.paymentConnector.isConnected()) {
              this.paymentConnector.getService().sale(saleRequest);
            } else {
              Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
              this.paymentConnector.connect();
            }
          }
        } catch (RemoteException e) {
          Log.e(this.getClass().getSimpleName(), " sale", e);
        }
      } else {
        dumpIntent(intent);
        startActivityForResult(intent, SECURE_PAY_REQUEST_CODE);
      }
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
