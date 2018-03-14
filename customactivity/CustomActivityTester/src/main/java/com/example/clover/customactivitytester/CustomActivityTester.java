package com.example.clover.customactivitytester;

import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.CloverDeviceConfiguration;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.USBCloverDeviceConfiguration;
import com.clover.remote.client.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.MessageToActivity;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.RetrievePrintersResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.ByteArrayToBase64TypeAdapter;
import com.clover.remote.message.CloverJSONifiableTypeAdapter;
import com.clover.remote.message.TipAddedMessage;
import com.clover.sdk.JSONifiable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class CustomActivityTester extends Activity {

  private static final String TAG = "CustomActivityTester";
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final String EXTRA_CLOVER_CONNECTOR_CONFIG = "EXTRA_CLOVER_CONNECTOR_CONFIG";
  public static final String EXTRA_WS_ENDPOINT = "WS_ENDPOINT";
  public static final String EXTRA_CLEAR_TOKEN = "CLEAR_TOKEN";
  private LinearLayout initialLayout, finalLayout, sendPayloadLayout, messagesLayout;
  private TextView initialPayload,finalPayload;
  private EditText customActionName, activityPayload, initialPayloadContent;
  private ListView messageList;
  private ICloverConnector cloverConnector;
  private SharedPreferences sharedPreferences;
  private AlertDialog pairingCodeDialog;
  private static final Gson GSON;
  private Button sendPayload, startActivity;

  private List<PayloadMessage> messages;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeHierarchyAdapter(JSONifiable.class, new CloverJSONifiableTypeAdapter());
    builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
    GSON = builder.create();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_custom_activity_tester);

    messages = new ArrayList<PayloadMessage>();
    initialLayout = (LinearLayout) findViewById(R.id.InitialPayloadLayout);
    sendPayloadLayout = (LinearLayout) findViewById(R.id.SendPayloadLayout);
    initialPayload = (TextView) findViewById(R.id.InitialPayload);
    initialPayloadContent = (EditText) findViewById(R.id.InitialPayloadContent);
    finalLayout = (LinearLayout) findViewById(R.id.FinalPayloadLayout);
    finalPayload = (TextView) findViewById(R.id.FinalPayload);
    customActionName = (EditText) findViewById(R.id.CustomActionName);
    activityPayload = (EditText) findViewById(R.id.ActivityPayload);
    startActivity = (Button) findViewById(R.id.StartCustomActivity);
    sendPayload = (Button) findViewById(R.id.sendPayloadToCustomActivity);
    messagesLayout = (LinearLayout) findViewById(R.id.MessagesLayout);
    messageList = (ListView) findViewById(R.id.MessagesListView);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    String posName = "Clover Custom Activity Tester";
    String applicationId = posName + ":1.4.1";
    CloverDeviceConfiguration config;

    String configType = getIntent().getStringExtra(EXTRA_CLOVER_CONNECTOR_CONFIG);
    if ("USB".equals(configType)) {
      config = new USBCloverDeviceConfiguration(this, applicationId);
    } else if ("WS".equals(configType)) {

      String serialNumber = "Aisle 4";
      String authToken = null;

      URI uri = (URI) getIntent().getSerializableExtra(EXTRA_WS_ENDPOINT);

      String query = uri.getRawQuery();
      if (query != null) {
        try {
          String[] nameValuePairs = query.split("&");
          for (String nameValuePair : nameValuePairs) {
            String[] nameAndValue = nameValuePair.split("=", 2);
            String name = URLDecoder.decode(nameAndValue[0], "UTF-8");
            String value = URLDecoder.decode(nameAndValue[1], "UTF-8");

            if("authenticationToken".equals(name)) {
              authToken = value;
            } else {
              Log.w(TAG, String.format("Found query parameter \"%s\" with value \"%s\"",
                  name, value));
            }
          }
          uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),uri.getPort(), uri.getPath(), null,uri.getFragment());
        } catch (Exception e) {
          Log.e(TAG, "Error extracting query information from uri.", e);
          setResult(RESULT_CANCELED);
          finish();
          return;
        }
      }
      KeyStore trustStore = createTrustStore();

      if(authToken == null) {
        boolean clearToken = getIntent().getBooleanExtra(EXTRA_CLEAR_TOKEN, false);
        if (!clearToken) {
          authToken = sharedPreferences.getString("AUTH_TOKEN", null);
        }
      }
      config = new WebSocketCloverDeviceConfiguration(uri, applicationId, trustStore, posName, serialNumber, authToken) {
        @Override
        public int getMaxMessageCharacters() {
          return 0;
        }

        @Override
        public void onPairingCode(final String pairingCode) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // If we previously created a dialog and the pairing failed, reuse
              // the dialog previously created so that we don't get a stack of dialogs
              if (pairingCodeDialog != null) {
                pairingCodeDialog.setMessage("Enter pairing code: " + pairingCode);
              } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomActivityTester.this);
                builder.setTitle("Pairing Code");
                builder.setMessage("Enter pairing code: " + pairingCode);
                pairingCodeDialog = builder.create();
              }
              pairingCodeDialog.show();
            }
          });
        }

        @Override
        public void onPairingSuccess(String authToken) {
          Preferences.userNodeForPackage(CustomActivityTester.class).put("AUTH_TOKEN", authToken);
          sharedPreferences.edit().putString("AUTH_TOKEN", authToken).apply();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
                pairingCodeDialog.dismiss();
                pairingCodeDialog = null;
              }
            }
          });
        }
      };
    } else {
      finish();
      return;
    }

    cloverConnector = new CloverConnector(config);
    initialize();

    PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(this, R.id.MessagesListView, messages);
    messageList.setAdapter(payloadMessageAdapter);

    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity();
      }
    });
    sendPayload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        sendMessageToActivity();
      }
    });
    View[] toDisable = {sendPayloadLayout, sendPayload};
    for(View view : toDisable){
      disableView(view);
    }
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
  }

  public void initialize() {

    if (cloverConnector != null) {
      cloverConnector.dispose();
    }

    ICloverConnectorListener ccListener = new ICloverConnectorListener() {
      public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomActivityTester.this, "Disconnected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "disconnected");
          }
        });

      }

      public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomActivityTester.this, "Connecting...", Toast.LENGTH_SHORT);
          }
        });
      }

      public void onDeviceReady(final MerchantInfo merchantInfo) {
        runOnUiThread(new Runnable() {
          public void run() {
            if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
              pairingCodeDialog.dismiss();
              pairingCodeDialog = null;
            }
            Toast.makeText(CustomActivityTester.this, "Ready!", Toast.LENGTH_SHORT);
          }
        });
        RetrievePrintersRequest rpr = new RetrievePrintersRequest();
        cloverConnector.retrievePrinters(rpr);
      }

      public void onError(final Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomActivityTester.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG);
          }
        });
      }

      public void onDebug(final String s) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomActivityTester.this, "Debug: " + s, Toast.LENGTH_LONG);
          }
        });
      }

      @Override
      public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {

      }

      @Override
      public void onReadCardDataResponse(final ReadCardDataResponse response) {
      }

      @Override
      public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
      }

      @Override
      public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {
        Toast.makeText(CustomActivityTester.this, "DeviceError: " + deviceErrorEvent.getMessage(), Toast.LENGTH_LONG);
      }

      @Override
      public void onAuthResponse(final AuthResponse response) {
      }

      @Override
      public void onPreAuthResponse(final PreAuthResponse response) {
      }

      @Override
      public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
      }

      @Override
      public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
      }

      @Override
      public void onCapturePreAuthResponse(final CapturePreAuthResponse response) {
      }

      @Override
      public void onVerifySignatureRequest(final VerifySignatureRequest request) {
      }

      @Override
      public void onMessageFromActivity(MessageFromActivity message) {
        messages.add(new PayloadMessage(message.getPayload(), false));
        updateMessages();
      }

      @Override
      public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
      }

      @Override
      public void onCloseoutResponse(CloseoutResponse response) {
      }

      @Override
      public void onSaleResponse(final SaleResponse response) {
      }

      @Override
      public void onManualRefundResponse(final ManualRefundResponse response) {
      }

      @Override
      public void onRefundPaymentResponse(final RefundPaymentResponse response) {
      }

      @Override
      public void onTipAdded(TipAddedMessage message) {

      }

      @Override
      public void onVoidPaymentResponse(VoidPaymentResponse response) {
      }

      @Override
      public void onVaultCardResponse(final VaultCardResponse response) {
      }

      @Override
      public void onPrintJobStatusResponse(PrintJobStatusResponse response) {
      }

      @Override
      public void onRetrievePrintersResponse(RetrievePrintersResponse response) {
      }

      @Override
      public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage pcm) {
      }

      @Override
      public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage pcdrm) {
      }

      @Override
      public void onPrintPaymentReceipt(PrintPaymentReceiptMessage pprm) {
      }

      @Override
      public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage ppdrm) {
      }

      @Override
      public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage ppmcrm) {
      }

      @Override
      public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage pprrm) {
      }

      @Override
      public void onCustomActivityResponse(final CustomActivityResponse response) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            String message = "";
            if (response.isSuccess()) {
              message = "Success! Got: " + response.getPayload() + " from CustomActivity" + response.getAction();
              Toast.makeText(CustomActivityTester.this,message, Toast.LENGTH_LONG);
            } else {
              if (response.getResult().equals(ResultCode.CANCEL)) {
                message = "Failure! Custom activity: " + response.getAction() + " failed. Reason: " + response.getReason();
                Toast.makeText(CustomActivityTester.this, message, Toast.LENGTH_LONG);
              } else {
                message = "Custom activity: " + response.getAction() + " was canceled. Reason: " + response.getReason();
                Toast.makeText(CustomActivityTester.this, message, Toast.LENGTH_LONG);
              }
            }
            finalPayload.setText(response.getPayload());
            View[] toDisable = {messagesLayout, sendPayloadLayout, sendPayload};
            for(View view : toDisable){
              disableView(view);
            }
            View [] toEnable = {customActionName, initialPayloadContent, startActivity};
            for(View view : toEnable){
              enableView(view);
            }
          }
        });
      }

      @Override
      public void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response) {
      }

      @Override
      public void onResetDeviceResponse(ResetDeviceResponse response) {
      }

      @Override
      public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
      }
    };
    cloverConnector.addCloverConnectorListener(ccListener);
    cloverConnector.initializeConnection();
  }

  private KeyStore createTrustStore() {
    try {
      String STORETYPE = "PKCS12";
      KeyStore trustStore = KeyStore.getInstance(STORETYPE);
      InputStream trustStoreStream = getClass().getResourceAsStream("/certs/clover_cacerts.p12");
      String TRUST_STORE_PASSWORD = "clover";

      trustStore.load(trustStoreStream, TRUST_STORE_PASSWORD.toCharArray());

      return trustStore;
    } catch (Throwable t) {
      Log.e(getClass().getSimpleName(), "Error loading trust store", t);
      t.printStackTrace();
      return null;
    }

  }

  public void enableView (View view){
    view.setEnabled(true);
    view.setAlpha(1);
  }

  public void disableView (View view){
    view.setEnabled(false);
    view.setAlpha((float)0.4);
  }

  public void updateMessages(){
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(CustomActivityTester.this, R.id.MessagesListView, messages);
        messageList.setAdapter(payloadMessageAdapter);
      }
    });
  }

  public void startActivity() {
    String activityId = customActionName.getText().toString();
    String payload = initialPayloadContent.getText().toString();

    CustomActivityRequest car = new CustomActivityRequest(activityId);
    car.setPayload(payload);
    boolean nonBlocking = ((Switch) findViewById(R.id.customActivityBlocking)).isChecked();
    car.setNonBlocking(nonBlocking);
    initialPayload.setText(payload);

    View [] toDisable = {customActionName, initialPayloadContent, startActivity};
    for(View view : toDisable){
      disableView(view);
    }
    View[] toEnable = {sendPayloadLayout, sendPayload, messagesLayout};
    for(View view : toEnable){
      enableView(view);
    }
    messages = new ArrayList<PayloadMessage>();
    updateMessages();
    initialPayloadContent.setText("");
    finalPayload.setText("");

    cloverConnector.startCustomActivity(car);
  }

  public void sendMessageToActivity() {
    String activityId = customActionName.getText().toString();
    String payload = activityPayload.getText().toString();
    MessageToActivity messageRequest = new MessageToActivity(activityId, toJsonString(payload));
    cloverConnector.sendMessageToActivity(messageRequest);
    messages.add(new PayloadMessage(payload, true));
    activityPayload.setText("");
    updateMessages();
  }

  public String toJsonString(String message) {
    return GSON.toJson(message);
  }

}
