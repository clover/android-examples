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

public class AdjustTipFragment extends DialogFragment {

  private View view;
  private EditText adjustTipAmount;
  private Button adjustTipButton;
  private long tipAmount;
  List<AdjustTipFragment.AdjustTipFragmentListener> listeners = new ArrayList<AdjustTipFragmentListener>(5);

  public static AdjustTipFragment newInstance(long tipAmount){
    AdjustTipFragment fragment = new AdjustTipFragment();
    fragment.setTipAmount(tipAmount);
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
    view = inflater.inflate(R.layout.fragment_adjust_tip, container, false);

    adjustTipAmount = (EditText) view.findViewById(R.id.AdjustTipAmount);
    adjustTipAmount.setText(CurrencyUtils.convertToString(tipAmount));
    adjustTipAmount.setSelection(adjustTipAmount.getText().length());
    adjustTipAmount.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          adjustTipAmount.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));
          adjustTipAmount.setText(formatted);
          adjustTipAmount.setSelection(formatted.length());
          adjustTipAmount.addTextChangedListener(this);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });

    adjustTipButton = (Button) view.findViewById(R.id.AdjustTipButton);
    adjustTipButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSave();
      }
    });

    return view;
  }

  public void setTipAmount(long amount){
    this.tipAmount = amount;
  }

  public void onSave() {
    dismiss();
    for( AdjustTipFragment.AdjustTipFragmentListener listener : listeners){
      listener.onSave(CurrencyUtils.convertToLong(adjustTipAmount.getText().toString()));
    }
  }

  public interface AdjustTipFragmentListener {
    public abstract void onSave(long tipAmount);
  }

  public void addListener(AdjustTipFragment.AdjustTipFragmentListener listener) {
    listeners.add(listener);
  }

  private AdjustTipFragment.AdjustTipFragmentListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (AdjustTipFragment.AdjustTipFragmentListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }

}
