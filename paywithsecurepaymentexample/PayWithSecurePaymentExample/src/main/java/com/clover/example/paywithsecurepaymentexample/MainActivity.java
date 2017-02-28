package com.clover.example.paywithsecurepaymentexample;

import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.connector.sdk.v3.PaymentV3Connector;
import com.clover.sdk.GenericParcelable;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.TransactionSettings;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PaymentResponse;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.TransactionRequest;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// import com.clover.sdk.v3.payments.PaymentResponse;

public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getName();

  boolean updatingSwitches = false;

  public Payment getLastPayment() {
    return lastPayment;
  }

  private PaymentConnector paymentServiceConnector;

  private Payment lastPayment = null;
  private Button connectorButton_sale;

  private Account account;

  private Button payButton;
  private Button connectorButton_preauth;
  private Button connectorButton_capturepreauth;
  private Button connectorButton_auth;
  private Button connectorButton_full_refund;
  private Button connectorButton_void;

  private OrderConnector orderConnector;
  private InventoryConnector inventoryConnector;
  private Order order;

  final PaymentV3Connector.PaymentServiceListener paymentConnectorListener = new PaymentV3Connector.PaymentServiceListener() {

    private void displayoutput(GenericParcelable response) {
      final Intent intent = new Intent(MainActivity.this, SerializationTestActivity.class);
      intent.putExtra("response", response);
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity(intent);
        }
      }, 1);
    }

    public void onPaymentResponse(PaymentResponse response) {
      if (response != null && response.isNotNullPayment()) {
        Payment payment = response.getPayment();
        Toast.makeText(MainActivity.this.getApplicationContext(), MainActivity.this.getString(R.string.payment_ext_id,
            payment.getExternalPaymentId()), Toast.LENGTH_LONG).show();
        setLastPayment(payment);
      } else {
        Toast.makeText(MainActivity.this.getApplicationContext(), MainActivity.this.getString(R.string.payment_null), Toast.LENGTH_LONG).show();
        setLastPayment(null);
      }
      displayoutput(response);
    }

    @Override
    public void onPreAuthResponse(PreAuthResponse response) {
      Log.d(this.getClass().getSimpleName(), "onPreAuthResponse " + response);
      onPaymentResponse(response);
    }

    @Override
    public void onAuthResponse(AuthResponse response) {
      Log.d(this.getClass().getSimpleName(), "onAuthResponse " + response);
      onPaymentResponse(response);
    }

    @Override
    public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
      Log.d(this.getClass().getSimpleName(), "onTipAdjustAuthResponse " + response);
      displayoutput(response);
    }

    @Override
    public void onCapturePreAuthResponse(CapturePreAuthResponse response) {
      Log.d(this.getClass().getSimpleName(), "onCapturePreAuthResponse " + response);
      displayoutput(response);
    }

    @Override
    public void onVerifySignatureRequest(VerifySignatureRequest request) {
      Log.d(this.getClass().getSimpleName(), "onVerifySignatureRequest " + request);
      displayoutput(request);
    }

    @Override
    public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
      Log.d(this.getClass().getSimpleName(), "onConfirmPaymentRequest " + request);
      displayoutput(request);
    }

    @Override
    public void onSaleResponse(SaleResponse response) {
      Log.d(this.getClass().getSimpleName(), "onSaleResponse " + response);
      onPaymentResponse(response);
    }

    @Override
    public void onManualRefundResponse(ManualRefundResponse response) {
      Log.d(this.getClass().getSimpleName(), "onManualRefundResponse " + response);
      displayoutput(response);
    }

    @Override
    public void onRefundPaymentResponse(RefundPaymentResponse response) {
      Log.d(this.getClass().getSimpleName(), "onRefundPaymentResponse " + response);
      displayoutput(response);
    }

    public void onTipAdded(TipAdded response) {
      Log.d(this.getClass().getSimpleName(), "onTipAdded " + response);
      displayoutput(response);
    }

    @Override
    public void onVoidPaymentResponse(VoidPaymentResponse response) {
      Log.d(this.getClass().getSimpleName(), "onVoidPaymentResponse " + response);
      displayoutput(response);
    }

    @Override
    public void onVaultCardResponse(VaultCardResponse response) {
      Log.d(this.getClass().getSimpleName(), "onVaultCardResponse " + response);
      displayoutput(response);
    }

    public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
      Log.d(this.getClass().getSimpleName(), "onRetrievePendingPaymentsResponse " + response);
      displayoutput(response);
    }

    public void onReadCardDataResponse(ReadCardDataResponse response) {
      Log.d(this.getClass().getSimpleName(), "onReadCardDataResponse " + response);
      displayoutput(response);
    }
  };

  public void setLastPayment(Payment lastPayment) {
    this.lastPayment = lastPayment;
    connectorButton_full_refund.setEnabled(lastPayment != null);
    connectorButton_void.setEnabled(lastPayment != null);
  }
  private AsyncTask waitingTask;

  private Boolean approveOfflinePaymentWithoutPrompt;
  private Boolean allowOfflinePayment;
  private Boolean disableCloverHandlesReceipts;
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
    // see https://developer.android.com/guide/components/bound-services.html#Additional_Notes
    // If you want your activity to receive responses even while it is stopped in the background,
    // then you can bind during onCreate() and unbind during onDestroy().
//    connectToPaymentService();
  }

  private void connectToPaymentService() {
    if (this.paymentServiceConnector == null) {
      this.paymentServiceConnector = new PaymentConnector(MainActivity.this, account,
          new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector connector) {
              Log.d(this.getClass().getSimpleName(), "onServiceConnected " + connector);
              MainActivity.this.paymentServiceConnector.addPaymentServiceListener(MainActivity.this.paymentConnectorListener);

              AsyncTask tempWaitingTask = waitingTask;
              waitingTask = null;

              if (tempWaitingTask != null) {
                tempWaitingTask.execute();
              }
            }

            @Override
            public void onServiceDisconnected(ServiceConnector connector) {
              Log.d(this.getClass().getSimpleName(), "onServiceDisconnected " + connector);
            }
          }
      );
      this.paymentServiceConnector.connect();
    } else if (!this.paymentServiceConnector.isConnected()) {
      this.paymentServiceConnector.connect();
    }
  }

  @Override
  protected void onResume() {
    Log.i(this.getClass().getSimpleName(), "MRH onResume");
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
    connectToPaymentService();

    payButton = (Button) findViewById(R.id.pay_button);
    connectorButton_sale = (Button) findViewById(R.id.connector_button_sale);
    connectorButton_auth = (Button) findViewById(R.id.connector_button_auth);
    connectorButton_preauth = (Button) findViewById(R.id.connector_button_preauth);

    connectorButton_full_refund = (Button) findViewById(R.id.connector_button_full_refund);
    connectorButton_full_refund.setEnabled(lastPayment != null);
    connectorButton_void = (Button) findViewById(R.id.connector_button_void);
    connectorButton_void.setEnabled(lastPayment != null);
    connectorButton_capturepreauth = (Button) findViewById(R.id.connector_button_capturepreauth);
    connectorButton_capturepreauth.setEnabled(lastPayment != null);

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
        startSecurePayment();
      }
    });

    connectorButton_sale.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_sale();
      }
    });

    connectorButton_auth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_auth();
      }
    });

    connectorButton_preauth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_preauth();
      }
    });

    connectorButton_full_refund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_refundPayment(getLastPayment());
      }
    });

    connectorButton_void.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_voidPayment(getLastPayment());
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

    ArrayList<String> values = new ArrayList<String>();

    int i = 0;
    for (TipMode tipMode: TipMode.values()) {
      values.add(i, tipMode.toString());
      i++;
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
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
    tipModeSpinner.setSelection(4);

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
          disableCloverHandlesReceipts = isChecked;
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
    Log.i(this.getClass().getSimpleName(), "MRH onPause");
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

  @Override
  protected void onDestroy() {
    if (this.paymentServiceConnector != null) {
      // see https://developer.android.com/guide/components/bound-services.html#Additional_Notes
      // If you want your activity to receive responses even while it is stopped in the background,
      // then you can bind during onCreate() and unbind during onDestroy().
      this.paymentServiceConnector.removePaymentServiceListener(this.paymentConnectorListener);
      this.paymentServiceConnector.disconnect();
      this.paymentServiceConnector = null;
    }
    super.onDestroy();
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

  private void startPaymentConnector_refundPayment(Payment payment) {
    if (payment != null) {
      try {
        final RefundPaymentRequest request = new RefundPaymentRequest();
        request.setAmount(payment.getAmount());
        request.setPaymentId(payment.getId());
        request.setFullRefund(true);
        request.setOrderId(payment.getOrder().getId());

        request.validate();
        Log.i(this.getClass().getSimpleName(), request.toString());
        if (this.paymentServiceConnector != null) {
          if (this.paymentServiceConnector.isConnected()) {
            this.paymentServiceConnector.getService().refundPayment(request);
          } else {
            Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
            this.paymentServiceConnector.connect();
            waitingTask = new AsyncTask() {
              @Override
              protected Object doInBackground(Object[] params) {
                try {
                  MainActivity.this.paymentServiceConnector.getService().refundPayment(request);
                } catch (RemoteException e) {
                  Log.e(this.getClass().getSimpleName(), " refund", e);
                }
                return null;
              }
            };
          }
        }
      } catch (IllegalArgumentException e) {
        Log.e(this.getClass().getSimpleName(), " refund", e);
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      } catch (RemoteException e) {
        Log.e(this.getClass().getSimpleName(), " refund", e);
      }
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.payment_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_voidPayment(Payment payment) {
    if (payment != null) {
      try {
        final VoidPaymentRequest request = new VoidPaymentRequest();
        request.setPaymentId(payment.getId());
        request.setOrderId(payment.getOrder().getId());
        request.setVoidReason(VoidReason.USER_CANCEL.toString());

        request.validate();
        Log.i(this.getClass().getSimpleName(), request.toString());
        if (this.paymentServiceConnector != null) {
          if (this.paymentServiceConnector.isConnected()) {
            this.paymentServiceConnector.getService().voidPayment(request);
          } else {
            Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
            this.paymentServiceConnector.connect();
            waitingTask = new AsyncTask() {
              @Override
              protected Object doInBackground(Object[] params) {
                try {
                  MainActivity.this.paymentServiceConnector.getService().voidPayment(request);
                } catch (RemoteException e) {
                  Log.e(this.getClass().getSimpleName(), " void", e);
                }
                return null;
              }
            };
          }
        }
      } catch (IllegalArgumentException e) {
        Log.e(this.getClass().getSimpleName(), " void", e);
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      } catch (RemoteException e) {
        Log.e(this.getClass().getSimpleName(), " void", e);
      }
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.payment_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_sale() {
    final SaleRequest request = new SaleRequest();
    setUpSaleRequest(request);

    try {
      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      if (this.paymentServiceConnector != null) {
        if (this.paymentServiceConnector.isConnected()) {
          this.paymentServiceConnector.getService().sale(request);
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
          this.paymentServiceConnector.connect();
          waitingTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
              try {
                MainActivity.this.paymentServiceConnector.getService().sale(request);
              } catch (RemoteException e) {
                Log.e(this.getClass().getSimpleName(), " sale", e);
              }
              return null;
            }
          };
        }
      }
    } catch (IllegalArgumentException e) {
      Log.e(this.getClass().getSimpleName(), " sale", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    } catch (RemoteException e) {
      Log.e(this.getClass().getSimpleName(), " sale", e);
    }
  }

  private void startPaymentConnector_auth() {
    final AuthRequest request = new AuthRequest();
    setUpAuthRequest(request);

    try {
      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      if (this.paymentServiceConnector != null) {
        if (this.paymentServiceConnector.isConnected()) {
          this.paymentServiceConnector.getService().auth(request);
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
          this.paymentServiceConnector.connect();
          waitingTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
              try {
                MainActivity.this.paymentServiceConnector.getService().auth(request);
              } catch (RemoteException e) {
                Log.e(this.getClass().getSimpleName(), " auth", e);
              }
              return null;
            }
          };
        }
      }
    } catch (IllegalArgumentException e) {
      Log.e(this.getClass().getSimpleName(), " auth", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    } catch (RemoteException e) {
      Log.e(this.getClass().getSimpleName(), " auth", e);
    }
  }

  private void startPaymentConnector_preauth() {
    final PreAuthRequest request = new PreAuthRequest();
    try {
      setUpTransactionRequest(request);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }

    try {
      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      if (this.paymentServiceConnector != null) {
        if (this.paymentServiceConnector.isConnected()) {
          this.paymentServiceConnector.getService().preAuth(request);
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
          this.paymentServiceConnector.connect();
          waitingTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
              try {
                MainActivity.this.paymentServiceConnector.getService().preAuth(request);
              } catch (RemoteException e) {
                Log.e(this.getClass().getSimpleName(), " preAuth", e);
              }
              return null;
            }
          };
        }
      }
    } catch (IllegalArgumentException e) {
      Log.e(this.getClass().getSimpleName(), " preAuth", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    } catch (RemoteException e) {
      Log.e(this.getClass().getSimpleName(), " preAuth", e);
    }
  }

  private void startPaymentConnector_capturepreauth(Payment payment) {
    final CapturePreAuthRequest request = new CapturePreAuthRequest();
    try {
      request.setPaymentId(payment.getId());
      request.setAmount(payment.getAmount());
      request.setTipAmount(payment.getTipAmount());

      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      if (this.paymentServiceConnector != null) {
        if (this.paymentServiceConnector.isConnected()) {
          this.paymentServiceConnector.getService().capturePreAuth(request);
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.connector_not_connected), Toast.LENGTH_LONG).show();
          this.paymentServiceConnector.connect();
          waitingTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
              try {
                MainActivity.this.paymentServiceConnector.getService().capturePreAuth(request);
              } catch (RemoteException e) {
                Log.e(this.getClass().getSimpleName(), " capturePreAuth", e);
              }
              return null;
            }
          };
        }
      }
    } catch (IllegalArgumentException e) {
      Log.e(this.getClass().getSimpleName(), " capturePreAuth", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    } catch (RemoteException e) {
      Log.e(this.getClass().getSimpleName(), " capturePreAuth", e);
    }
  }

  // Start intent to launch Clover's secure payment activity
  //NOTE: ACTION_SECURE_PAY requires that your app has "clover.permission.ACTION_PAY" in it's AndroidManifest.xml file
  private void startSecurePayment() {

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
      }

      CheckBox advancedCheckBox = (CheckBox) findViewById(R.id.show_advanced_check_box);
      if (advancedCheckBox.isChecked()) {
        TransactionSettings transactionSettings = new TransactionSettings();
        boolean restartTxn = getBooleanFromCheckbox(R.id.restart_tx_when_failed_check_box);
        //for booleans, only need to set it if it does not match the default
        if (!restartTxn) {
          transactionSettings.setDisableRestartTransactionOnFailure(true);
        }

        disableCloverHandlesReceipts = printingSwitch.isChecked();
        if (disableCloverHandlesReceipts != null && disableCloverHandlesReceipts) {
          transactionSettings.setCloverShouldHandleReceipts(false);
        }

        boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
        if (disableCashBack) {
          transactionSettings.setDisableCashBack(true);
        }

        if (allowOfflinePayment != null && allowOfflinePayment) {
          transactionSettings.setAllowOfflinePayment(allowOfflinePayment);
        }

        autoAcceptPaymentConfirmations = autoAcceptPaymentConfirmationsSwitch.isChecked();
        if (autoAcceptPaymentConfirmations != null && autoAcceptPaymentConfirmations) {
          transactionSettings.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
        }

        autoAcceptSignature = autoAcceptSignatureSwitch.isChecked();
        if (autoAcceptSignature != null && autoAcceptSignature) {
          transactionSettings.setAutoAcceptSignature(autoAcceptSignature);
        }

        if (approveOfflinePaymentWithoutPrompt != null && approveOfflinePaymentWithoutPrompt) {
          transactionSettings.setApproveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt);
        }

        if (tipMode != null) {
          transactionSettings.setTipMode(tipMode);
        }

        if (signatureEntryLocation != null) {
          transactionSettings.setSignatureEntryLocation(signatureEntryLocation);
        }

        signatureThreshold = sigatureThresholdHandler.getValue();
        if (signatureThreshold != null) {
          transactionSettings.setSignatureThreshold(signatureThreshold);
        }

        disableDuplicateChecking = disableDuplicateCheckSwitch.isChecked();
        if (disableDuplicateChecking != null && disableDuplicateChecking) {
          transactionSettings.setDisableDuplicateCheck(disableDuplicateChecking);
        }

        disableReceiptOptions = disableReceiptOptionsSwitch.isChecked();
        if (disableReceiptOptions != null && disableReceiptOptions) {
          transactionSettings.setDisableReceiptSelection(disableReceiptOptions);
        }

        //Allow only selected card entry methods
        transactionSettings.setCardEntryMethods(cardEntryMethodsAllowed);

        //  Add transaction settings to the Intent
        intent.putExtra(Intents.EXTRA_TRANSACTION_SETTINGS, transactionSettings);

        boolean cardNotPresent = getBooleanFromCheckbox(R.id.card_not_present_check_box);
        if (cardNotPresent) {
          intent.putExtra(Intents.EXTRA_CARD_NOT_PRESENT, true);
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
        }

        Long tipAmount = tipAmountHandler.getValue();
        if (tipAmount != null) {
          intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
        }

        Long taxAmount = taxAmountHandler.getValue();
        if (taxAmount != null) {
          intent.putExtra(Intents.EXTRA_TAX_AMOUNT, taxAmount);
        }
      }
      dumpIntent(intent);
      startActivityForResult(intent, SECURE_PAY_REQUEST_CODE);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }
  }

  private void setUpSaleRequest(SaleRequest request) {
    try {
      boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
      if (disableCashBack) {
        request.setDisableCashback(true);
      }

      if (allowOfflinePayment != null) {
        request.setAllowOfflinePayment(allowOfflinePayment);
      }
      if (approveOfflinePaymentWithoutPrompt != null) {
        request.setApproveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt);
      }

      if (tipMode != null) {
        request.setTipMode(tipMode);
      }
      Long tipAmount = tipAmountHandler.getValue();
      if (tipAmount != null) {
        request.setTipAmount(tipAmount);
      }

      Long taxAmount = taxAmountHandler.getValue();
      if (taxAmount != null) {
        request.setTaxAmount(taxAmount);
      }
      setUpTransactionRequest(request);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }
  }

  private void setUpAuthRequest(AuthRequest request) {
    try {
      boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
      if (disableCashBack) {
        request.setDisableCashback(true);
      }

      if (allowOfflinePayment != null) {
        request.setAllowOfflinePayment(allowOfflinePayment);
      }
      if (approveOfflinePaymentWithoutPrompt != null) {
        request.setApproveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt);
      }

      Long taxAmount = taxAmountHandler.getValue();
      if (taxAmount != null) {
        request.setTaxAmount(taxAmount);
      }
      setUpTransactionRequest(request);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }
  }

  private void setUpTransactionRequest(TransactionRequest request) throws ParseException {
    //EXTRA_AMOUNT is required for secure payment
    Long amount = amountHandler.getValue();
    if (amount != null) {
      request.setAmount(amount);
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.amount_required), Toast.LENGTH_LONG).show();
      return;
    }

    String orderId = getStringFromEditText(R.id.order_id_edit_text);
    if (orderId != null) {
      request.setOrderId(orderId);
    }

    CheckBox advancedCheckBox = (CheckBox) findViewById(R.id.show_advanced_check_box);
    if (advancedCheckBox.isChecked()) {
      boolean restartTxn = getBooleanFromCheckbox(R.id.restart_tx_when_failed_check_box);
      //for booleans, only need to set it if it does not match the default
      if (!restartTxn) {
        request.setDisableRestartTransactionOnFail(true);
      }

      disableCloverHandlesReceipts = printingSwitch.isChecked();
      if (disableCloverHandlesReceipts != null) {
        request.setDisablePrinting(disableCloverHandlesReceipts);
      }

      autoAcceptPaymentConfirmations = autoAcceptPaymentConfirmationsSwitch.isChecked();
      if (autoAcceptPaymentConfirmations != null) {
        request.setAutoAcceptPaymentConfirmations(autoAcceptPaymentConfirmations);
      }

      autoAcceptSignature = autoAcceptSignatureSwitch.isChecked();
      if (autoAcceptSignature != null) {
        request.setAutoAcceptSignature(autoAcceptSignature);
      }

      if (signatureEntryLocation != null) {
        request.setSignatureEntryLocation(signatureEntryLocation);
      }

      signatureThreshold = sigatureThresholdHandler.getValue();
      if (signatureThreshold != null) {
        request.setSignatureThreshold(signatureThreshold);
      }
      disableDuplicateChecking = disableDuplicateCheckSwitch.isChecked();
      if (disableDuplicateChecking != null) {
        request.setDisableDuplicateChecking(disableDuplicateChecking);
      }

      disableReceiptOptions = disableReceiptOptionsSwitch.isChecked();
      if (disableReceiptOptions != null) {
        request.setDisableReceiptSelection(disableReceiptOptions);
      }

      //Allow only selected card entry methods
      request.setCardEntryMethods(cardEntryMethodsAllowed);

      boolean cardNotPresent = getBooleanFromCheckbox(R.id.card_not_present_check_box);
      if (cardNotPresent) {
        request.setCardNotPresent(true);
      }

      String transactionNumber = getStringFromEditText(R.id.transaction_no_edit_text);
      if (transactionNumber != null) {
        // TODO: ???
      }

      String voiceAuth = getStringFromEditText(R.id.voice_auth_code_edit_text);
      if (voiceAuth != null) {
        // TODO: ???

      }

      String postalCode = getStringFromEditText(R.id.postal_code_edit_text);
      if (postalCode != null) {
        // TODO: ???
      }

      String externalPaymentId = getStringFromEditText(R.id.external_txn_id_edit_text);
      if (externalPaymentId != null) {
        request.setExternalId(externalPaymentId);
      }
    }

    if (request.getExternalId() == null) {
      String externalPaymentId = "DEFAULTED" + (Math.random() * 1000);
      request.setExternalId(externalPaymentId);
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
        if (formatted.equals("$0.00")) {
          formatted = null;
        }
        current = formatted;
        editText.setText(formatted);
        if (formatted != null) {
          editText.setSelection(formatted.length());
        }
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
