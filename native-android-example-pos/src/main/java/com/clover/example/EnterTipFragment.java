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

public class EnterTipFragment extends DialogFragment {

  private View view;
  private Button enterTipContinue, enterTipNo;
  List<EnterTipDialogFragmentListener> listeners = new ArrayList<EnterTipDialogFragmentListener>(5);
  private EditText enterTipAmount;

  public static EnterTipFragment newInstance(){
    EnterTipFragment fragment = new EnterTipFragment();
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
    view = inflater.inflate(R.layout.fragment_enter_tip, container, false);

    enterTipAmount = (EditText) view.findViewById(R.id.EnterTipAmount);
    enterTipContinue = (Button) view.findViewById(R.id.EnterTipContinue);
    enterTipContinue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onContinue(CurrencyUtils.convertToLong(enterTipAmount.getText().toString()));
      }
    });
    enterTipNo = (Button) view.findViewById(R.id.EnterTipNo);
    enterTipNo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onContinue(0L);
      }
    });
    enterTipAmount.setSelection(enterTipAmount.getText().length());
    enterTipAmount.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          enterTipAmount.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

          enterTipAmount.setText(formatted);
          enterTipAmount.setSelection(formatted.length());
          enterTipAmount.addTextChangedListener(this);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });
    return view;
  }

  public void onContinue(long amount) {
      dismiss();
      for( EnterTipDialogFragmentListener listener : listeners){
        listener.onContinue(amount);
      }

  }

  public interface EnterTipDialogFragmentListener {
    public abstract void onContinue(long amount);
  }

  public void addListener(EnterTipDialogFragmentListener listener) {
    listeners.add(listener);
  }

  private EnterTipDialogFragmentListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (EnterTipDialogFragmentListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }
}
