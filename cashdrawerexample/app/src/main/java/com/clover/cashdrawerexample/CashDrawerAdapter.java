package com.clover.cashdrawerexample;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.clover.sdk.cashdrawer.CashDrawer;

public class CashDrawerAdapter extends TypedAdapter<CashDrawer, CashDrawerAdapter.CashDrawerHolder> {
  static class CashDrawerHolder extends TypedAdapter.ViewHolder {
    private final TextView nameText;
    private final TextView numText;

    public CashDrawerHolder(View view) {
      super(view);

      this.nameText = (TextView) view.findViewById(R.id.text_name);
      this.numText = (TextView) view.findViewById(R.id.text_num);
    }
  }

  private final Context context;

  public CashDrawerAdapter(Context context) {
    super(R.layout.item_cash_drawer);
    this.context = context;
  }

  @Override
  protected void bind(CashDrawerHolder holder, CashDrawer item, int position) {
    holder.numText.setText(String.format("%02d.", position + 1));
    holder.nameText.setText(item.toString());
  }

  @Override
  protected CashDrawerHolder createHolder(View view) {
    return new CashDrawerHolder(view);
  }
}
