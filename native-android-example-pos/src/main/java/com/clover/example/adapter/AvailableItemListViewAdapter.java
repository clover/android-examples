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
import com.clover.example.model.POSLineItem;
import com.clover.example.utils.CurrencyUtils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

public class AvailableItemListViewAdapter extends ArrayAdapter<POSLineItem> {

  Context context;
  int layoutResourceId;
  POSLineItem[] data = null;

  public AvailableItemListViewAdapter(Context context, int layoutResourceId, POSLineItem[] data) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.data = data;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    POSLineItemHolder holder = null;

    if (row == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      row = inflater.inflate(layoutResourceId, parent, false);

      holder = new POSLineItemHolder();
      holder.itemQuantity = (TextView) row.findViewById(R.id.OrderItemQuantityLabel);
      holder.itemName = (TextView) row.findViewById(R.id.OrderItemNameLabel);
      holder.itemPrice = (TextView) row.findViewById(R.id.OrderItemPriceLabel);
      holder.itemDiscount = (TextView) row.findViewById(R.id.OrderItemDiscountLabel);
      holder.itemDiscountPrice = (TextView) row.findViewById(R.id.OrderItemDiscountPriceLabel);

      row.setTag(holder);
    } else {
      holder = (POSLineItemHolder) row.getTag();
    }

    POSLineItem lineItem = data[position];
    holder.itemName.setText(lineItem.getItem().getName());
    holder.itemQuantity.setText(lineItem.getQuantity() + "");
    holder.itemPrice.setText(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
    if (lineItem.getDiscount() != null) {
      holder.itemDiscount.setText(lineItem.getDiscount().getName());
      holder.itemDiscountPrice.setText(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getItem().getPrice()), Locale.getDefault()));
    } else {
      holder.itemDiscount.setText("");
      holder.itemDiscountPrice.setText("");
    }

    return row;
  }

  static class POSLineItemHolder {
    TextView itemQuantity;
    TextView itemName;
    TextView itemPrice;
    TextView itemDiscount;
    TextView itemDiscountPrice;
  }
}





