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
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;
import com.clover.example.utils.ImageUtil;
import com.clover.sdk.v3.payments.Result;
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

public class PaymentsListViewAdapter extends ArrayAdapter<POSTransaction> {

  public PaymentsListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public PaymentsListViewAdapter(Context context, int resource, List<POSTransaction> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.payments_row, null);
    }

    POSTransaction posPayment = getItem(position);

    if (posPayment != null) {
      ImageView resultImage = (ImageView) v.findViewById(R.id.OrderTransactionImage);
      TextView statusColumn = (TextView) v.findViewById(R.id.PaymentsRowStatusColumn);
      TextView totalColumn = (TextView) v.findViewById(R.id.PaymentsRowTotalColumn);
      TextView time = (TextView) v.findViewById(R.id.OrderTransactionTime);
      TextView date = (TextView) v.findViewById(R.id.OrderTransactionDate);
      TextView tender = (TextView) v.findViewById(R.id.OrderTransactionTender);
      TextView cardDetails = (TextView) v.findViewById(R.id.OrderTransactionCardDetails);
      ImageView tenderImage = (ImageView) v.findViewById(R.id.OrderTransactionTenderImage);
      SimpleDateFormat localDateFormat = new SimpleDateFormat("hh:mm a");
      DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
      time.setText(localDateFormat.format(posPayment.getDate()));
      date.setText(formatter.format(posPayment.getDate()));

      tender.setText(posPayment.getTender());
      cardDetails.setText(posPayment.getCardDetails());
      tenderImage.setImageResource(ImageUtil.getCardTypeImage(posPayment.getCardType()));
      totalColumn.setVisibility(View.VISIBLE);
      v.setAlpha(1);
      if(posPayment.getResult() == Result.SUCCESS){
        resultImage.setBackgroundResource(R.drawable.status_green);
      }
      else if(posPayment.getResult() == Result.FAIL){
        resultImage.setBackgroundResource(R.drawable.status_red);
      }
      if (posPayment instanceof POSPayment) {
        POSPayment payment = (POSPayment) posPayment;
        statusColumn.setText(payment.getPaymentStatus() == null ? "" : "" + (payment.getPaymentStatus()));
        statusColumn.setTextColor(v.getResources().getColor(R.color.black));
        totalColumn.setText(CurrencyUtils.format((posPayment).getAmount(), Locale.getDefault()));
        totalColumn.setTextColor(v.getResources().getColor(R.color.black));
        if(payment.getPaymentStatus() == POSPayment.Status.VOIDED){
          v.setAlpha((float)0.4);
        }
      } else if (posPayment instanceof POSRefund) {
        statusColumn.setText("REFUND");
        statusColumn.setTextColor(v.getResources().getColor(R.color.red_text));
        totalColumn.setText("("+CurrencyUtils.format((posPayment).getAmount(), Locale.getDefault())+")");
        totalColumn.setTextColor(v.getResources().getColor(R.color.red_text));

      }
    }
    return v;
  }
}
