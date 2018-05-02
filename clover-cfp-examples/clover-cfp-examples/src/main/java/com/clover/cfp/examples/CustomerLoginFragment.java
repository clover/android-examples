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

package com.clover.cfp.examples;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerLoginFragment.CustomerLoginFragmentListener} interface
 * to handle interaction events.
 * Use the {@link CustomerLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerLoginFragment extends Fragment implements View.OnClickListener{
  private CustomerLoginFragmentListener mListener;
  private TextView phoneTextField, login;
  private int index;


  public static CustomerLoginFragment newInstance() {
    CustomerLoginFragment fragment = new CustomerLoginFragment();
    return fragment;
  }

  public CustomerLoginFragment() {
    // Required empty public constructor
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }


  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    int orientation = this.getResources().getConfiguration().orientation;
    View view;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      view = inflater.inflate(R.layout.fragment_login_portrait, container, false);
    } else {
      view = inflater.inflate(R.layout.fragment_login_landscape, container, false);
    }
    phoneTextField = (EditText)view.findViewById(R.id.newphoneNumberField);
    phoneTextField.setKeyListener(null);
    initViews(view);

    login = (TextView) view.findViewById(R.id.login_button);
    login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        loginClicked();
      }
    });

    return view;
  }

  private void initViews(View mainView) {
    index = 0;
    mainView.findViewById(R.id.t9_key_0).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_1).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_2).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_3).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_4).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_5).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_6).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_7).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_8).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_9).setOnClickListener(this);
    mainView.findViewById(R.id.t9_key_backspace).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    // handle number button click
    if (v.getTag() != null && "number_button".equals(v.getTag())) {
      addNumber(((TextView) v).getText().toString());
      return;
    }
    switch (v.getId()) {
      case R.id.t9_key_backspace: { // handle backspace button
        // delete one character
        Editable editable = (Editable) phoneTextField.getText();
        int charCount = editable.length();
        if (charCount > 0) {
          deleteNumber();
        }
      }
      break;
    }
  }

  public void addNumber(String num){
    CharSequence current = phoneTextField.getText();
    int phoneMax = 11;
    if(current.length() > 0){
      String first = current.subSequence(0,1).toString();
      Log.d("first", first);
      if(!first.equals("1")){
        phoneMax = 10;
      }
    }
    if(index < phoneMax) {
      phoneTextField.setText(PhoneNumberUtils.formatNumber(current + num));
      index++;
    }
    Log.d("add index : ", String.valueOf(index));
  }

  public void deleteNumber(){
    if(index > 0) {
      CharSequence current = phoneTextField.getText();
      String last = current.subSequence(current.length()-1, current.length()).toString();
      Log.d("last", last);
      if(!last.equals("-")){
        Log.d("deleting ", last);
        index--;
      }
      phoneTextField.setText(PhoneNumberUtils.formatNumber(current.subSequence(0,current.length()-1).toString()));
    }
    Log.d("delete index : ", String.valueOf(index));
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (CustomerLoginFragmentListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnRatingsChangedListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }


  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface CustomerLoginFragmentListener {
     void onNumberEntered(String phoneNumber);
  }

  public void loginClicked() {
    phoneTextField.clearFocus();
    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);
    String phoneNumber = phoneTextField.getText().toString();
    phoneNumber = phoneNumber.replaceAll("\\D", "");
    if (mListener != null) {
      mListener.onNumberEntered(phoneNumber);
    }
  }
}
