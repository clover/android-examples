package com.clover.example.modifyorder.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ForbiddenException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.order.Discount;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.OrderConnector;


public class AddDiscountActivity extends Activity {
  private static final String TAG = "AddDiscountActivity";
  private Button mAddButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_discount);

    mAddButton = (Button) findViewById(R.id.add_discount_button);
    mAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String orderId = getIntent().getStringExtra(Intents.EXTRA_ORDER_ID);
        addDiscount(orderId);
      }
    });
  }

  private void addDiscount(final String orderId) {
    Log.w("AddDiscount", orderId);

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          OrderConnector orderConnector = new OrderConnector(AddDiscountActivity.this, CloverAccount.getAccount(AddDiscountActivity.this), null);
          orderConnector.connect();

          final Discount discount = new Discount();
          discount.setPercentage(10l);
          discount.setName("Example 10% Discount");

          orderConnector.addDiscount(orderId, discount);
          orderConnector.disconnect();
        } catch (Exception e) {
        }
        return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        finish();
      }
    }.execute();

  }
}
