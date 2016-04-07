package com.test.all_examples.orders;

import android.accounts.Account;
import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;
import com.test.all_examples.R;

import java.math.BigDecimal;
import java.util.Date;

public class ReadCurrentOrderActivity extends Activity {

  private Account account;
  private OrderConnector orderConnector;

  private TextView orderId;
  private TextView lineItemCount;
  private TextView total;
  private TextView createTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_current_order);

    orderId = (TextView) findViewById(R.id.order_id);
    lineItemCount = (TextView) findViewById(R.id.line_item_count);
    total = (TextView) findViewById(R.id.total);
    createTime = (TextView) findViewById(R.id.create_time);
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

    // Create and Connect to the OrderConnector
    connect();

    // Load the last order or create a new order
    loadLastOrder();
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

  private void loadLastOrder() {
    new OrderAsyncTask().execute();
  }

  private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {

    @Override
    protected final Order doInBackground(Void... params) {
      String orderId = null;
      Cursor cursor = null;
      try {
        // Query the last order
        cursor = ReadCurrentOrderActivity.this.getContentResolver().query(OrderContract.Summaries.contentUriWithAccount(account), new String[]{OrderContract.Summaries.ID}, null, null, OrderContract.Summaries.LAST_MODIFIED + " DESC LIMIT 1");
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
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
      return null;
    }

    @Override
    protected final void onPostExecute(Order order) {
      // Populate the UI
      orderId.setText(order.getId());

      int lineItemSize = 0;

      if (order.getLineItems() != null) {
        lineItemSize = order.getLineItems().size();
      }
      lineItemCount.setText(Integer.toString(lineItemSize));
      total.setText(BigDecimal.valueOf(order.getTotal()).divide(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
      createTime.setText(new Date(order.getCreatedTime()).toString());
    }
  }
}
