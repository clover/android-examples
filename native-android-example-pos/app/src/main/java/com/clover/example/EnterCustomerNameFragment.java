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

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class EnterCustomerNameFragment extends DialogFragment {

  private View view;
  private Button save;
  List<EnterCustomerNameFragment.EnterCustomerNameListener> listeners = new ArrayList<EnterCustomerNameListener>(5);
  private EditText customerName;

  public static EnterCustomerNameFragment newInstance(){
    EnterCustomerNameFragment fragment = new EnterCustomerNameFragment();
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
    view = inflater.inflate(R.layout.fragment_enter_customer_name, container, false);
    customerName = (EditText) view.findViewById(R.id.VaultCardCustomerName);
    save = (Button) view.findViewById(R.id.VaultCardNameSave);
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onContinue();
      }
    });

    return view;
  }

  public void onContinue() {
    String errorMessage = "";
    if(customerName.getText().toString().length() < 1){
      errorMessage = "You must enter a name to be associated with a Vaulted Card";
    }
    if(customerName.getText().toString().length()>0){
      dismissAllowingStateLoss();
      for( EnterCustomerNameFragment.EnterCustomerNameListener listener : listeners){
        listener.onContinue(customerName.getText().toString());
      }
    }
    if(errorMessage.length() > 0){
      ((NativePOSActivity)getActivity()).showPopupMessage(null, new String[] {errorMessage}, false);
    }
  }

  public interface EnterCustomerNameListener {
    public abstract void onContinue(String name);
  }

  public void addListener(EnterCustomerNameFragment.EnterCustomerNameListener listener) {
    listeners.add(listener);
  }

  private EnterCustomerNameFragment.EnterCustomerNameListener mListener;
  // make sure the Activity implemented it

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (EnterCustomerNameFragment.EnterCustomerNameListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }
}
