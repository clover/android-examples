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

import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.example.model.POSStore;
import com.clover.example.utils.CurrencyUtils;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.payments.DataEntryLocation;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class TransactionSettingsFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

  public enum transactionTypes {SALE, AUTH, PREAUTH}
  private transactionTypes type;
  private POSStore store;
  boolean updatingSwitches = false;
  private OnFragmentInteractionListener mListener;
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  List<CurrentOrderFragmentListener> listeners = new ArrayList<CurrentOrderFragmentListener>(5);
  private View view;
  private Switch manualSwitch, swipeSwitch, chipSwitch, contactlessSwitch, printingSwitch, disableReceiptOptionsSwitch, disableDuplicateCheckSwitch, automaticSignatureConfirmationSwitch, automaticPaymentConfirmationSwitch;
  private RadioGroup allowOfflineRG, forceOfflineRG, approveOfflineNoPromptRG, signatureEntryLocationRG;
  private Spinner tipModeSpinner, signatureEntryLocationSpinner;
  private EditText tipAmountText, signatureThresholdText;
  private LinearLayout tips;
  private Button continueButton;
  private TextView settingsLabel;

  public static TransactionSettingsFragment newInstance(POSStore store, transactionTypes type) {
    TransactionSettingsFragment fragment = new TransactionSettingsFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setStore(store);
    fragment.setType(type);
    return fragment;
  }

  public TransactionSettingsFragment(){

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   setStyle(STYLE_NO_TITLE, R.style.CustomDialog);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_transaction_settings, container, false);

    settingsLabel = (TextView) view.findViewById(R.id.TransactionSettingsLabel);
    manualSwitch = ((Switch) view.findViewById(R.id.ManualSwitch));
    swipeSwitch = ((Switch) view.findViewById(R.id.SwipeSwitch));
    chipSwitch = ((Switch) view.findViewById(R.id.ChipSwitch));
    contactlessSwitch = ((Switch) view.findViewById(R.id.ContactlessSwitch));
    allowOfflineRG = (RadioGroup) view.findViewById(R.id.AcceptOfflinePaymentRG);
    forceOfflineRG = (RadioGroup) view.findViewById(R.id.ForceOfflinePaymentRG);
    approveOfflineNoPromptRG = (RadioGroup) view.findViewById(R.id.ApproveOfflineWithoutPromptRG);
    tips = (LinearLayout) view.findViewById(R.id.Tips);
    tipModeSpinner = ((Spinner) view.findViewById(R.id.TipModeSpinner));
    signatureEntryLocationSpinner = ((Spinner) view.findViewById(R.id.SignatureEntryLocationSpinner));
    tipAmountText = ((EditText) view.findViewById(R.id.tipAmount));
    disableReceiptOptionsSwitch = ((Switch) view.findViewById(R.id.DisableReceiptOptionsSwitch));
    disableDuplicateCheckSwitch = ((Switch) view.findViewById(R.id.DisableDuplicateCheckSwitch));
    automaticSignatureConfirmationSwitch = ((Switch) view.findViewById(R.id.AutomaticSignatureConfirmationSwitch));
    automaticPaymentConfirmationSwitch = ((Switch) view.findViewById(R.id.AutomaticPaymentConfirmationSwitch));
    printingSwitch = ((Switch) view.findViewById(R.id.PrintingSwitch));
    signatureThresholdText = ((EditText) view.findViewById(R.id.signatureThreshold));
    continueButton = (Button) view.findViewById(R.id.continueButton);
    manualSwitch.setTag(POSStore.CARD_ENTRY_METHOD_MANUAL);
    swipeSwitch.setTag(POSStore.CARD_ENTRY_METHOD_MAG_STRIPE);
    chipSwitch.setTag(POSStore.CARD_ENTRY_METHOD_ICC_CONTACT);
    contactlessSwitch.setTag(POSStore.CARD_ENTRY_METHOD_NFC_CONTACTLESS);

    signatureThresholdText.setSelection(signatureThresholdText.getText().length());
    signatureThresholdText.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          signatureThresholdText.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

          signatureThresholdText.setText(formatted);
          signatureThresholdText.setSelection(formatted.length());
          signatureThresholdText.addTextChangedListener(this);
          store.setSignatureThreshold(CurrencyUtils.convertToLong(formatted)> 0 ? CurrencyUtils.convertToLong(formatted) : null);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });

    RadioGroup.OnCheckedChangeListener radioGroupChangeListener = new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!updatingSwitches) {
          PaymentConnector cc = (PaymentConnector) cloverConnectorWeakReference.get();
          if (cc == null) {
            Log.e(getClass().getSimpleName(), "Clover Connector reference is null");
            return;
          }
          if (group == allowOfflineRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            Boolean allowOffline = null;
            switch (checkedRadioButtonId) {
              case R.id.acceptOfflineDefault: {
                allowOffline = null;
                break;
              }
              case R.id.acceptOfflineFalse: {
                allowOffline = false;
                break;
              }
              case R.id.acceptOfflineTrue: {
                allowOffline = true;
                break;
              }
            }
            store.setAllowOfflinePayment(allowOffline);
          } else if (group == forceOfflineRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            Boolean forceOffline = null;
            switch (checkedRadioButtonId) {
              case R.id.forceOfflineDefault: {
                forceOffline = null;
                break;
              }
              case R.id.forceOfflineFalse: {
                forceOffline = false;
                break;
              }
              case R.id.forceOfflineTrue: {
                forceOffline = true;
                break;
              }
            }
            store.setForceOfflinePayment(forceOffline);
          } else if (group == approveOfflineNoPromptRG) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            Boolean approveWOPrompt = null;
            switch (checkedRadioButtonId) {
              case R.id.approveOfflineWithoutPromptDefault: {
                approveWOPrompt = null;
                break;
              }
              case R.id.approveOfflineWithoutPromptFalse: {
                approveWOPrompt = false;
                break;
              }
              case R.id.approveOfflineWithoutPromptTrue: {
                approveWOPrompt = true;
                break;
              }
            }
            store.setApproveOfflinePaymentWithoutPrompt(approveWOPrompt);
          }
        }
      }
    };

    CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setCardEntryMethods(getCardEntryMethodStates());
        }
      }
    };

    continueButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onContinue();
      }
    });

    // Defaults for testing sign on paper with no Clover printing or receipt options screen
    // Also allow offline payments without any prompt
    // This setup would be used if you want the most minimal interaction with the mini
    // (i.e. payment only)
    //
    //store.setTipMode(SaleRequest.TipMode.NO_TIP);
    //store.setSignatureEntryLocation(DataEntryLocation.ON_PAPER);
    //store.setCloverHandlesReceipts(false);
    //store.setDisableReceiptOptions(true);
    //store.setDisableDuplicateChecking(true);
    //store.setAllowOfflinePayment(true);
    //store.setApproveOfflinePaymentWithoutPrompt(true);
    //store.setAutomaticSignatureConfirmation(true);
    //store.setAutomaticPaymentConfirmation(true);

    // Defaults for testing sign on screen before payment with Clover printing and receipt options screen
    // Also allow offline payments, but prompt for acceptance
    // This setup would be used if you want the completely automated interaction with the mini
    // (i.e. tip on screen, payment, signature, receipt option and mini printing)
    //
    //store.setTipMode(TipMode.ON_SCREEN_BEFORE_PAYMENT);
    //store.setSignatureEntryLocation(DataEntryLocation.ON_SCREEN);
    //store.setCloverHandlesReceipts(true);
    //store.setDisableReceiptOptions(false);
    //store.setDisableDuplicateChecking(false);
    //store.setAllowOfflinePayment(true);
    //store.setApproveOfflinePaymentWithoutPrompt(false);
    //store.setAutomaticSignatureConfirmation(false);
    //store.setAutomaticPaymentConfirmation(false);

    manualSwitch.setOnCheckedChangeListener(changeListener);
    swipeSwitch.setOnCheckedChangeListener(changeListener);
    chipSwitch.setOnCheckedChangeListener(changeListener);
    contactlessSwitch.setOnCheckedChangeListener(changeListener);

    allowOfflineRG.setOnCheckedChangeListener(radioGroupChangeListener);
    forceOfflineRG.setOnCheckedChangeListener(radioGroupChangeListener);
    approveOfflineNoPromptRG.setOnCheckedChangeListener(radioGroupChangeListener);

    ArrayList<String> values = new ArrayList<>();

    values.add(0, "DEFAULT");
    int i = 1;
    for (POSStore.TipMode tipMode : POSStore.TipMode.values()) {
      values.add(i, tipMode.toString());
      i++;
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
        R.layout.spinner_dropdown_item, values);
    tipModeSpinner.setAdapter(adapter);
    tipModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        POSStore.TipMode tipMode = getSelectedTipMode(position);
        if (tipMode != null) {
          store.setTipMode(tipMode);
        } else {
          store.setTipMode(null);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        store.setTipMode(null);
      }
    });

    tipAmountText.setSelection(tipAmountText.getText().length());
    tipAmountText.addTextChangedListener(new TextWatcher(){
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String current = "";
        if(!s.toString().equals(current)){
          tipAmountText.removeTextChangedListener(this);

          String cleanString = s.toString().replaceAll("[$,.]", "");

          double parsed = Double.parseDouble(cleanString);
          String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

          tipAmountText.setText(formatted);
          tipAmountText.setSelection(formatted.length());
          tipAmountText.addTextChangedListener(this);
          store.setTipAmount(CurrencyUtils.convertToLong(formatted)> 0 ? CurrencyUtils.convertToLong(formatted) : null);
        }
      }
      @Override
      public void afterTextChanged(Editable arg0) { }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });

    ArrayList<String> sigValues = new ArrayList<>();

    sigValues.add(0, "DEFAULT");
    int x = 1;
    for (DataEntryLocation sigLoc : DataEntryLocation.values()) {
      sigValues.add(x, sigLoc.toString());
      x++;
    }

    ArrayAdapter<String> sigAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
        R.layout.spinner_dropdown_item, sigValues);
    signatureEntryLocationSpinner.setAdapter(sigAdapter);
    signatureEntryLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DataEntryLocation dataEntryLocation = getSelectedSignatureEntryLocation(position);
        if (dataEntryLocation != null) {
          store.setSignatureEntryLocation(getSelectedSignatureEntryLocation(position));
        } else {
          store.setSignatureEntryLocation(null);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        store.setSignatureEntryLocation(null);
      }
    });
    disableReceiptOptionsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setDisableReceiptOptions(isChecked);
        }
      }
    });

    disableDuplicateCheckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setDisableDuplicateChecking(isChecked);
        }
      }
    });

    printingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setDisablePrinting(isChecked);
        }
      }
    });
    automaticSignatureConfirmationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setAutomaticSignatureConfirmation(isChecked);
        }
      }
    });
    automaticPaymentConfirmationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!updatingSwitches) {
          store.setAutomaticPaymentConfirmation(isChecked);
        }
      }
    });

    if(type != transactionTypes.SALE){
      tips.setVisibility(View.GONE);
    }

    if(type == transactionTypes.AUTH){
      settingsLabel.setText("Auth Settings");
    }
    if(type == transactionTypes.PREAUTH){
      settingsLabel.setText("PreAuth Settings");
    }

    updateSwitches(view);
    return view;
  }

  private int getCardEntryMethodStates() {
    int val = 0;
    val |= manualSwitch.isChecked() ? (Integer) manualSwitch.getTag() : 0;
    val |= swipeSwitch.isChecked() ? (Integer) swipeSwitch.getTag() : 0;
    val |= chipSwitch.isChecked() ? (Integer) chipSwitch.getTag() : 0;
    val |= contactlessSwitch.isChecked() ? (Integer) contactlessSwitch.getTag() : 0;

    return val;
  }
  private POSStore.TipMode getSelectedTipMode(int position) {
    String tipModeString = tipModeSpinner.getItemAtPosition(position).toString();
    return getTipModeFromString(tipModeString);
  }

  private POSStore.TipMode getTipModeFromString(String tipModeString) {
    for (POSStore.TipMode tipMode : POSStore.TipMode.values()) {
      if (tipMode.toString().equals(tipModeString)) {
        return tipMode;
      }
    }
    return null;
  }

  private int getTipModePositionFromString(String value) {
    for (int i = 0; i < tipModeSpinner.getAdapter().getCount(); i++) {
      if (tipModeSpinner.getItemAtPosition(i).toString().equals(value)) {
        return i;
      }
    }
    return -1;
  }

  private DataEntryLocation getSelectedSignatureEntryLocation(int position) {
    String sigLocationEntryLocationString = signatureEntryLocationSpinner.getItemAtPosition(position).toString();
    return getSignatureEntryLocationFromString(sigLocationEntryLocationString);
  }

  private DataEntryLocation getSignatureEntryLocationFromString(String sigEntryLocationString) {
    for (DataEntryLocation dataEntryLocation : DataEntryLocation.values()) {
      if (dataEntryLocation.toString().equals(sigEntryLocationString)) {
        return dataEntryLocation;
      }
    }
    return null;
  }

  private int getSignatureEntryLocationPositionFromString(String value) {
    for (int i = 0; i < signatureEntryLocationSpinner.getAdapter().getCount(); i++) {
      if (signatureEntryLocationSpinner.getItemAtPosition(i).toString().equals(value)) {
        return i;
      }
    }
    return -1;
  }

  private void updateSwitches(View view) {
    if (manualSwitch != null) {

      updatingSwitches = true;
      PaymentConnector cc = (PaymentConnector) cloverConnectorWeakReference.get();
      if (cc == null) {
        Log.e(getClass().getSimpleName(), "Clover Connector Weak Reference is null");
        return;
      }
      manualSwitch.setChecked((store.getCardEntryMethods() & POSStore.CARD_ENTRY_METHOD_MANUAL) == POSStore.CARD_ENTRY_METHOD_MANUAL);
      contactlessSwitch.setChecked((store.getCardEntryMethods() & POSStore.CARD_ENTRY_METHOD_NFC_CONTACTLESS) == POSStore.CARD_ENTRY_METHOD_NFC_CONTACTLESS);
      chipSwitch.setChecked((store.getCardEntryMethods() & POSStore.CARD_ENTRY_METHOD_ICC_CONTACT) == POSStore.CARD_ENTRY_METHOD_ICC_CONTACT);
      swipeSwitch.setChecked((store.getCardEntryMethods() & POSStore.CARD_ENTRY_METHOD_MAG_STRIPE) == POSStore.CARD_ENTRY_METHOD_MAG_STRIPE);

      printingSwitch.setChecked(store.getDisablePrinting() != null ? store.getDisablePrinting() : false);
      disableReceiptOptionsSwitch.setChecked(store.getDisableReceiptOptions() != null ? store.getDisableReceiptOptions() : false);
      disableDuplicateCheckSwitch.setChecked(store.getDisableDuplicateChecking() != null ? store.getDisableDuplicateChecking() : false);
      automaticSignatureConfirmationSwitch.setChecked(store.getAutomaticSignatureConfirmation() != null ? store.getAutomaticSignatureConfirmation() : false);
      automaticPaymentConfirmationSwitch.setChecked(store.getAutomaticPaymentConfirmation() != null ? store.getAutomaticPaymentConfirmation() : false);
      if (store.getTipMode() != null && getTipModePositionFromString(store.getTipMode().toString()) != -1) {
        tipModeSpinner.setSelection(getTipModePositionFromString(store.getTipMode().toString()));
      }
      if (store.getSignatureEntryLocation() != null && getSignatureEntryLocationPositionFromString(store.getSignatureEntryLocation().toString()) != -1) {
        signatureEntryLocationSpinner.setSelection(getSignatureEntryLocationPositionFromString(store.getSignatureEntryLocation().toString()));
      }

      Boolean allowOfflinePayment = store.getAllowOfflinePayment();
      ((RadioButton) view.findViewById(R.id.acceptOfflineDefault)).setChecked(allowOfflinePayment == null);
      ((RadioButton) view.findViewById(R.id.acceptOfflineTrue)).setChecked(allowOfflinePayment != null && allowOfflinePayment);
      ((RadioButton) view.findViewById(R.id.acceptOfflineFalse)).setChecked(allowOfflinePayment != null && !allowOfflinePayment);
      Boolean forceOfflinePayment = store.getForceOfflinePayment();
      ((RadioButton) view.findViewById(R.id.forceOfflineDefault)).setChecked(forceOfflinePayment == null);
      ((RadioButton) view.findViewById(R.id.forceOfflineTrue)).setChecked(forceOfflinePayment != null && forceOfflinePayment);
      ((RadioButton) view.findViewById(R.id.forceOfflineFalse)).setChecked(forceOfflinePayment != null && !forceOfflinePayment);
      Boolean approveOfflinePaymentWithoutPrompt = store.getApproveOfflinePaymentWithoutPrompt();
      ((RadioButton) view.findViewById(R.id.approveOfflineWithoutPromptDefault)).setChecked(approveOfflinePaymentWithoutPrompt == null);
      ((RadioButton) view.findViewById(R.id.approveOfflineWithoutPromptTrue)).setChecked(approveOfflinePaymentWithoutPrompt != null && allowOfflinePayment);
      ((RadioButton) view.findViewById(R.id.approveOfflineWithoutPromptFalse)).setChecked(approveOfflinePaymentWithoutPrompt != null && !allowOfflinePayment);
      Long signatureThreshold = store.getSignatureThreshold();
      if (signatureThreshold != null) {
        ((EditText) view.findViewById(R.id.signatureThreshold)).setText(signatureThreshold.toString());
      } else {
        ((EditText) view.findViewById(R.id.signatureThreshold)).setText("$0.00");
      }
      InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
      updatingSwitches = false;
    }

  }

  public Runnable continueAction;

  public void onContinue (){
    continueAction.run();
    dismiss();
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setStore(POSStore store) {
    this.store = store;
  }

  public void setType(transactionTypes transactionType){
    type = transactionType;
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }

  public void setWeakCloverConnector(WeakReference<IPaymentConnector> cloverConnector){
    cloverConnectorWeakReference = cloverConnector;
  }

}
