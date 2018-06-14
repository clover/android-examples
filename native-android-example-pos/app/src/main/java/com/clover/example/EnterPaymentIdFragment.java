/*
 * Copyright (C) 2016 Clover Network, Inc.
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
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class EnterPaymentIdFragment extends DialogFragment {

  private View view;
  private TextView close;
  private EditText paymentId;
  private Button lookup;
  List<EnterPaymentIdFragment.EnterPaymentIdFragmentListener> listeners = new ArrayList<EnterPaymentIdFragmentListener>(5);

  public static EnterPaymentIdFragment newInstance(){
    EnterPaymentIdFragment fragment = new EnterPaymentIdFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
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
    view = inflater.inflate(R.layout.fragment_enter_payment_id, container, false);
    paymentId = (EditText) view.findViewById(R.id.PaymentIdEditText);
    close = (TextView) view.findViewById(R.id.CloseEnterPaymentId);
    close.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
    lookup = (Button) view.findViewById(R.id.PaymentIdLookup);
    lookup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onLookup();
      }
    });

    return view;
  }

  public void onLookup() {
    dismiss();
    for( EnterPaymentIdFragment.EnterPaymentIdFragmentListener listener : listeners){
      listener.onLookup(paymentId.getText().toString());
    }
  }

  public interface EnterPaymentIdFragmentListener {
    public abstract void onLookup(String paymentId);
  }

  public void addListener(EnterPaymentIdFragment.EnterPaymentIdFragmentListener listener) {
    listeners.add(listener);
  }

  private EnterPaymentIdFragment.EnterPaymentIdFragmentListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (EnterPaymentIdFragment.EnterPaymentIdFragmentListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }

}
