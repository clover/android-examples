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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class ItemsListViewAdapter extends ArrayAdapter<POSLineItem> {

  public ItemsListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public ItemsListViewAdapter(Context context, int resource, List<POSLineItem> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.items_row, null);
    }

    POSLineItem posLI = getItem(position);

    if (posLI != null) {
      TextView quantityColumn = (TextView) v.findViewById(R.id.ItemsRowQuantityColumn);
      TextView descriptionColumn = (TextView) v.findViewById(R.id.ItemsRowDescriptionColumn);
      TextView priceColumn = (TextView) v.findViewById(R.id.ItemsRowPriceColumn);

      quantityColumn.setText("" + posLI.getQuantity());
      descriptionColumn.setText(posLI.getItem().getName());
      priceColumn.setText(CurrencyUtils.format(posLI.getItem().getPrice(), Locale.getDefault()));
    }

    return v;
  }
}
