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

public class PreAuthDialogFragment extends DialogFragment {

  private View view;
  private Button preauthContinue;
  List<PreAuthDialogFragmentListener> listeners = new ArrayList<PreAuthDialogFragmentListener>(5);
  private EditText preauthName, preauthAmount;

  public static PreAuthDialogFragment newInstance(){
    PreAuthDialogFragment fragment = new PreAuthDialogFragment();
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
    view = inflater.inflate(R.layout.fragment_preauth_dialog, container, false);
    preauthName = (EditText) view.findViewById(R.id.PreauthName);
    preauthAmount = (EditText) view.findViewById(R.id.PreauthAmount);
    preauthContinue = (Button) view.findViewById(R.id.PreauthContinue);
    preauthContinue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onContinue();
      }
    });

    preauthAmount.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          preauthAmount.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

          current = formatted;
          preauthAmount.setText(formatted);
          preauthAmount.setSelection(formatted.length());

          preauthAmount.addTextChangedListener(this);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });
    return view;
  }

  public void onContinue() {
    List<String> errorMessage = new ArrayList<String>();
    if(preauthName.getText().toString().length() < 1){
      errorMessage.add("You must enter a name to be associated with PreAuth");
    }
    if(CurrencyUtils.convertToLong(preauthAmount.getText().toString()) <= 0L){
      errorMessage.add("You must have a PreAuth amount greater than $0.00");
    }
    if(preauthName.getText().toString().length()>1 && CurrencyUtils.convertToLong(preauthAmount.getText().toString()) > 0L){
      dismissAllowingStateLoss();
      for( PreAuthDialogFragmentListener listener : listeners){
        listener.onContinue(preauthName.getText().toString(), preauthAmount.getText().toString());
      }
    }
    if(errorMessage.size() > 0){
      String[] errorArray = new String[errorMessage.size()];
      ((NativePOSActivity)getActivity()).showPopupMessage(null, errorMessage.toArray(errorArray), false);
    }
  }

  public interface PreAuthDialogFragmentListener {
    public abstract void onContinue(String name, String amount);
  }

  public void addListener(PreAuthDialogFragmentListener listener) {
    listeners.add(listener);
  }

  private PreAuthDialogFragmentListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (PreAuthDialogFragmentListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }
}
