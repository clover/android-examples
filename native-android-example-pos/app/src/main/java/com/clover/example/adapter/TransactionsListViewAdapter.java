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

public class TransactionsListViewAdapter extends ArrayAdapter<POSTransaction> {
  public TransactionsListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public TransactionsListViewAdapter(Context context, int resource, List<POSTransaction> items) {
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

    POSTransaction transaction = getItem(position);

    if (transaction != null) {
      SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
      DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

      TextView time = (TextView) v.findViewById(R.id.TransactionRowTime);
      TextView date = (TextView) v.findViewById(R.id.TransactionRowDate);
      TextView paymentType = (TextView) v.findViewById(R.id.TransactionRowPaymentType);
      TextView paymentId = (TextView) v.findViewById(R.id.TransactionRowPaymentId);
      TextView total = (TextView) v.findViewById(R.id.TransactionRowTotal);
      total.setTextColor(v.getResources().getColor(R.color.black));
      ImageView tenderImage = (ImageView) v.findViewById(R.id.TransactionRowTenderImage);
      TextView tender = (TextView) v.findViewById(R.id.TransactionRowTender);
      TextView cardDetails = (TextView) v.findViewById(R.id.TransactionRowCardDetails);
      TextView employee = (TextView) v.findViewById(R.id.TransactionRowEmployee);

      time.setText(timeFormat.format(transaction.getDate()));
      date.setText(dateFormat.format(transaction.getDate()));
      paymentType.setText(transaction.getTransactionTitle());
      paymentId.setText(transaction.getId());
      paymentId.setVisibility(View.VISIBLE);
      total.setText(CurrencyUtils.convertToString(transaction.getAmount()));
      tenderImage.setImageResource(ImageUtil.getCardTypeImage(transaction.getCardType()));
      tender.setText(transaction.getTender());
      cardDetails.setText(transaction.getCardDetails());
      employee.setText(transaction.getEmployee());

      v.setAlpha(1);
      if(transaction.getResult() ==  Result.SUCCESS){
        ImageView check = (ImageView) v.findViewById(R.id.TransactionRowStatusImage);
        check.setVisibility(View.VISIBLE);
      }

      if(transaction instanceof POSPayment){
        if(((POSPayment)transaction).getPaymentStatus() == POSPayment.Status.VOIDED){
          paymentType.setText("Voided");
          total.setText("("+CurrencyUtils.convertToString(transaction.getAmount())+")");
          total.setTextColor(v.getResources().getColor(R.color.red_text));
          paymentId.setVisibility(View.GONE);
          v.setAlpha((float).4);
        }
      }
      else if(transaction instanceof POSRefund){
        if(transaction.getTransactionTitle() == "Refund"){
          total.setText("-"+CurrencyUtils.convertToString(transaction.getAmount())+"");
          total.setTextColor(v.getResources().getColor(R.color.red_text));
        }
      }
      if(transaction.getRefund()){
        total.setText("-"+CurrencyUtils.convertToString(transaction.getAmount()));
        total.setTextColor(v.getResources().getColor(R.color.red_text));
      }
  }
    return v;
  }
}
