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

import com.clover.example.model.POSTransaction;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IDisplayConnector;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.example.adapter.AvailableItemsAdapter;
import com.clover.example.model.OrderObserver;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSDiscount;
import com.clover.example.model.POSItem;
import com.clover.example.model.POSLineItem;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.StoreObserver;
import com.clover.example.utils.CurrencyUtils;
import com.clover.example.utils.IdUtils;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.order.DisplayDiscount;
import com.clover.sdk.v3.order.DisplayLineItem;
import com.clover.sdk.v3.order.DisplayOrder;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterFragment extends Fragment implements CurrentOrderFragmentListener, AvailableItemListener,
    PreAuthDialogFragment.PreAuthDialogFragmentListener{
  private OnFragmentInteractionListener mListener;
  private static final String TAG = RegisterFragment.class.getSimpleName();
  private View view;

  POSStore store;
  private WeakReference<IPaymentConnector> paymentConnectorWeakReference;
  IDisplayConnector displayConnector;
  boolean preAuth = false;
  boolean vaulted = false;
  POSCard vaultedCard;
  Map<POSItem, AvailableItem> itemToAvailableItem = new HashMap<POSItem, AvailableItem>();
  DisplayOrder currentDisplayOrder;

  public static RegisterFragment newInstance(POSStore store, IPaymentConnector paymentConnector, IDisplayConnector displayConnector) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setPaymentConnector(paymentConnector);
    fragment.setDisplayConnector(displayConnector);
    fragment.setPreAuth(false);
    fragment.setVaulted(false);
    fragment.setVaultedCard(null);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public static RegisterFragment newInstance(POSStore store, IPaymentConnector cloverConnector, IDisplayConnector displayConnector, boolean preauth) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setPaymentConnector(cloverConnector);
    fragment.setDisplayConnector(displayConnector);
    fragment.setPreAuth(preauth);
    fragment.setVaulted(false);
    fragment.setVaultedCard(null);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public static RegisterFragment newInstance(POSStore store, IPaymentConnector cloverConnector, IDisplayConnector displayConnector, boolean vaulted, POSCard vaultedCard) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setPaymentConnector(cloverConnector);
    fragment.setDisplayConnector(displayConnector);
    fragment.setVaulted(vaulted);
    fragment.setVaultedCard(vaultedCard);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }


  public RegisterFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_register, container, false);
    // Inflate the layout for this fragment

    GridView gv = (GridView)view.findViewById(R.id.AvailableItems);

    final AvailableItemsAdapter availableItemsAdapter = new AvailableItemsAdapter(view.getContext(), R.id.AvailableItems, new ArrayList<POSItem>(store.getAvailableItems()), store);
    gv.setAdapter(availableItemsAdapter);

    gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        POSItem item = availableItemsAdapter.getItem(position);
        onItemSelected(item);
      }
    });

    gv.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

      int lastFirstVisibleItem = -1;
      @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem != lastFirstVisibleItem) {
          availableItemsAdapter.notifyDataSetChanged();
          lastFirstVisibleItem = firstVisibleItem;
        }
      }
    });

    if(getActivity().getResources().getBoolean(R.bool.isFlex)) {
      Button reviewOrder = (Button) view.findViewById(R.id.ReviewOrderButton);
      reviewOrder.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentManager fragmentManager = getFragmentManager();
          FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
          CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentByTag("CURRENT_ORDER"));
          if (currentOrderFragment == null) {
            currentOrderFragment = CurrentOrderFragment.newInstance(preAuth);
            currentOrderFragment.setOrder(store.getCurrentOrder());
            currentOrderFragment.setStore(store);
            currentOrderFragment.setVaulted(vaulted);
            currentOrderFragment.setVaultedCard(vaultedCard);
            currentOrderFragment.addListener(RegisterFragment.this);
            currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
            fragmentTransaction.add(R.id.contentContainer, currentOrderFragment, "CURRENT_ORDER");
          }
          else {
            currentOrderFragment.setOrder(store.getCurrentOrder());
            currentOrderFragment.setStore(store);
            currentOrderFragment.setVaulted(vaulted);
            currentOrderFragment.setVaultedCard(vaultedCard);
            currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
            fragmentTransaction.show(currentOrderFragment);
          }
          if(preAuth) {
            showPreAuthDialog();
            currentOrderFragment.setPreAuth(true);
          }
          fragmentTransaction.addToBackStack("CURRENT_ORDER");
          fragmentTransaction.commitAllowingStateLoss();
        }
      });
    }
    else {
      final CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      currentOrderFragment.setOrder(store.getCurrentOrder());
      currentOrderFragment.setStore(store);
      currentOrderFragment.addListener(this);
      currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
      if(preAuth) {
        showPreAuthDialog();
        currentOrderFragment.setPreAuth(true);
      }
      if(vaulted){
        currentOrderFragment.setVaulted(true);
        currentOrderFragment.setVaultedCard(vaultedCard);
      }
    }
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (currentDisplayOrder != null && currentDisplayOrder.hasLineItems() && !currentDisplayOrder.getLineItems().isEmpty()) {
      displayConnector.showDisplayOrder(currentDisplayOrder);
    } else {
      displayConnector.showWelcomeScreen();
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {

      throw new ClassCastException(activity.toString()
                                   + " must implement OnFragmentInteractionListener: " + activity.getClass().getName());
    }
  }

  private void showPreAuthDialog () {
    new Handler().post(new Runnable() {
      public void run() {
        FragmentManager fm = getFragmentManager();
        PreAuthDialogFragment preAuthDialogFragment = PreAuthDialogFragment.newInstance();
        preAuthDialogFragment.addListener(RegisterFragment.this);
        preAuthDialogFragment.show(fm, "fragment_preauth_dialog");
      }
    });
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    CurrentOrderFragment f = (CurrentOrderFragment) getFragmentManager()
        .findFragmentById(R.id.PendingOrder);
    if (f != null)
      getFragmentManager().beginTransaction().remove(f).commit();
  }

  @Override
  public void onContinue(String name, String amount) {
    Log.d("RegisterFragment", name + amount);
    TextView preAuthName = (TextView) getActivity().findViewById(R.id.PreAuthName);
    TextView preAuthAmount = (TextView) getActivity().findViewById(R.id.PreAuthAmount);
    preAuthName.setText("Name: "+name);
    preAuthAmount.setText("Amount: "+amount);
    makePreAuth(amount);
  }

  public void makePreAuth(String amount){
    Long preauthAmount = CurrencyUtils.convertToLong(amount);
    PreAuthRequest request = new PreAuthRequest();
    request.setAmount(preauthAmount);
    request.setExternalId(IdUtils.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    final IPaymentConnector cloverConnector = paymentConnectorWeakReference.get();
    Log.d("setPaymentStatus: ", request.toString());
    cloverConnector.preAuth(request);
  }

  public void clearPreAuth(){
    preAuth = false;
    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.PreAuthInfo);
    layout.setVisibility(View.GONE);
    TextView preAuthName = (TextView) getActivity().findViewById(R.id.PreAuthName);
    TextView preAuthAmount = (TextView) getActivity().findViewById(R.id.PreAuthAmount);
    preAuthName.setText("Name: ");
    preAuthAmount.setText("Amount: ");
  }


  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public POSStore getStore() {
    return store;
  }

  public void setStore(POSStore store) {
    this.store = store;

    RegisterObserver observer = new RegisterObserver();
    store.addStoreObserver(observer);
    store.addCurrentOrderObserver(observer);
  }

  public void setPaymentConnector(IPaymentConnector paymentConnector) {
    paymentConnectorWeakReference = new WeakReference<>(paymentConnector);
  }

  public void setDisplayConnector (IDisplayConnector displayConnector){
    this.displayConnector = displayConnector;
  }

  public void setPreAuth (boolean value){
    preAuth = value;

    if (view != null && getActivity().getResources().getBoolean(R.bool.isFlex) == false) {
      if(preAuth) {
        showPreAuthDialog();
      }
      CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      currentOrderFragment.setOrder(store.getCurrentOrder());
      currentOrderFragment.setStore(store);
      currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
      currentOrderFragment.setPreAuth(preAuth);
    }
  }

  public void setVaulted(boolean value){
    vaulted = value;
  }

  public void setVaultedCard(POSCard vaultedCard) {
    this.vaultedCard = vaultedCard;
    if (view != null && getActivity().getResources().getBoolean(R.bool.isFlex) == false) {
      CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      currentOrderFragment.setOrder(store.getCurrentOrder());
      currentOrderFragment.setStore(store);
      currentOrderFragment.setVaulted(vaulted);
      currentOrderFragment.setVaultedCard(vaultedCard);
      currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
    }
  }

  @Override
  public void onSaleClicked() {
    SaleRequest request = new SaleRequest();
    request.setAmount(store.getCurrentOrder().getTotal());
    request.setExternalId(IdUtils.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setForceOfflinePayment(store.getForceOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    if (store.getTipMode() != null && !store.getTipMode().toString().equals("DEFAULT")) {
      TipMode tipMode = TipMode.valueOf(store.getTipMode().toString());
      request.setTipMode(tipMode);
    }
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    if(store.getTipMode() == POSStore.TipMode.TIP_PROVIDED) {
      request.setTipAmount(store.getTipAmount());
    }
    else{
      request.setTipAmount(null);
    }
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    final IPaymentConnector paymentConnector = paymentConnectorWeakReference.get();
    paymentConnector.sale(request);
  }

  @Override
  public void onNewOrderClicked() {
    clearPreAuth();
    store.createOrder(true);
    CurrentOrderFragment currentOrderFragment;
    if(getActivity().getResources().getBoolean(R.bool.isFlex)) {
      currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentByTag("CURRENT_ORDER");
    }
    else {
      currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
    }

    currentOrderFragment.setOrder(store.getCurrentOrder());
  }

  @Override
  public void onAuthClicked() {
    AuthRequest request = new AuthRequest();
    request.setAmount(store.getCurrentOrder().getTotal());
    request.setExternalId(IdUtils.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setForceOfflinePayment(store.getForceOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    final IPaymentConnector cloverConnector = paymentConnectorWeakReference.get();
    cloverConnector.auth(request);
  }

  @Override
  public void onSelectLineItem() {
    //
  }

  @Override
  public void payWithPreAuth(long tipAmount) {
    CapturePreAuthRequest car = new CapturePreAuthRequest();
    car.setPaymentId(store.getCurrentOrder().getPreAuth().getId());
    car.setAmount(store.getCurrentOrder().getTotal());
    car.setTipAmount(tipAmount);
    paymentConnectorWeakReference.get().capturePreAuth(car);
  }

  @Override
  public void onItemSelected(POSItem item) {
    store.getCurrentOrder().addItem(item, 1);

  }

  class RegisterObserver implements StoreObserver, OrderObserver {
    DisplayOrder displayOrder = new DisplayOrder();
    Map<POSLineItem, DisplayLineItem> liToDli = new HashMap<POSLineItem, DisplayLineItem>();

    public RegisterObserver() {
      displayOrder.setLineItems(Collections.EMPTY_LIST);
      currentDisplayOrder = displayOrder;
    }

    @Override
    public void onCurrentOrderChanged(POSOrder currentOrder) {
      CurrentOrderFragment currentOrderFragment;
      if(getActivity().getResources().getBoolean(R.bool.isFlex)) {
        currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentByTag("CURRENT_ORDER"));
        TextView total = (TextView) view.findViewById(R.id.RegisterTotal);
        total.setText(CurrencyUtils.format(currentOrder.getTotal(), Locale.getDefault()));
        currentOrder.addOrderObserver(this);
      }
      else {
        currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      }
      currentOrderFragment.setOrder(currentOrder);
      currentOrderFragment.setCloverConnector(paymentConnectorWeakReference.get());
    }

    @Override
    public void newOrderCreated(POSOrder order, boolean userInitiated) {
      displayConnector.showWelcomeScreen();
      liToDli.clear();
      displayOrder = new DisplayOrder();
      displayOrder.setLineItems(Collections.EMPTY_LIST);
      currentDisplayOrder = displayOrder;
      updateTotals(order, displayOrder);
    }

    @Override
    public void cardAdded(POSCard card) {

    }

    @Override public void refundAdded(POSTransaction refund) {

    }

    @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

    }

    @Override
    public void transactionsChanged(List<POSTransaction> transactions) {

    }

    @Override
    public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = new DisplayLineItem();
      dli.setId(lineItem.getId());
      dli.setName(lineItem.getItem().getName());
      dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }
      liToDli.put(lineItem, dli);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      items.addAll(displayOrder.getLineItems());
      items.add(dli);
      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      displayConnector.showDisplayOrder(displayOrder);
    }

    @Override
    public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }

      liToDli.remove(lineItem);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      for (DisplayLineItem dlItem : displayOrder.getLineItems()) {
        if (!dlItem.getId().equals(dli.getId())) {
          items.add(dlItem);
        }
      }

      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      displayConnector.showDisplayOrder(displayOrder);
    }

    @Override
    public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      if(dli != null) {
        dli.setName(lineItem.getItem().getName());
        dli.setQuantity("" + lineItem.getQuantity());
        dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));

        List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
        if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
          DisplayDiscount dd = new DisplayDiscount();
          dd.setName(lineItem.getDiscount().name);
          dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
        }
        dli.setDiscounts(dDiscounts);
      }
      updateTotals(posOrder, displayOrder);
      displayConnector.showDisplayOrder(displayOrder);

    }

    private void updateTotals(POSOrder order, DisplayOrder displayOrder) {
      displayOrder.setTax(CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault()));
      displayOrder.setSubtotal(CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault()));
      displayOrder.setTotal(CurrencyUtils.format(order.getTotal(), Locale.getDefault()));
      if(getActivity() != null) {
        if (getActivity().getResources().getBoolean(R.bool.isFlex)) {
          TextView total = (TextView) view.findViewById(R.id.RegisterTotal);
          total.setText(CurrencyUtils.format(order.getTotal(), Locale.getDefault()));
        }
      }

      POSDiscount discount = order.getDiscount();
      List<DisplayDiscount> displayDiscounts = null;
      if (discount != null && discount.getValue(1000) != 0) {
        displayDiscounts = new ArrayList<DisplayDiscount>();
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(discount.getName());
        dd.setAmount("" + discount.getValue(order.getPreDiscountSubTotal()));
        displayDiscounts.add(dd);
      }
      displayOrder.setDiscounts(displayDiscounts);
      if (displayOrder.hasLineItems() && !displayOrder.getLineItems().isEmpty())
        displayConnector.showDisplayOrder(displayOrder);
    }

    @Override
    public void paymentAdded(POSOrder posOrder, POSPayment payment) {

    }

    @Override
    public void refundAdded(POSOrder posOrder, POSRefund refund) {

    }

    @Override
    public void paymentChanged(POSOrder posOrder, POSTransaction pay) {

    }

    @Override
    public void discountAdded(POSOrder posOrder, POSDiscount discount) {

    }

    @Override
    public void discountChanged(POSOrder posOrder, POSDiscount discount) {

    }

    @Override
    public void paymentVoided(POSOrder posOrder, POSPayment payment) {

    }
  }

}
