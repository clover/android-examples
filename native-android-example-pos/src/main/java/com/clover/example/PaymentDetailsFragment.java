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

import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.TipAdjustAuthRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PaymentDetailsFragment extends Fragment implements AdjustTipFragment.AdjustTipFragmentListener, RefundPaymentFragment.PaymentRefundListener{
  private View view;
  private TextView title, transactionTitle, date, total, paymentStatus, tender, cardDetails, employee, deviceId, paymentId, entryMethod,
      transactionState, transactionType, absoluteTotal, tip, refundDate, refundTotal, refundTender, refundEmployee, refundDevice, refundId;
  private LinearLayout tipRow, refundRow, paymentSuccessfulRow;
  private ImageView paymentStatusImage;
  private Button refund, voidPayment, addTip;
  private POSTransaction transaction;
  private POSStore store;
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
  private DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

  public static PaymentDetailsFragment newInstance(POSTransaction transaction, IPaymentConnector cloverConnector, POSStore store) {
    PaymentDetailsFragment fragment = new PaymentDetailsFragment();
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);
    fragment.setTransaction(transaction);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public PaymentDetailsFragment(){

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_payment_details, container, false);

    title = (TextView) view.findViewById(R.id.PaymentDetailsTitle);
    transactionTitle = (TextView) view.findViewById(R.id.PaymentDetailsTransactionTitle);
    date = (TextView) view.findViewById(R.id.PaymentDetailsDate);
    total = (TextView) view.findViewById(R.id.PaymentDetailsTotal);
    paymentStatusImage = (ImageView) view.findViewById(R.id.PaymentDetailsPaymentStatusImage);
    paymentStatus = (TextView) view.findViewById(R.id.PaymentDetailsStatus);
    tender = (TextView) view.findViewById(R.id.PaymentDetailsTender);
    cardDetails = (TextView) view.findViewById(R.id.PaymentDetailsCardDetails);
    employee = (TextView) view.findViewById(R.id.PaymentDetailsEmployee);
    deviceId = (TextView) view.findViewById(R.id.PaymentDetailsDeviceId);
    paymentId = (TextView) view.findViewById(R.id.PaymentDetailsPaymentId);
    entryMethod = (TextView) view.findViewById(R.id.PaymentDetailsEntryMethod);
    transactionType = (TextView) view.findViewById(R.id.PaymentDetailsTransactionType);
    transactionState = (TextView) view.findViewById(R.id.PaymentDetailsTransactionState);
    absoluteTotal = (TextView) view.findViewById(R.id.PaymentDetailsAbsoluteTotal);
    tipRow = (LinearLayout) view.findViewById(R.id.PaymentDetailsTipRow);
    tip = (TextView) view.findViewById(R.id.PaymentDetailsTip);
    refundRow = (LinearLayout) view.findViewById(R.id.PaymentDetailsRefundRow);
    paymentSuccessfulRow = (LinearLayout) view.findViewById(R.id.PaymentDetailsPaymentSuccessfulRow);
    refundTotal = (TextView) view.findViewById(R.id.PaymentDetailsRefundTotal);
    refundTender = (TextView) view.findViewById(R.id.PaymentDetailsRefundTender);
    refundEmployee = (TextView) view.findViewById(R.id.PaymentDetailsRefundEmployee);
    refundDevice = (TextView) view.findViewById(R.id.PaymentDetailsRefundDeviceId);
    refundId = (TextView) view.findViewById(R.id.PaymentDetailsRefundId);
    refundDate = (TextView) view.findViewById(R.id.PaymentDetailsRefundDate);

    populateFields();
    return view;
  }

  private void populateFields(){
    refund = (Button) view.findViewById(R.id.PaymentDetailsRefund);
    refund.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(transaction.getTransactionTitle() == "Payment"){
          showRefundPaymentDialog();
        }
        else{
          makeFullRefund();
        }
      }
    });
    voidPayment = (Button) view.findViewById(R.id.PaymentDetailsVoidPayment);
    voidPayment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        voidPayment();
      }
    });
    addTip = (Button) view.findViewById(R.id.PaymentDetailsAddTip);
    addTip.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAdjustTipDialog();
      }
    });

    transactionTitle.setText(transaction.getTransactionTitle());
    date.setText(dateFormat.format(transaction.getDate())+" • "+timeFormat.format(transaction.getDate()));
    total.setText(CurrencyUtils.convertToString(transaction.getAmount()));
    tender.setText(transaction.getTender());
    cardDetails.setText(transaction.getCardDetails());
    employee.setText(transaction.getEmployee());
    deviceId.setText(transaction.getDeviceId());
    paymentId.setText(transaction.getId());
    entryMethod.setText(transaction.getEntryMethod().toString());
    enableView(refund);
    enableView(addTip);
    enableView(voidPayment);
    if(transaction.getTransactionType() != null) {
      transactionType.setText(transaction.getTransactionType().toString());
    }
    if(transaction.getTransactionTitle() == "PreAuth"){
      disableView(refund);
    }
    if(!transaction.getRefund()){
      title.setText("Payment Details");
      paymentStatusImage.setImageResource(R.drawable.status_green);
      paymentStatus.setText("Payment Successful");
      if(transaction.getTransactionTitle() == "Auth"){
        addTip.setVisibility(View.VISIBLE);
      }
      if (((POSPayment) transaction).getTipAmount() != 0) {
        addTip.setText("Adjust Tip");
        tipRow.setVisibility(View.VISIBLE);
        tip.setText(CurrencyUtils.convertToString(((POSPayment) transaction).getTipAmount()));
      }
      else{
        tipRow.setVisibility(View.GONE);
        tip.setText("");
      }
      if (((POSPayment) transaction).getPaymentStatus() == POSPayment.Status.VOIDED) {
        disableView(refund);
        disableView(addTip);
        disableView(voidPayment);
        transactionType.setText("VOIDED");
      }
      if (((POSPayment) transaction).getRefundId() != null) {
        addRefund(transaction, transaction.getAmount());
      }
      else{
        refundRow.setVisibility(View.GONE);
      }
      absoluteTotal.setText(CurrencyUtils.convertToString(transaction.getAmount()+((POSPayment)transaction).getTipAmount()));
    }
    else{
      title.setText("Manual Refund Details");
      absoluteTotal.setText(CurrencyUtils.convertToString(transaction.getAmount()));
      disableView(refund);
      disableView(voidPayment);
      disableView(addTip);
      tipRow.setVisibility(View.GONE);
      refundRow.setVisibility(View.GONE);
      paymentSuccessfulRow.setVisibility(View.GONE);

    }
  }

  public void disableView (View view){
    view.setEnabled(false);
    view.setAlpha((float)0.4);
  }

  public void enableView (View view){
    view.setEnabled(true);
    view.setAlpha(1);
  }

  private void showAdjustTipDialog() {
    FragmentManager fm = getFragmentManager();
    AdjustTipFragment adjustTipFragment = AdjustTipFragment.newInstance(((POSPayment)transaction).getTipAmount());
    adjustTipFragment.addListener(this);
    adjustTipFragment.show(fm, "fragment_enter_payment_id");
  }

  public void setTransaction (POSTransaction posTransaction){
    this.transaction = posTransaction;
    if(view != null) {
      populateFields();
    }
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }

  public void setStore(POSStore posStore){
    this.store = posStore;
  }

  public void voidPayment(){
    VoidPaymentRequest vpr = new VoidPaymentRequest();
    vpr.setPaymentId(transaction.getId());
    vpr.setOrderId(((POSPayment)transaction).getCloverOrderId());
    vpr.setVoidReason("USER_CANCEL");
    final IPaymentConnector cloverConnector = cloverConnectorWeakReference.get();
    cloverConnector.voidPayment(vpr);
  }


  public void paymentVoided (final POSTransaction posTransaction){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        transaction = posTransaction;
        transactionType.setText("VOIDED");
        paymentId.setText("");
        disableView(refund);
        disableView(voidPayment);
        disableView(addTip);
      }
    });
  }


  public void setTip(final long tipAmount){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if(tipAmount > 0){
          tipRow.setVisibility(View.VISIBLE);
          tip.setText(CurrencyUtils.convertToString(tipAmount));
          addTip.setText("Adjust Tip");
          absoluteTotal.setText(CurrencyUtils.convertToString(transaction.getAmount() + ((POSPayment)transaction).getTipAmount()));
        }
      }
    });
  }

  public void addRefund(final POSTransaction posTransaction, final long amount){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        transaction = posTransaction;
        POSOrder order = store.getOrderByCloverPaymentId(transaction.getId());
        for (POSTransaction _transaction : order.getPayments()) {
          if (_transaction instanceof POSRefund) {
            refundDate.setText(dateFormat.format(_transaction.getDate())+" • "+timeFormat.format(_transaction.getDate()));
          }
        }
        refundRow.setVisibility(View.VISIBLE);
        refundTotal.setText(CurrencyUtils.convertToString(amount));
        refundTender.setText(transaction.getTender());
        refundEmployee.setText(transaction.getEmployee());
        refundDevice.setText(transaction.getDeviceId());
        refundId.setText(((POSPayment)transaction).getRefundId());
        disableView(refund);
        disableView(voidPayment);
        disableView(addTip);
      }
    });
  }

  private void showRefundPaymentDialog(){
    FragmentManager fm = getFragmentManager();
    RefundPaymentFragment refundPaymentFragment = RefundPaymentFragment.newInstance(transaction.getAmount());
    refundPaymentFragment.addListener(this);
    refundPaymentFragment.show(fm, "fragment_refund_payment");
  }


  @Override
  public void onSave(long tipAmount) {
    TipAdjustAuthRequest taar = new TipAdjustAuthRequest();
    taar.setPaymentId(transaction.getId());
    taar.setOrderId(((POSPayment)transaction).getCloverOrderId());
    taar.setTipAmount(tipAmount);
    final IPaymentConnector paymentConnector = cloverConnectorWeakReference.get();
    paymentConnector.tipAdjustAuth(taar);
  }

  @Override
  public void makePartialRefund(long amount) {
    RefundPaymentRequest refund = new RefundPaymentRequest();
    refund.setAmount(amount);
    refund.setPaymentId(transaction.getId());
    refund.setOrderId(((POSPayment)transaction).getCloverOrderId());
    refund.setFullRefund(false);
    final IPaymentConnector cloverConnector = cloverConnectorWeakReference.get();
    cloverConnector.refundPayment(refund);
  }

  @Override
  public void makeFullRefund() {
    RefundPaymentRequest refund = new RefundPaymentRequest();
    refund.setAmount(transaction.getAmount());
    refund.setPaymentId(transaction.getId());
    refund.setOrderId(((POSPayment)transaction).getCloverOrderId());
    refund.setFullRefund(true);
    final IPaymentConnector cloverConnector = cloverConnectorWeakReference.get();
    cloverConnector.refundPayment(refund);
  }
}
