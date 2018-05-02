/*
 * Copyright (C) 2018 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.example.adapter;

import com.clover.example.R;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrdersListViewAdapter extends ArrayAdapter<POSOrder> {

  public OrdersListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public OrdersListViewAdapter(Context context, int resource, List<POSOrder> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.orders_row, null);
    }

    POSOrder posOrder = getItem(position);

    if (posOrder != null) {
      TextView idColumn = (TextView) v.findViewById(R.id.OrdersRowIdColumn);
      TextView time = (TextView) v.findViewById(R.id.OrderTime);
      TextView date = (TextView) v.findViewById(R.id.OrderDate);
      TextView tender = (TextView) v.findViewById(R.id.OrderTender);
      TextView statusColumn = (TextView) v.findViewById(R.id.OrdersRowStatusColumn);
      TextView subtotalColumn = (TextView) v.findViewById(R.id.OrdersRowSubtotalColumn);

      SimpleDateFormat localDateFormat = new SimpleDateFormat("hh:mm a");
      DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
      idColumn.setText(posOrder.id);
      time.setText(localDateFormat.format(posOrder.date));
      date.setText(formatter.format(posOrder.date));
      if(posOrder.getPayments().size() > 0) {
        POSTransaction payment = posOrder.getPayments().get(0);
        tender.setText(payment.getTender());
      }
      String status = posOrder.getStatus().toString();
      statusColumn.setText(status);
      if(status == "PAID"){
        statusColumn.setTextColor(v.getResources().getColor(R.color.green_text));
      }
      else if(status == "REFUNDED" || status == "MANUALLY REFUNDED"){
        statusColumn.setTextColor(v.getResources().getColor(R.color.red_text));
      }
      else{
        statusColumn.setTextColor(v.getResources().getColor(R.color.yellow_text));
      }

      subtotalColumn.setText(CurrencyUtils.format(posOrder.getTotal(), Locale.getDefault()));
    }

    return v;
  }
}
