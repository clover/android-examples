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

package com.clover.example;

import com.clover.example.utils.CurrencyUtils;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class RefundPaymentFragment extends DialogFragment {

  private long totalAmount;
  private View view;
  private Button fullRefund, partialRefund;
  List<PaymentRefundListener> listeners = new ArrayList<PaymentRefundListener>(5);
  private EditText refundAmount;

  public static RefundPaymentFragment newInstance(long totalAmount){
    RefundPaymentFragment fragment = new RefundPaymentFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setTotalAmount(totalAmount);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NO_TITLE, R.style.CustomDialog);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_refund_popup, container, false);

    refundAmount = (EditText) view.findViewById(R.id.PaymentRefundAmount);
    fullRefund = (Button) view.findViewById(R.id.MakeFullRefund);
    fullRefund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        makeFullRefund();
      }
    });
    partialRefund = (Button) view.findViewById(R.id.MakePartialRefund);
    partialRefund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        makePartialRefund(CurrencyUtils.convertToLong(refundAmount.getText().toString()));
      }
    });

    refundAmount.setSelection(refundAmount.getText().length());
    refundAmount.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          refundAmount.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

          refundAmount.setText(formatted);
          refundAmount.setSelection(formatted.length());
          refundAmount.addTextChangedListener(this);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });
    return view;
  }

  public long getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(long totalAmount) {
    this.totalAmount = totalAmount;
  }

  public void makePartialRefund(long amount) {
    if(amount <= totalAmount && amount > 0) {
      dismiss();
      for (PaymentRefundListener listener : listeners) {
        listener.makePartialRefund(amount);
      }
    }
    else{
      ((NativePOSActivity)getActivity()).showPopupMessage(null, new String []{"Amount must be less than total amount and greater than $0.00"}, false);
    }
  }

  public void makeFullRefund () {
    dismiss();
    for( PaymentRefundListener listener : listeners){
      listener.makeFullRefund();
    }
  }

  public interface PaymentRefundListener {
    public abstract void makePartialRefund(long amount);
    public abstract void makeFullRefund();
  }

  public void addListener(PaymentRefundListener listener) {
    listeners.add(listener);
  }

  private PaymentRefundListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (PaymentRefundListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement makePartialRefund and makeFullRefund");
    }
  }
}