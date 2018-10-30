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
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;
import com.clover.example.utils.ImageUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RefundsListViewAdapter extends ArrayAdapter<POSTransaction> {
    public RefundsListViewAdapter(Context context, int resource) {
      super(context, resource);
    }

    public RefundsListViewAdapter(Context context, int resource, List<POSTransaction> items) {
      super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      View v = convertView;

      if (v == null) {
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        v = vi.inflate(R.layout.transactions_row, null);
      }

      POSTransaction posRefund = getItem(position);

      if (posRefund != null) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        TextView time = (TextView) v.findViewById(R.id.TransactionRowTime);
        TextView date = (TextView) v.findViewById(R.id.TransactionRowDate);
        TextView paymentType = (TextView) v.findViewById(R.id.TransactionRowPaymentType);
        TextView id = (TextView) v.findViewById(R.id.TransactionRowPaymentId);
        TextView total = (TextView) v.findViewById(R.id.TransactionRowTotal);
        ImageView tenderImage = (ImageView) v.findViewById(R.id.TransactionRowTenderImage);
        TextView tender = (TextView) v.findViewById(R.id.TransactionRowTender);
        TextView cardDetails = (TextView) v.findViewById(R.id.TransactionRowCardDetails);
        TextView employee = (TextView) v.findViewById(R.id.TransactionRowEmployee);

        time.setText(timeFormat.format(posRefund.getDate()));
        date.setText(dateFormat.format(posRefund.getDate()));
        paymentType.setText("Refund");
        id.setText(posRefund.getId());
        total.setText("("+CurrencyUtils.format(posRefund.getAmount(), Locale.getDefault())+")");
        total.setTextColor(v.getResources().getColor(R.color.red_text));
        tenderImage.setImageResource(ImageUtil.getCardTypeImage(posRefund.getCardType()));
        tender.setText(posRefund.getTender());
        cardDetails.setText(posRefund.getCardDetails());
        employee.setText(posRefund.getEmployee());
      }

      return v;
    }
}
