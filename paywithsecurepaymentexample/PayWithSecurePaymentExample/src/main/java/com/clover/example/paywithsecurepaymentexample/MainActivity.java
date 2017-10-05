package com.clover.example.paywithsecurepaymentexample;

import android.content.Context;
import android.os.*;

import com.clover.common.analytics.ALog;
import com.clover.common.cardreader.CardReader;
import com.clover.common.cardreader.ReadCardResult;
import com.clover.common.http.Callback;
import com.clover.common.http.HttpRequestTask;
import com.clover.common.http.ServerPing;
import com.clover.common.util.ScreenReceiver;
import com.clover.common2.CommonActivity;
import com.clover.common2.appservices.ServiceConnectorManager;
import com.clover.common2.clover.CloverConnector;
import com.clover.common2.clover.MerchantGateway;
import com.clover.common2.payments.PayIntent;
import com.clover.common2.securepay.SecurePay;
import com.clover.commonpayments.CapturePreAuthService;
import com.clover.commonpayments.RefundPaymentService;
import com.clover.commonpayments.ResultIntentService;
import com.clover.common2.clover.Clover;
import com.clover.commonpayments.RetreivePendingPaymentsService;
import com.clover.commonpayments.VoidPaymentService;
import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.connector.sdk.v3.CardEntryMethods;
import com.clover.http.DeviceHttpClient;
import com.clover.sdk.GenericParcelable;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.pay.PaymentRequest;
import com.clover.sdk.v3.pay.PaymentRequestCardDetails;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardTransactionState;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Result;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.TransactionSettings;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
import com.clover.sdk.v3.remotepay.CloseoutResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PaymentResponse;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataRequest;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthRequest;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.TransactionRequest;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
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
import java.util.concurrent.ExecutionException;

public class MainActivity extends ServiceActivity {

  private static final String TAG = MainActivity.class.getName();

  boolean updatingSwitches = false;

  public Payment getLastPayment() {
    return lastPayment;
  }

  private PaymentConnector paymentConnector;

  /**
   * @param orderId    The ID of the updated order.
   * @param selfChange True if the update was triggered by the listening application running on the same device.
   */
  @Override
  public void onOrderUpdated(String orderId, boolean selfChange) {

  }

  private enum PaymentType {SALE, AUTH, PREAUTH, CREDIT}

  private Button saleButton;
  private Button authButton;
  private Button preauthButton;
  private Button tipadjustButton;
  private Button capturePreauthButton;
  private Button voidButton;
  private Button refundButton;
  private Button rppButton;
  private Button vaultCardButton;
  private Button readCardDataButton;
  private Button manualRefundButton;

  private Button connectorButton_sale;
  private Button connectorButton_preauth;
  private Button connectorButton_capturepreauth;
  private Button connectorButton_auth;
  private Button connectorButton_full_refund;
  private Button connectorButton_tip_adjust;
  private Button connectorButton_void;
  private Button connectorButton_rpp;
  private Button connectorButton_vault_card;
  private Button connectorButton_read_card_data;
  private Button connectorButton_manual_refund;
  private Button connectorButton_retrieve_payment;
  private Button connectorButton_closeout;

  private Order lastOrder = null;
  private boolean isStation;
  private boolean allowOpenTabs = false;
  private Payment lastPayment = null;
  public boolean isSale = false;
  public boolean isAuth = false;
  public boolean isPreAuth = false;
  private VaultedCard vaultedCard = null;

  final IPaymentConnectorListener paymentConnectorListener = new IPaymentConnectorListener() {

    private void displayoutput(GenericParcelable response) {
      final Intent intent = new Intent(MainActivity.this, SerializationTestActivity.class);
      intent.putExtra("response", response);
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity(intent);
        }
      }, 1000);
    }

    public void onPaymentResponse(PaymentResponse response) {
      if (response != null && response.isNotNullPayment()) {
        Payment payment = response.getPayment();
        Toast.makeText(MainActivity.this.getApplicationContext(), MainActivity.this.getString(R.string.payment_ext_id,
            payment.getExternalPaymentId()), Toast.LENGTH_LONG).show();
        setLastPayment(payment);
        String orderId = payment.hasOrder() ? payment.getOrder().getId() : null;
        if (orderId != null) {
          new SetLastOrderAsyncTask(orderId).execute();
        }
        EditText externalIdText = (EditText)findViewById(R.id.external_txn_id_edit_text);
        externalIdText.setText(payment.getExternalPaymentId());
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

      // Below is some super hacky code to allow the UI to reset the buttons properly
      // The modified payment doesn't get returned after the capture, so it needs to
      // be massaged to look like a normal auth (tip-adjustable)
      Payment modifiedPayment = lastPayment;
      CardTransaction ct = lastPayment.getCardTransaction();
      ct.setType(CardTransactionType.PREAUTH);
      ct.setState(CardTransactionState.PENDING);
      modifiedPayment.setCardTransaction(ct);
      modifiedPayment.setResult(Result.SUCCESS);
      modifiedPayment.setAmount(response.getAmount());
      modifiedPayment.setTipAmount(response.getTipAmount());
      setLastPayment(modifiedPayment); // This resets the payment and the button states

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
      setLastPayment(null);
    }

    public void onTipAdded(TipAdded response) {
      Log.d(this.getClass().getSimpleName(), "onTipAdded " + response);
      displayoutput(response);
    }

    @Override
    public void onVoidPaymentResponse(VoidPaymentResponse response) {
      Log.d(this.getClass().getSimpleName(), "onVoidPaymentResponse " + response);
      displayoutput(response);
      setLastPayment(null);
    }

    @Override
    public void onVaultCardResponse(VaultCardResponse response) {
      Log.d(this.getClass().getSimpleName(), "onVaultCardResponse " + response);
      setVaultedCard(response);
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

    /**
     * Called in response to a closeout being processed
     *
     * @param response
     */
    @Override
    public void onCloseoutResponse(CloseoutResponse response) {
      Log.d(this.getClass().getSimpleName(), "onCloseoutResponse " + response);
      displayoutput(response);
    }

    /**
     * Called in response to a doRetrievePayment(...) request
     *
     * @param response
     */
    @Override
    public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
      Log.d(this.getClass().getSimpleName(), "onRetrievePaymentResponse " + response);
      displayoutput(response);
    }

    /**
     * Called when the Clover device is disconnected
     * Not applicable to the PaymentConnector (Native)
     */
    @Override
    public void onDeviceDisconnected() {

    }

    /**
     * Called when the Clover device is connected, but not ready to communicate
     * Not applicable to the PaymentConnector (Native)
     */
    @Override
    public void onDeviceConnected() {

    }
  };

  public void setLastOrder(Order order) {
    lastOrder = order;
    EditText orderIdText = (EditText)findViewById(R.id.order_id_edit_text);
    if (lastOrder != null) {
      orderIdText.setText(order.getId());
    } else {
      orderIdText.setText("");
    }
  }

  public void setLastPayment(Payment lastPayment) {
    this.lastPayment = lastPayment;
    if (lastPayment != null) {
      if (lastPayment.getCardTransaction() != null && lastPayment.getCardTransaction().getType() != null) {
        if (lastPayment.getCardTransaction().getType().equals(CardTransactionType.PREAUTH)) {
          if (lastPayment.getResult().equals(Result.SUCCESS)) {
            isAuth = true;
            isSale = false;
            isPreAuth = false;
          } else {
            isPreAuth = true;
            isAuth = false;
            isSale = false;
          }
        } else {
          if (lastPayment.getCardTransaction().getType().equals(CardTransactionType.AUTH)) {
            isSale = true;
            isAuth = false;
            isPreAuth = false;
          }
        }
      } else {
        if (lastPayment.getOffline()) {
          isSale = true;
          isAuth = false;
          isPreAuth = false;
        }
      }
    } else {
      isSale = false;
      isAuth = false;
      isPreAuth = false;
    }
    setButtonStates();
  }

  public void setButtonStates() {
    tipadjustButton.setEnabled(lastPayment != null && isAuth);
    capturePreauthButton.setEnabled(!isStation && lastPayment != null && isPreAuth);
    voidButton.setEnabled(lastPayment != null && (isSale || isAuth));
    refundButton.setEnabled(lastPayment != null && (isSale || isAuth));

    connectorButton_full_refund.setEnabled(lastPayment != null && (isSale || isAuth));
    connectorButton_void.setEnabled(lastPayment != null && (isSale || isAuth));
    connectorButton_capturepreauth.setEnabled(lastPayment != null && isPreAuth);
    connectorButton_tip_adjust.setEnabled(lastPayment != null && isAuth);
  }

  public void onPaymentIntentResponse(Intent intent) {
    Payment payment = intent.getParcelableExtra(Intents.EXTRA_PAYMENT);
    if (payment != null) {
      setLastPayment(payment);
      new SetLastOrderAsyncTask(payment.getOrder().getId());
    } else {
      setLastPayment(null);
      setLastOrder(null);
    }
  }

  public void setVaultedCard(VaultCardResponse response) {
    this.vaultedCard = response.hasCard() ? response.getCard() : null;
  }

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
  private static final int MANUAL_REFUND_REQUEST_CODE = 2;
  private static final int READ_CARD_DATA_REQUEST_CODE = 3;
  private static final int VAULT_CARD_REQUEST_CODE = 4;
  //This bit value is used to store selected card entry methods, which can be combined with bitwise 'or' and passed to EXTRA_CARD_ENTRY_METHODS
  private int cardEntryMethodsAllowed = CardEntryMethods.ALL;
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Log.d("MainActivity", "SecurePayExample.MainActivity.onCreate()");
    if(savedInstanceState != null)
    {
      Object lp = savedInstanceState.getParcelable("lastPayment");
      if(lp != null)
      {
        Log.d("MainActivity", "Restoring last payment");
        setLastPayment((Payment)lp);
      }
    }
  }

  private void connectToPaymentService() {
    if (this.paymentConnector == null) {
      this.paymentConnector = new PaymentConnector(MainActivity.this, account, paymentConnectorListener);
    }
  }

  @Override
  public void onResume() {
    Log.i(this.getClass().getSimpleName(), "MRH onResume");
    super.onResume();
    if (!setupComplete) {
      Toast.makeText(this, getString(R.string.services_not_connected), Toast.LENGTH_LONG).show();
      return;
    }
    // Create and Connect
    connectToPaymentService();

    saleButton = (Button) findViewById(R.id.sale_button);
    authButton = (Button) findViewById(R.id.auth_button);
    preauthButton = (Button) findViewById(R.id.preauth_button);
    preauthButton.setEnabled(!isStation);

    tipadjustButton = (Button) findViewById(R.id.tip_adjust_button);
    capturePreauthButton = (Button) findViewById(R.id.capturepreauth_button);
    voidButton = (Button) findViewById(R.id.void_button);
    refundButton = (Button) findViewById(R.id.full_refund_button);
    rppButton = (Button) findViewById(R.id.button_rpp);
    rppButton.setEnabled(!isStation);

    vaultCardButton = (Button) findViewById(R.id.button_vault_card);
    vaultCardButton.setEnabled(!isStation);

    readCardDataButton = (Button) findViewById(R.id.button_read_card_data);
    readCardDataButton.setEnabled(!isStation);

    manualRefundButton = (Button) findViewById(R.id.button_manual_refund);
    manualRefundButton.setEnabled(!isStation);

    connectorButton_void = (Button) findViewById(R.id.connector_button_void);
    connectorButton_capturepreauth = (Button) findViewById(R.id.connector_button_capturepreauth);

    connectorButton_sale = (Button) findViewById(R.id.connector_button_sale);
    connectorButton_auth = (Button) findViewById(R.id.connector_button_auth);
    connectorButton_preauth = (Button) findViewById(R.id.connector_button_preauth);
    connectorButton_rpp = (Button) findViewById(R.id.connector_button_rpp);
    connectorButton_vault_card = (Button) findViewById(R.id.connector_button_vault_card);
    connectorButton_read_card_data = (Button) findViewById(R.id.connector_button_read_card_data);
    connectorButton_manual_refund = (Button) findViewById(R.id.connector_button_manual_refund);

    connectorButton_full_refund = (Button) findViewById(R.id.connector_button_full_refund);
    connectorButton_tip_adjust = (Button) findViewById(R.id.connector_button_tip_adjust);
    connectorButton_void = (Button) findViewById(R.id.connector_button_void);
    connectorButton_capturepreauth = (Button) findViewById(R.id.connector_button_capturepreauth);
    connectorButton_retrieve_payment = (Button) findViewById(R.id.connector_button_retrieve_payment);
    connectorButton_retrieve_payment.setEnabled(true);
    connectorButton_closeout = (Button) findViewById(R.id.connector_button_closeout);
    connectorButton_closeout.setEnabled(true);

    setButtonStates(); //initializes button enabling based on the current value of lastPayment

    CheckBox magStripeCheckBox = (CheckBox) findViewById(R.id.mag_stripe_check_box);
    CheckBox chipCardCheckBox = (CheckBox) findViewById(R.id.chip_card_check_box);
    CheckBox nfcCheckBox = (CheckBox) findViewById(R.id.nfc_check_box);
    CheckBox manualEntryCheckBox = (CheckBox) findViewById(R.id.manual_entry_check_box);
    CheckBox showAdvancedCheckbox = (CheckBox) findViewById(R.id.show_advanced_check_box);
    CheckBox allowOpenTabsCheckbox = (CheckBox) findViewById(R.id.allow_open_tabs_check_box);

    allowOpenTabsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        allowOpenTabs = isChecked;
      }
    });

    //These methods toggle the bitvalue storing which card entry methods are allowed
    magStripeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ CardEntryMethods.CARD_ENTRY_METHOD_MAG_STRIPE;
      }
    });
    chipCardCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ CardEntryMethods.CARD_ENTRY_METHOD_ICC_CONTACT;
      }
    });
    nfcCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ CardEntryMethods.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
      }
    });
    manualEntryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ CardEntryMethods.CARD_ENTRY_METHOD_MANUAL;
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

    /*
       Intent Button Actions (Payments are intent-based, other actions - not so much)
       ******************************************************************************
     */

    saleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePayment(PaymentType.SALE);
      }
    });

    authButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePayment(PaymentType.AUTH);
      }
    });

    preauthButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startSecurePayment(PaymentType.PREAUTH);
      }
    });

    tipadjustButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new OrderConnectorAsyncTask().execute();
      }
    });

    capturePreauthButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final Long amount;
        final Long tipAmount;
        try {
          amount = amountHandler.getValue();
          if (amount != null) {
            final String employeeId = employee.getId();
            final String merchantId = merchant.getId();
            tipAmount = tipAmountHandler.getValue() != null ? tipAmountHandler.getValue() : 0L;
            doCapturePreAuth(lastPayment.getId(), amount, tipAmount, merchantId, employeeId);
          } else {
            Toast.makeText(getApplicationContext(), getString(R.string.capture_preauth_failed) + " : Amount field must contain a value greater than zero.", Toast.LENGTH_SHORT).show();
          }
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
    });

    voidButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doVoid(lastPayment.getId(), lastOrder.getId(), employee.getId(), null, VoidReason.USER_CANCEL, getPackageName());
      }
    });

    refundButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doFullRefund(lastPayment.getId(), lastOrder.getId(), 0L, true);
     }
    });

    rppButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doRetrievePendingPayments();
      }
    });

    manualRefundButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MerchantGateway merchantGateway = clover.getMerchantGateway();
        if (!merchantGateway.isSupportsNakedCredit()) {
          // Does not support this.
          Toast.makeText(getApplicationContext(),  "Merchant Configuration Validation Error : In manualRefund: Support is not enabled for the payment gateway.", Toast.LENGTH_SHORT).show();
          return;
        } else {
          startSecurePayment(PaymentType.CREDIT);
        }

      }
    });

    readCardDataButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doReadCardData();
      }
    });

    vaultCardButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doVaultCard();
      }
    });

    /*
       Connector Button Actions
       ***************************************************************
     */

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
      public void onClick(View v) { startPaymentConnector_preauth(); }
    });

    connectorButton_capturepreauth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) { startPaymentConnector_capturepreauth(getLastPayment()); }
    });

    connectorButton_full_refund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_refundPayment(getLastPayment());
      }
    });

    connectorButton_tip_adjust.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_adjustTip(getLastPayment());
      }
    });

    connectorButton_void.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_voidPayment(getLastPayment());
      }
    });

    connectorButton_rpp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_retrievePendingPayments();
      }
    });

    connectorButton_manual_refund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_manualRefund();
      }
    });

    connectorButton_read_card_data.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_readCardData();
      }
    });

    connectorButton_vault_card.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_vaultCard();
      }
    });

    connectorButton_capturepreauth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPaymentConnector_capturepreauth(getLastPayment());
      }
    });

    connectorButton_retrieve_payment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String externalPaymentId = getStringFromEditText(R.id.external_txn_id_edit_text);
        if (externalPaymentId == null) {
          Toast.makeText(getApplicationContext(), getString(R.string.external_payment_id_null), Toast.LENGTH_LONG).show();
        } else {
          startPaymentConnector_retrievePayment(externalPaymentId);
        }
      }
    });

    connectorButton_closeout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String batchId = getStringFromEditText(R.id.batch_id_edit_text);
        startPaymentConnector_closeout(batchId, allowOpenTabs);
      }
    });

    amountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.amount_edit_text));
    tipAmountHandler = new CurrencyTextHandler((EditText)findViewById(R.id.tip_amount_edit_text));

    /*
     Advance Settings (Transaction_Settings)
     ***************************************
     */

    sigatureThresholdHandler = new CurrencyTextHandler((EditText)findViewById(R.id.signatureThreshold));
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

    values.add(0, "DEFAULT");
    int i = 1;
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
  public void onPause() {
    Log.i(this.getClass().getSimpleName(), "MRH onPause");
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

  @Override
  protected void onDestroy() {
    if (this.paymentConnector != null) {
      // see https://developer.android.com/guide/components/bound-services.html#Additional_Notes
      // If you want your activity to receive responses even while it is stopped in the background,
      // then you can bind during onCreate() and unbind during onDestroy().
      this.paymentConnector.removeCloverConnectorListener(this.paymentConnectorListener);
      this.paymentConnector.dispose();
      this.paymentConnector = null;
    }
    super.onDestroy();
  }

  // updates an order payment with a tip amount
  private class OrderConnectorAsyncTask extends AsyncTask<Void, Void, Order> {

    @Override
    protected final Order doInBackground(Void... params) {
      Order mOrder;

      try {
        Long tipAmount = tipAmountHandler.getValue();
        if (tipAmount != null) {
          mOrder = orderConnector.addTip(lastOrder.getId(), lastPayment.getId(), tipAmount, false);
          return mOrder;
        } else {
          return null;
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (ClientException e) {
        e.printStackTrace();
      } catch (ServiceException e) {
        e.printStackTrace();
      } catch (BindingException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected final void onPostExecute(Order order) {
      if (!isFinishing()) {
        if (order == null) {
          Toast.makeText(getApplicationContext(), "Tip Adjust Failed: Call to OrderConnector.addTip() failed.", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getApplicationContext(), "Tip adjusted successfully", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  // gets an order using the id
  private class SetLastOrderAsyncTask extends AsyncTask<Void, Void, Order> {

    private String orderId;
    public SetLastOrderAsyncTask(String orderId) {
      super();
      this.orderId = orderId;
    }

    @Override
    protected final Order doInBackground(Void... params) {
      Order mOrder;
      try {
          mOrder = orderConnector.getOrder(orderId);
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
      if (!isFinishing()) {
        if (order != null) {
          setLastOrder(order);
          Toast.makeText(getApplicationContext(), "Order found for the provided ID", Toast.LENGTH_LONG).show();
        }
      }
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
        mOrder = orderConnector.createOrder(new Order().setManualTransaction(true));
        Long amount = amountHandler.getValue();
        if (amount == null) {
          amount = new Long(100);
        }
        LineItem customLineItem = new LineItem().setName("Manual Transaction").setAlternateName("").setPrice(amount);

        customLineItem = orderConnector.addCustomLineItem(mOrder.getId(), customLineItem, false);
        List<LineItem> lineItems;
        if (mOrder.hasLineItems()) {
          lineItems = new ArrayList<LineItem>(mOrder.getLineItems());
        } else {
          lineItems = new ArrayList<LineItem>();
        }
        lineItems.add(customLineItem);
        mOrder.setLineItems(lineItems);

        // TODO: What about taxes/service charge?

        return orderConnector.getOrder(mOrder.getId());
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (ClientException e) {
        e.printStackTrace();
      } catch (ServiceException e) {
        e.printStackTrace();
      } catch (BindingException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected final void onPostExecute(Order order) {
      if (!isFinishing()) {
        setLastOrder(order);
      }
    }
  }

  // retrieves the currently logged in employee
  private class EmployeeConnectorAsyncTask extends AsyncTask<Void, Void, com.clover.sdk.v3.employees.Employee> {

    @Override
    protected final com.clover.sdk.v3.employees.Employee doInBackground(Void... params) {
      com.clover.sdk.v3.employees.Employee mEmployee;

      try {
          return employeeConnector.getEmployee();
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
    protected final void onPostExecute(com.clover.sdk.v3.employees.Employee currentEmployee) {
      if (!isFinishing()) {
        if (currentEmployee == null) {
          Toast.makeText(getApplicationContext(), "Get Employee Failed: Call to EmployeeConnector.getEmployee() failed.", Toast.LENGTH_LONG).show();
        } else {
          employee = currentEmployee;
        }
      }
    }
  }

  // retrieves the currently logged in employee
  private class CloverConnectorAsyncTask extends AsyncTask<Void, Void, Clover> {

    @Override
    protected final Clover doInBackground(Void... params) {
      try {
        return cloverConnector.getClover();
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
    protected final void onPostExecute(Clover cloverIn) {
      if (!isFinishing()) {
        if (cloverIn == null) {
          Toast.makeText(getApplicationContext(), "Get Clover configuration object Failed: Call to CloverConnector.getClover() failed.", Toast.LENGTH_LONG).show();
        } else {
          clover = cloverIn;
        }
        EditText amountText = (EditText)findViewById(R.id.amount_edit_text);
        amountText.setText(lastOrder.getTotal() + "");
        EditText externalIdText = (EditText)findViewById(R.id.external_txn_id_edit_text);
        externalIdText.setText("");
      }
    }
  }

  private void startPaymentConnector_refundPayment(Payment payment) {
    if (payment != null) {
      final RefundPaymentRequest request = new RefundPaymentRequest();
      request.setAmount(payment.getAmount());
      request.setPaymentId(payment.getId());
      request.setFullRefund(true);
      request.setOrderId(payment.getOrder().getId());

      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      paymentConnector.refundPayment(request);
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.payment_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_adjustTip(Payment payment) {
    if (payment != null) {
        final TipAdjustAuthRequest request = new TipAdjustAuthRequest();
        try{
          Long tipAmount = tipAmountHandler.getValue();
          if (tipAmount != null) {
            request.setTipAmount(tipAmount);
          } else {
            Toast.makeText(getApplicationContext(), "Tip amount must have a numeric value", Toast.LENGTH_LONG).show();
          }
        } catch (ParseException e) {
          Log.e(this.getClass().getSimpleName(), " adjust tip", e);
          Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          return;
        }
        request.setPaymentId(payment.getId());
        request.setOrderId(payment.getOrder().getId());

        request.validate();
        Log.i(this.getClass().getSimpleName(), request.toString());
        paymentConnector.tipAdjustAuth(request);
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.payment_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_voidPayment(Payment payment) {
    if (payment != null) {
      final VoidPaymentRequest request = new VoidPaymentRequest();
      request.setPaymentId(payment.getId());
      request.setOrderId(payment.getOrder().getId());
      request.setVoidReason(VoidReason.USER_CANCEL.toString());

      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      paymentConnector.voidPayment(request);
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.payment_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_retrievePendingPayments() {
    Log.i(this.getClass().getSimpleName(), "");
    paymentConnector.retrievePendingPayments();
  }

  private void startPaymentConnector_readCardData() {
    final ReadCardDataRequest request = new ReadCardDataRequest();
    request.setCardEntryMethods(cardEntryMethodsAllowed);
    request.setIsForceSwipePinEntry(false);
    Log.i(this.getClass().getSimpleName(), "");
    paymentConnector.readCardData(request);
  }

  private void startPaymentConnector_vaultCard() {
    Log.i(this.getClass().getSimpleName(), "");
    paymentConnector.vaultCard(cardEntryMethodsAllowed);
  }

  private void startPaymentConnector_manualRefund() {
    final ManualRefundRequest request = new ManualRefundRequest();
    try {
      setUpTransactionRequest(request);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }

    Log.i(this.getClass().getSimpleName(), "");
    paymentConnector.manualRefund(request);
  }

  private void startPaymentConnector_retrievePayment(String externalPaymentId) {
    if (externalPaymentId != null) {
      final RetrievePaymentRequest request = new RetrievePaymentRequest();
      request.setExternalPaymentId(externalPaymentId);
      request.validate();
      Log.i(this.getClass().getSimpleName(), request.toString());
      paymentConnector.retrievePayment(request);
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.external_payment_id_null), Toast.LENGTH_LONG).show();
    }
  }

  private void startPaymentConnector_closeout(String batchId, boolean allowOpenTabs) {
    final CloseoutRequest request = new CloseoutRequest();
    request.setBatchId(batchId);
    request.setAllowOpenTabs(allowOpenTabs);
    request.validate();
    Log.i(this.getClass().getSimpleName(), request.toString());
    paymentConnector.closeout(request);
  }

  private void startPaymentConnector_sale() {
    final SaleRequest request = new SaleRequest();
    setUpSaleRequest(request);
    try {
      request.validate();
    } catch (IllegalArgumentException e) {
      Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    Log.i(this.getClass().getSimpleName(), request.toString());
    paymentConnector.sale(request);
  }

  private void startPaymentConnector_auth() {
    final AuthRequest request = new AuthRequest();
    setUpAuthRequest(request);
    try {
      request.validate();
    } catch (IllegalArgumentException e) {
      Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    Log.i(this.getClass().getSimpleName(), request.toString());
    paymentConnector.auth(request);
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
    } catch (IllegalArgumentException e) {
      Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    Log.i(this.getClass().getSimpleName(), request.toString());
    paymentConnector.preAuth(request);
  }

  private void startPaymentConnector_capturepreauth(Payment payment) {
    final CapturePreAuthRequest request = new CapturePreAuthRequest();
    request.setPaymentId(payment.getId());
    try{
      Long amount = amountHandler.getValue();
      if (amount != null) {
        request.setAmount(amount);
      } else {
        Toast.makeText(getApplicationContext(), "Sale amount must have a numeric value", Toast.LENGTH_LONG).show();
      }
    } catch (ParseException e) {
      Log.e(this.getClass().getSimpleName(), " capture preauth", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      return;
    }

    try{
      Long tipAmount = tipAmountHandler.getValue() != null ? tipAmountHandler.getValue() : 0L;
      request.setTipAmount(tipAmount);
    } catch (ParseException e) {
      Log.e(this.getClass().getSimpleName(), " capture preauth", e);
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    try {
      request.validate();
    } catch (IllegalArgumentException e) {
      Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    Log.i(this.getClass().getSimpleName(), request.toString());
    paymentConnector.capturePreAuth(request);
  }

  // Start intent to launch Clover's secure payment activity
  //NOTE: ACTION_SECURE_PAY requires that your app has "clover.permission.ACTION_PAY" in it's AndroidManifest.xml file
  private void startSecurePayment(PaymentType paymentType) {
    int request_code = SECURE_PAY_REQUEST_CODE;

    Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
    if (vaultedCard != null && (paymentType.equals(PaymentType.AUTH) || paymentType.equals(PaymentType.SALE))) {
      VaultedCard tempVaultedCard = vaultedCard;
      intent.putExtra(Intents.EXTRA_VAULTED_CARD, tempVaultedCard);
      vaultedCard = null;
    }

    try {
      //EXTRA_AMOUNT is required for secure payment
      Long amount = amountHandler.getValue();
      if (amount != null ) {
        if (paymentType.equals(PaymentType.CREDIT)) {
          intent.putExtra(Intents.EXTRA_AMOUNT, amount * -1);
        } else {
            intent.putExtra(Intents.EXTRA_AMOUNT, amount);
        }
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.amount_required), Toast.LENGTH_LONG).show();
        return;
      }

      if (paymentType != null && paymentType.equals(PaymentType.PREAUTH)) {
        // Set the TransactionType extra to AUTH
        intent.putExtra(Intents.EXTRA_TRANSACTION_TYPE, Intents.TRANSACTION_TYPE_AUTH);
      } else {
        if (paymentType != null && paymentType.equals(PaymentType.CREDIT)) {
          // Set the TransactionType extra to CREDIT
          intent.putExtra(Intents.EXTRA_TRANSACTION_TYPE, Intents.TRANSACTION_TYPE_CREDIT);
          request_code = MANUAL_REFUND_REQUEST_CODE;
        }
      }

      String orderId = getStringFromEditText(R.id.order_id_edit_text);
      if (orderId != null) {
        intent.putExtra(Intents.EXTRA_ORDER_ID, orderId);
      } else {
        OrderAsyncTask oat = new OrderAsyncTask();
        oat.execute();
        try {
          setLastOrder(oat.get());
          if (lastOrder != null) {
            intent.putExtra(Intents.EXTRA_ORDER_ID, lastOrder.getId());
          } else {
            Toast.makeText(getApplicationContext(), getString(R.string.could_not_create_order), Toast.LENGTH_LONG).show();
            return;
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
      CheckBox advancedCheckBox = (CheckBox) findViewById(R.id.show_advanced_check_box);
      Long tipAmount = null;
      try {
        tipAmount = tipAmountHandler.getValue();
      } catch (ParseException e) {
        e.printStackTrace();
      }
      // We don't want to set the tip amount extra, unless it's a sale)
      if (paymentType.equals(PaymentType.SALE)) {
        // if advancedCheckbox is checked, we will set the tipAmount in the advanced settings code
        if (!advancedCheckBox.isChecked()) {
          if (tipAmount != null) {
            intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
          } else {
            // Setting tip to zero will ensure that the transaction is closed (non-tip-adjustable)
            intent.putExtra(Intents.EXTRA_TIP_AMOUNT, 0L);
          }
        }
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.tip_amount_ignored), Toast.LENGTH_SHORT).show();
      }

      intent.putExtra(Intents.EXTRA_CARD_ENTRY_METHODS, cardEntryMethodsAllowed);

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

        boolean forceOfflinePayment = getBooleanFromCheckbox(R.id.force_offline_payment_check_box);
        if (forceOfflinePayment) {
          transactionSettings.setForceOfflinePayment(true);
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

        transactionSettings.setTipMode(tipMode);
        switch (paymentType) {
          case SALE:
            if (tipMode != null) {
              if (tipMode.equals(TipMode.NO_TIP)) {
                if (tipAmount != null) {
                  intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
                } else {
                  intent.putExtra(Intents.EXTRA_TIP_AMOUNT, 0); //force to final sale
                }
              } else {
                if (tipMode.equals(TipMode.TIP_PROVIDED)) {
                  if (tipAmount == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.tip_amount_required), Toast.LENGTH_LONG).show();
                    return;
                  } else {
                    intent.putExtra(Intents.EXTRA_TIP_AMOUNT, tipAmount);
                  }
                } else {
                  Toast.makeText(getApplicationContext(), getString(R.string.tip_mode_invalid_for_sale), Toast.LENGTH_LONG).show();
                  return;
                }
              }
            }
            break;
          case AUTH:
            // ignore tipMode and tipAmount, as we want a null tip to make the payment tip-adjustable
            break;
          case PREAUTH:
            // ignore tipMode and tipAmount
            break;
          case CREDIT:
            // ignore tipMode and tipAmount
            break;
          default:
            Toast.makeText(getApplicationContext(), "PaymentType is not valid : ", Toast.LENGTH_LONG).show();
            return;
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

        Long taxAmount = taxAmountHandler.getValue();
        if (taxAmount != null) {
          intent.putExtra(Intents.EXTRA_TAX_AMOUNT, taxAmount);
        }
      }
      dumpIntent(intent);
      startActivityForResult(intent, request_code);
    } catch (ParseException pe) {
      Toast.makeText(getApplicationContext(), getString(R.string.invalid_amount), Toast.LENGTH_LONG).show();
    }
  }

  public void doCapturePreAuth(String paymentId, long amount, long tipAmount, String merchantId, String employeeId) {
    ResultReceiver capturePreAuthResultReceiver = new ResultReceiver(new Handler()) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultInfo) {
        if (resultCode == 0) {
          boolean success = resultInfo.getBoolean(ResultIntentService.SUCCESS_TAG, true);
          if (!success) {
            String msg = resultInfo.getString(ResultIntentService.RESULT_ERR_MSG);
            Toast.makeText(getApplicationContext(), getString(R.string.capture_preauth_failed) + " - " + msg, Toast.LENGTH_LONG).show();
            return;
          }
          String paymentId = resultInfo.getString(SecurePay.EXTRA_PAYMENT_ID);
          String orderId = resultInfo.getString(SecurePay.EXTRA_ORDER_ID);

          // Below is some super hacky code to allow the UI to reset the buttons properly
          // The modified payment doesn't get returned after the capture, so it needs to
          // be massaged to look like a normal auth (tip-adjustable)
          new SetLastOrderAsyncTask(orderId);
          Payment modifiedPayment = lastPayment;
          CardTransaction ct = lastPayment.getCardTransaction();
          ct.setType(CardTransactionType.PREAUTH);
          ct.setState(CardTransactionState.PENDING);
          modifiedPayment.setCardTransaction(ct);
          modifiedPayment.setResult(Result.SUCCESS);
          long amount = resultInfo.getLong(CapturePreAuthService.PAY_AMOUNT);
          long tipAmount = resultInfo.getLong(ResultIntentService.EXTRA_TIP_AMOUNT);
          modifiedPayment.setAmount(amount);
          modifiedPayment.setTipAmount(tipAmount);
          setLastPayment(modifiedPayment); // This resets the payment and the button states

          Toast.makeText(getApplicationContext(), getString(R.string.capture_preauth_successful, paymentId, amount, tipAmount), Toast.LENGTH_SHORT).show();
        }
      }
    };
    CapturePreAuthService.start(this, paymentId, amount, tipAmount, merchantId, employeeId, capturePreAuthResultReceiver);
  }

  public void doFullRefund(String paymentId, String orderId, final long amount , boolean fullRefund) {
    ResultReceiver refundResultReceiver = new ResultReceiver(new Handler()) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultInfo) {
        if (resultCode == 0) {
          boolean success = resultInfo.getBoolean(ResultIntentService.SUCCESS_TAG, true);
          String paymentId = resultInfo.getString(SecurePay.EXTRA_PAYMENT_ID);
          if (!success) {
            String msg = resultInfo.getString(ResultIntentService.RESULT_ERR_MSG);
            Toast.makeText(getApplicationContext(), getString(R.string.refund_failed) + " - " + msg, Toast.LENGTH_LONG).show();
            return;
          }
          setLastPayment(null);
          setLastOrder(null);
          Toast.makeText(getApplicationContext(), getString(R.string.refund_successful, paymentId, amount), Toast.LENGTH_SHORT).show();
        }
      }
    };
    RefundPaymentService.start(this, paymentId, orderId, amount, fullRefund, refundResultReceiver);
  }

  public void doVoid(String paymentId, String orderId, String employeeId , String iccContainer, VoidReason voidReason, String packageName) {
    ResultReceiver voidResultReceiver = new ResultReceiver(new Handler()) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultInfo) {
        if (resultCode == 0) {
          boolean success = resultInfo.getBoolean(ResultIntentService.SUCCESS_TAG, true);
          String paymentId = resultInfo.getString(SecurePay.EXTRA_PAYMENT_ID);
          if (!success) {
            String msg = resultInfo.getString(ResultIntentService.RESULT_ERR_MSG);
            Toast.makeText(getApplicationContext(), getString(R.string.void_failed) + " - " + msg, Toast.LENGTH_LONG).show();
            return;
          }
          setLastPayment(null);
          setLastOrder(null);
          Toast.makeText(getApplicationContext(), getString(R.string.void_successful, paymentId), Toast.LENGTH_SHORT).show();
        }
      }
    };
    VoidPaymentService.start(this, paymentId, orderId, employeeId, iccContainer, voidReason, packageName, voidResultReceiver);
  }

  private void doReadCardData() {
    PayIntent.Builder builder = new PayIntent.Builder();
    builder.transactionType(PayIntent.TransactionType.DATA);
    builder.cardEntryMethods(cardEntryMethodsAllowed);
    builder.forceSwipePinEntry(false);

    PayIntent payIntent = builder.build();

    try {
      Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
      PayIntent.Builder payIntentBuilder = new PayIntent.Builder()
          .payIntent(payIntent)
          .action(Intents.ACTION_SECURE_CARD_DATA);

      if (payIntent.cardDataMessage == null) {
        payIntentBuilder.cardDataMessage(getString(R.string.insert_or_swipe_card));
      }

      PayIntent pIntent = payIntentBuilder.build();
      pIntent.addTo(intent);

      startActivityForResult(intent, READ_CARD_DATA_REQUEST_CODE);
    } catch (Exception e) {
      Toast.makeText(getApplicationContext(), getString(R.string.read_card_data_failed), Toast.LENGTH_LONG).show();
    }
  }

  private void doRetrievePendingPayments() {
    ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultInfo) {
        if (resultCode == 0) {
          boolean success = resultInfo.getBoolean(ResultIntentService.SUCCESS_TAG, true);
          if (success) {
            ArrayList<Payment> pendingPayments = resultInfo.getParcelableArrayList(RetreivePendingPaymentsService.EXTRA_PAYMENTS);
            ArrayList<PendingPaymentEntry> pendingPaymentEntries = new ArrayList(0);
            Integer paymentCount = new Integer(0);
            if (pendingPayments != null && pendingPayments.size() > 0) {
              paymentCount = new Integer(pendingPayments.size());
              Iterator pendingPaymentsIter = pendingPayments.iterator();
              while (pendingPaymentsIter.hasNext()) {
                Payment payment = (Payment) pendingPaymentsIter.next();
                PendingPaymentEntry pendingPaymentsEntry = new PendingPaymentEntry();
                pendingPaymentsEntry.setPaymentId(payment.getId());
                pendingPaymentsEntry.setAmount(payment.getAmount().longValue());
                pendingPaymentEntries.add(pendingPaymentsEntry);
              }
            }
            Toast.makeText(getApplicationContext(), getString(R.string.retrieve_pending_payments_successful) + " - Count = " + paymentCount.toString(), Toast.LENGTH_LONG).show();
          } else {
            String msg = resultInfo.getString(ResultIntentService.RESULT_ERR_MSG);
            Toast.makeText(getApplicationContext(), getString(R.string.retrieve_pending_payments_failed) + " - " + msg, Toast.LENGTH_LONG).show();
          }
        }
      }
    };
    RetreivePendingPaymentsService.start(this, clover.getAccount(), resultReceiver);
  }

  private void doVaultCard() {
    try {
      Intent intent = new Intent();
      PayIntent payIntent = new PayIntent.Builder()
          .action(Intents.ACTION_SECURE_CARD_DATA)
          .cardEntryMethods(cardEntryMethodsAllowed)
          .transactionType(PayIntent.TransactionType.DATA)
          .cardDataMessage(getString(R.string.insert_or_swipe_card))
          .build();
      payIntent.addTo(intent);
      startActivityForResult(intent, VAULT_CARD_REQUEST_CODE);
    } catch (Exception e) {
      ALog.e(this, e, "Error capturing card data");
      Toast.makeText(getApplicationContext(), "Error adding card: Vault Card failed to start the required activity", Toast.LENGTH_LONG).show();
    }
  }

  private void setUpSaleRequest(SaleRequest request) {
    try {
      boolean disableCashBack = getBooleanFromCheckbox(R.id.disable_cash_back_check_box);
      if (disableCashBack) {
        request.setDisableCashback(true);
      }

      boolean forceOfflinePayment = getBooleanFromCheckbox(R.id.force_offline_payment_check_box);
      if (forceOfflinePayment) {
        request.setForceOfflinePayment(true);
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

      boolean forceOfflinePayment = getBooleanFromCheckbox(R.id.force_offline_payment_check_box);
      if (forceOfflinePayment) {
        request.setForceOfflinePayment(true);
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

    //Allow only selected card entry methods
    request.setCardEntryMethods(cardEntryMethodsAllowed);

    if (vaultedCard != null) {
      VaultedCard tempVaultedCard = vaultedCard;
      request.setVaultedCard(tempVaultedCard);
      vaultedCard = null;
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
    //Once the secure payment activity completes the result and its extras can be worked with
    switch (requestCode) {
      case SECURE_PAY_REQUEST_CODE :
        if (resultCode == RESULT_OK){
          onPaymentIntentResponse(data);
          Toast.makeText(getApplicationContext(), getString(R.string.payment_successful, lastOrder != null ? lastOrder.getId() : ""), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.payment_failed), Toast.LENGTH_LONG).show();
        }
        break;
      case MANUAL_REFUND_REQUEST_CODE :
        if (resultCode == RESULT_OK){
          Toast.makeText(getApplicationContext(), getString(R.string.manual_refund_successful, lastOrder != null ? lastOrder.getId() : ""), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.manual_refund_failed), Toast.LENGTH_LONG).show();
        }
        break;
      case READ_CARD_DATA_REQUEST_CODE :
        if (resultCode == RESULT_OK){
          PaymentRequestCardDetails cardDetails = data.getParcelableExtra(Intents.EXTRA_CARD_DATA);
          Toast.makeText(getApplicationContext(), getString(R.string.read_card_data_successful, cardDetails != null ? cardDetails.getTrack1() : ""), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.read_card_data_failed), Toast.LENGTH_LONG).show();
        }
        break;
      case VAULT_CARD_REQUEST_CODE :
        handleVaultCardResult(resultCode, data);
        break;
      default:
        Toast.makeText(getApplicationContext(), getString(R.string.start_secure_pay_failed), Toast.LENGTH_LONG).show();
        break;
    }
  }

  private void handleVaultCardResult (int resultCode, Intent data) {
    if (resultCode != RESULT_OK || data == null || !data.hasExtra(Intents.EXTRA_CARD_DATA)) {
      Toast.makeText(getApplicationContext(), getString(R.string.vault_card_failed), Toast.LENGTH_LONG).show();
      return;
    }

    final PaymentRequestCardDetails details = data.getParcelableExtra(Intents.EXTRA_CARD_DATA);

    String uri = ServerPing.getTokenUrl(this, merchant.getId());
    final String url = DeviceHttpClient.getCloudUri(this, uri).toString();

    final PaymentRequest paymentRequest = new PaymentRequest();
    paymentRequest.setCard(details);

    // TODO: Should use remoteControlClient.doUiState(...) before task to show status and allow cancel on POS?

    executeAsyncTask(HttpRequestTask.newPostRequest(this, url, paymentRequest, employee.getId(),
        new Callback<String>() {
          @Override
          public void onSuccess(String result) {
            if (isFinishing() || isDestroyed()) {
              ALog.i(MainActivity.class, "Activity is not valid");
              return;
            }

            if (result != null) {
              vaultedCard = new VaultedCard(result);
              // if the first 6 aren't 6 digits, then get the first 6(i.e. first4()) from the card details.
              // For manual cards, first6 is "XXXXXX"
              if (vaultedCard.getFirst6() == null || !vaultedCard.getFirst6().matches("\\d{6}")) {
                vaultedCard.setFirst6(details.getFirst4());
              }
              ALog.d(this, "Token = " + vaultedCard.getToken());

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(getApplicationContext(), getString(R.string.vault_card_successful), Toast.LENGTH_SHORT).show();
                }
              });
            } else {
              vaultedCard = null;
              ALog.e(this, "Error adding card: empty result from tokenization service");
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(getApplicationContext(), "Error adding card: empty result from tokenization service", Toast.LENGTH_LONG).show();
                }
              });
            }
          }

          @Override
          public void onFailure(int errorCode, String message) {
            vaultedCard = null;
            ALog.e(MainActivity.class, "Add card failed: " + errorCode + ", " + message);
            boolean offline = clover != null && clover.getConnectionStatus() == Clover.ConnectionState.DISCONNECTED;
            if (offline) {
              ALog.e(this, "Error adding card: device is currently offline");
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(getApplicationContext(), "Error adding card: device is currently offline", Toast.LENGTH_LONG).show();
                }
              });
            } else {
              ALog.e(this, "Error adding card: empty result from tokenization service");
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(getApplicationContext(), "Error adding card: empty result from tokenization service", Toast.LENGTH_LONG).show();
                }
              });
            }
          }

          @Override
          public void onTransportError(Throwable ex, Context context) {
            super.onTransportError(ex, context);
            vaultedCard = null;
            ALog.e(MainActivity.class, ex, "Transport error");
            Toast.makeText(getApplicationContext(), "Error adding card: Transport error", Toast.LENGTH_LONG).show();
          }
        }));
  }

  private <Params> void executeAsyncTask(AsyncTask<Params, ?, ?> task, Params... params) {
    // Use pool because thread interrupt does not affect IPC which will continue to hang
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
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

        double parsed = 0;
        if(cleanString.length() > 0)
        {
          parsed = Double.parseDouble(cleanString);
        }
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


  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    if(lastPayment != null)
    {
      try {
        savedInstanceState.putParcelable("lastPayment", lastPayment);
      }catch(Exception e)
      {
        Log.e("MainActivity", e.getMessage());
      }
    }
    if(lastOrder != null)
    {
      try {
        savedInstanceState.putParcelable("lastOrder", lastOrder);
      }catch(Exception e)
      {
        Log.e("MainActivity", e.getMessage());
      }
    }
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      isStation = Platform.isCloverStation();

      try{
        Object lp = savedInstanceState.getParcelable("lastPayment");
        if(lp != null)
        {
          setLastPayment((Payment) lp);
        }
        Object ord = savedInstanceState.getParcelable("lastOrder");
        if(ord != null)
        {
          setLastOrder((Order) ord);
        }
      }catch(Exception e)
      {
        Log.e("MainActivity", e.getMessage());
      }
  }
}

abstract class ServiceActivity
    extends Activity
    implements ServiceConnector.OnServiceConnectedListener, EmployeeConnector.OnActiveEmployeeChangedListener, CloverConnector.OnCloverChangedListener, OrderConnector.OnOrderUpdateListener{
  @SuppressWarnings("unused")
  private static final String TAG = "CommonActivity";

  public enum CardReaderStatus {
    CONNECTED(R.string.cardReaderStatusConnected), DISCONNECTED(R.string.cardReaderStatusDisconnected);

    private final int messageId;
    CardReaderStatus(int messageId) {
      this.messageId = messageId;
    }
  }

  public static final String MERCHANT_CONNECTOR = "merchant";
  public static final String EMPLOYEE_CONNECTOR = "employee";
  public static final String CLOVER_CONNECTOR = "clover";
  public static final String CUSTOMER_CONNECTOR = "customer";
  public static final String PRINTER_CONNECTOR = "printer";
  public static final String SHIFT_CONNECTOR = "shift";
  public static final String INVENTORY_CONNECTOR = "inventory";
  public static final String ORDER_CONNECTOR = "order";

  private static final int MAX_SERVICE_RETRY_ATTEMPTS = 5;
  private static final int SERVICE_REBIND_DELAY = 2000;
  private static final int MSG_REBIND = 0;

  protected Merchant merchant = null;
  protected Employee employee = null;
  protected CloverConnector cloverConnector;
  protected EmployeeConnector employeeConnector;
  protected OrderConnector orderConnector;

  protected Clover clover = null;
  protected CardReaderStatus cardReaderStatus = CardReaderStatus.DISCONNECTED;
  protected CardReader cardReader;
  protected Account account = null;
  ServiceConnectorManager mServiceConnectorManager;

  boolean setupComplete;
  long resumeTimestamp;

  private final CardReader.CardReadReceiver readCardReceiver = new CardReader.CardReadReceiver() {
    @Override
    protected void onReadCardSuccess(ReadCardResult result) {
      cardReaderStatus = CardReaderStatus.CONNECTED;
      onCardReaderConnected();
      invalidateOptionsMenu();
    }

    @Override
    protected void onReadCardEmpty() {
      cardReaderStatus = CardReaderStatus.CONNECTED;
      onCardReaderConnected();
      invalidateOptionsMenu();
    }

    @Override
    protected void onReadCardFailure(int reason, String message) {
    }
  };
  private final ScreenReceiver screenReceiver = new ScreenReceiver(this) {
    @Override
    protected void onScreenOff() {
      cardReaderStatus = CardReaderStatus.DISCONNECTED;
      onCardReaderDisconnected();
      invalidateOptionsMenu();
    }

    @Override
    protected void onScreenOn() {
    }
  };
  private final CardReader.OpenReaderReceiver openReaderReceiver = new CardReader.OpenReaderReceiver() {
    @Override
    protected void onOpenReaderSuccess() {
    }

    @Override
    protected void onOpenReaderFailure() {
      cardReaderStatus = CardReaderStatus.DISCONNECTED;
      onCardReaderDisconnected();
      invalidateOptionsMenu();
    }

    @Override
    protected void onReaderClose() {
      cardReaderStatus = CardReaderStatus.DISCONNECTED;
      onCardReaderDisconnected();
      invalidateOptionsMenu();
    }
  };

  @Override
  public void onCreate(Bundle savedInstance) {
    ALog.i(this, "%s +onCreate: %s", this, savedInstance);
    super.onCreate(savedInstance);
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
    mServiceConnectorManager = new ServiceConnectorManager(this.getApplicationContext(), account, this);
    this.cardReader = new CardReader(this);
    if (!mServiceConnectorManager.connect()) {
      finish();
      return;
    }

    cloverConnector = mServiceConnectorManager.getConnector(CommonActivity.CLOVER_CONNECTOR);
    cloverConnector.addOnCloverChangedListener(this);

    employeeConnector = mServiceConnectorManager.getConnector(CommonActivity.EMPLOYEE_CONNECTOR);
    employeeConnector.addOnActiveEmployeeChangedListener(this);

    orderConnector = mServiceConnectorManager.getConnector(CommonActivity.ORDER_CONNECTOR);
    orderConnector.addOnOrderChangedListener(this);
    ALog.i(this, "%s -onCreate", this);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (allServicesAreConnected()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            clover = cloverConnector.getClover();
            merchant = clover.getMerchant();
          } catch (RemoteException e) {
            e.printStackTrace();
          } catch (ClientException e) {
            e.printStackTrace();
          } catch (ServiceException e) {
            e.printStackTrace();
          } catch (BindingException e) {
            e.printStackTrace();
          }
          try {
            employee = employeeConnector.getEmployee();
          } catch (RemoteException e) {
            e.printStackTrace();
          } catch (ClientException e) {
            e.printStackTrace();
          } catch (ServiceException e) {
            e.printStackTrace();
          } catch (BindingException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  }


  @Override
  public void onResume() {
    ALog.i(this, "%s +onResume", this);
    super.onResume();
    resumeTimestamp = SystemClock.elapsedRealtime();

    cardReader.register(readCardReceiver);
    cardReader.register(openReaderReceiver);
    screenReceiver.register();
    setupComplete = allServicesAreConnected();
    ALog.i(this, "%s -onResume", this);
  }

  private boolean allServicesAreConnected() {
    if (employeeConnector != null && cloverConnector != null && orderConnector != null) {
      return true;
    }
    return false;
  }
  public <S extends ServiceConnector> S getConnector(String connectorId) {
    return mServiceConnectorManager.getConnector(connectorId);
  }

  @Override
  public void onPause() {
    ALog.i(this, "%s +onPause", this);
    setupComplete = false;
    cardReader.unregister(readCardReceiver);
    cardReader.unregister(openReaderReceiver);
    screenReceiver.unregister();
    super.onPause();
    ALog.i(this, "%s -onPause", this);
  }

  @Override
  protected void onDestroy() {
    ALog.i(this, "%s +onDestroy", this);
    CloverConnector cloverConnector = (CloverConnector) mServiceConnectorManager.get(CommonActivity.CLOVER_CONNECTOR);
    if (cloverConnector != null) {
      cloverConnector.removeOnCloverChangedListener(this);
    }

    EmployeeConnector employeeConnector = (EmployeeConnector) mServiceConnectorManager.get(CommonActivity.EMPLOYEE_CONNECTOR);
    if (employeeConnector != null) {
      employeeConnector.removeOnActiveEmployeeChangedListener(this);
    }

    OrderConnector orderConnector = (OrderConnector) mServiceConnectorManager.get(CommonActivity.ORDER_CONNECTOR);
    if (orderConnector != null) {
      orderConnector.removeOnOrderChangedListener(this);
    }

    disconnect();

    super.onDestroy();
    ALog.i(this, "%s -onDestroy", this);
  }

  @Override
  public void onServiceConnected(ServiceConnector connector) {
    ALog.d(this, "%s service connected: %s", this, connector);
  }

  @Override
  public void onServiceDisconnected(ServiceConnector connector) {
    ALog.i(this, "%s %s", this, connector);
    mServiceConnectorManager.removeConnector(connector);
  }

  public void disconnect() {
    mServiceConnectorManager.disconnect();
  }

  protected void onCardReaderConnected() {
  }

  protected void onCardReaderDisconnected() {
  }

  public CardReaderStatus getCardReaderStatus() {
    return cardReaderStatus;
  }

  @Override
  public void onActiveEmployeeChanged(final Employee employee) {
    ALog.i(this, "%s employee changed: %s", this, employee);
    this.employee = employee;
  }

  @Override
  public void onCloverChanged(Clover clover) {
    ALog.i(this, "%s clover changed: %s", this, clover);
    this.clover = clover;
    this.merchant = clover.getMerchant();
  }

  @Override
  public void onOrderUpdated(String orderId, boolean selfChange) {
    ALog.i(this, "%s order changed: %s", this, orderId);
  }

  @Override
  public void finish() {
    super.finish();
    ALog.i(this, "%s finish", this);
  }

}
