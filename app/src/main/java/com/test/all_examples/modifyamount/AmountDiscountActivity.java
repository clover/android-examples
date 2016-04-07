package com.test.all_examples.modifyamount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.clover.sdk.v1.Intents;
import com.test.all_examples.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class AmountDiscountActivity extends Activity {
  private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
  private Button mAddButton;

  public static double longToDouble(long l) {
    final double d = new BigDecimal(l).divide(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP).doubleValue();
    return d;
  }

  public static long doubleToLong(Double d) {
    return d != null ? new BigDecimal(d).multiply(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP).longValue() : 0;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_modify_amount_discount);

    final Long amount = getIntent().hasExtra(Intents.EXTRA_AMOUNT) ? getIntent().getLongExtra(Intents.EXTRA_AMOUNT, 0l) : null;
    if (amount != null) {

      TextView amountTextView = (TextView) findViewById(R.id.amount);

      NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
      String formattedAmount = currencyFormat.format(amount / 100.0);
      amountTextView.setText(formattedAmount);

      mAddButton = (Button) findViewById(R.id.add_discount_button);
      mAddButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent data = new Intent();
          double discountedAmount = (longToDouble(amount)) * 0.9;
          data.putExtra(Intents.EXTRA_AMOUNT, doubleToLong(discountedAmount));
          setResult(RESULT_OK, data);
          finish();
        }
      });
    }
  }
}
