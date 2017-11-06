package com.clover.example.displayconnectorexample;

import com.clover.R;
import com.clover.connector.sdk.v3.DisplayConnector;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.connector.IDisplayConnector;
import com.clover.sdk.v3.connector.IDisplayConnectorListener;
import com.clover.sdk.v3.order.DisplayOrder;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class DisplayConnectorTestActivity extends AppCompatActivity {

  private static final String LOG_TAG = "DsplyCnnctrTst";

  boolean swap = false;
  DisplayOrder displayOrder1 = null;
  DisplayOrder displayOrder2 = null;
  private IDisplayConnector connector;

  /**
   * Grab the content of a input stream as text.
   * @param is inputstream to read
   * @return athe content of the stream as text
   */
  static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display_connector_test);
  }

  /**
   * Create a DisplayConnector, and set this class variable to the reference.
   */
  private void initDisplayConnector() {
    disposeDisplayConnector();
    // Retrieve the Clover account
    Account account = null;
    account = CloverAccount.getAccount(this);

    // If an account can't be acquired, exit the app
    if (account == null) {
      Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Log.d(LOG_TAG, String.format("Account is=%s", account));
    /*
     * Just listens for connection events.
     */
    IDisplayConnectorListener listener = new IDisplayConnectorListener() {
      @Override
      public void onDeviceDisconnected() {
        Log.d(LOG_TAG, "onDeviceDisconnected");
      }

      @Override
      public void onDeviceConnected() {
        Log.d(LOG_TAG, "onDeviceConnected");
      }
    };
    connector = new DisplayConnector(this, account, listener);
  }

  /**
   * Destroy this classes DisplayConnector and dispose of it.
   */
  private void disposeDisplayConnector() {
    if (connector != null) {
      connector.dispose();
      connector = null;
    }
  }

  /**
   * Create/dispose of the connector on this class.
   */
  private void toggleConnector() {
    if (connector != null) {
      disposeDisplayConnector();
    } else {
      initDisplayConnector();
    }
    setButtonState();
  }

  /**
   * Set the button states based on the connector instance.  If the connector is not
   * set, hide the action buttons and set the toggle button to allow for creation.
   */
  private void setButtonState() {
    if (connector != null) {
      ToggleButton toggleConnector = (ToggleButton) findViewById(R.id.toggleConnector);
      toggleConnector.setChecked(false);
      View action_buttons = findViewById(R.id.action_buttons);
      action_buttons.setVisibility(View.VISIBLE);
    } else {
      ToggleButton toggleConnector = (ToggleButton) findViewById(R.id.toggleConnector);
      toggleConnector.setChecked(true);
      View action_buttons = findViewById(R.id.action_buttons);
      action_buttons.setVisibility(View.INVISIBLE);
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (connector != null) {
      View action_buttons = findViewById(R.id.action_buttons);
      action_buttons.setVisibility(View.VISIBLE);
    } else {
      View action_buttons = findViewById(R.id.action_buttons);
      action_buttons.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    setButtonState();
  }

  @Override
  protected void onDestroy() {
    connector.dispose();
    super.onDestroy();
  }

  /*
  Handlers for the buttons
   */
  public void onClickToggleConnector(View view) {
    toggleConnector();
  }

  public void onClickDisplayOrder(View view) {
    connector.showDisplayOrder(generateTestDisplayOrder());
  }

  public void onClickShowWelcome(View view) {
    connector.showWelcomeScreen();
  }

  public void onClickShowMessage(View view) {
    connector.showMessage("This is some message");
  }

  public void onClickShowThankYou(View view) {
    connector.showThankYouScreen();
  }

  /**
   * @return an example DisplayOrder.  Two different orders will be returned; alternating
   */
  public DisplayOrder generateTestDisplayOrder() {
    if (displayOrder1 == null) {
      displayOrder1 = loadDisplayOrder(R.raw.displayorder1);
      displayOrder2 = loadDisplayOrder(R.raw.displayorder2);
    }
    DisplayOrder displayOrder = swap ? displayOrder1 : displayOrder2;
    swap = !swap;
    try {
      Log.d(LOG_TAG, String.format("DisplayOrder=%s", displayOrder.getJSONObject().toString(4)));
    } catch (JSONException e) {
      Log.e(LOG_TAG, "Error serializing DisplayOrder", e);
    }
    return displayOrder;
  }

  /**
   * @param fileId a resource id for a file containing a json-serialized DisplayOrder
   * @return a DisplayOrder
   */
  private DisplayOrder loadDisplayOrder(int fileId) {
    InputStream ins = null;
    DisplayOrder displayOrder = null;
    try {
      ins = getResources().openRawResource(fileId);
      String text = convertStreamToString(ins);
      displayOrder = new DisplayOrder(text);
    } finally {
      try {
        if (ins!=null) {
          ins.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return displayOrder;
  }
}
