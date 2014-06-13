package com.clover.example.paywithregisterexample;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;


public class MainActivity extends Activity {

  private Account account;
  private OrderConnector orderConnector;
  private Order order;
  private Button payButton;

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

      if (account == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    // Create and Connect
    connect();

    // create order
    new OrderAsyncTask().execute();

    payButton = (Button) findViewById(R.id.pay_button);
    payButton.setEnabled(false);
    payButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // start intent to launch clover's register pay activity
        Intent intent = new Intent(Intents.ACTION_CLOVER_REGISTER_PAY);
        intent.putExtra(Intents.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
      }
    });
  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }

  private void connect() {
    disconnect();
    if (account != null) {
      orderConnector = new OrderConnector(this, account, null);
      orderConnector.connect();
    }
  }

  private void disconnect() {
    if (orderConnector != null) {
      orderConnector.disconnect();
      orderConnector = null;
    }
  }

  private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {

    @Override
    protected final Order doInBackground(Void... params) {
      String orderId = null;
      Cursor cursor = null;
      try {
        // Query the last order
        cursor = MainActivity.this.getContentResolver().query(OrderContract.Summaries.contentUriWithAccount(account), new String[]{OrderContract.Summaries.ID}, null, null, OrderContract.Summaries.LAST_MODIFIED + " DESC LIMIT 1");
        if (cursor != null && cursor.moveToFirst()) {
          orderId = cursor.getString(cursor.getColumnIndex(OrderContract.Summaries.ID));
        }

        if (orderId == null) {
          return orderConnector.createOrder(new Order());
        } else {
          return orderConnector.getOrder(orderId);
        }
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
      MainActivity.this.order = order;
      if (order != null) {
        payButton.setEnabled(true);
      }
    }
  }
}
