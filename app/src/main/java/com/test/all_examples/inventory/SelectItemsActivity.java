package com.test.all_examples.inventory;

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
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;
import com.test.all_examples.R;


public class SelectItemsActivity extends Activity {

  private Account account;
  private OrderConnector orderConnector;
  private Order order;
  private Button mSelectItemButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_items);
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

    mSelectItemButton = (Button) findViewById(R.id.select_item_button);
    mSelectItemButton.setEnabled(false);
    mSelectItemButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // start intent to launch clover's select item activity
        Intent intent = new Intent("com.clover.intent.action.ITEM_SELECT");
        intent.putExtra("com.clover.intent.extra.ORDER_ID", order.getId());
        startActivityForResult(intent, 1001);
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
        cursor = SelectItemsActivity.this.getContentResolver().query(OrderContract.Summaries.contentUriWithAccount(account), new String[]{OrderContract.Summaries.ID}, null, null, OrderContract.Summaries.LAST_MODIFIED + " DESC LIMIT 1");
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
      SelectItemsActivity.this.order = order;
      if (order != null) {
        mSelectItemButton.setEnabled(true);
      }
    }
  }
}
