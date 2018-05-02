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

import com.clover.example.adapter.AvailableItemListViewAdapter;
import com.clover.example.model.OrderObserver;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSDiscount;
import com.clover.example.model.POSLineItem;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;
import com.clover.example.utils.IdUtils;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.remotepay.SaleRequest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CurrentOrderFragment extends Fragment implements OrderObserver, ChooseSaleTypeFragment.ChooseSaleTypeListener, EnterTipFragment.EnterTipDialogFragmentListener{

  private POSStore store;
  private View v;
  private POSCard vaultedCard;
  private boolean preAuth = false;
  private boolean vaulted = false;
  POSOrder order = new POSOrder();
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  List<CurrentOrderFragmentListener> listeners = new ArrayList<CurrentOrderFragmentListener>(5);
  private OnFragmentInteractionListener mListener;

  public static CurrentOrderFragment newInstance(boolean preauth) {
    CurrentOrderFragment fragment = new CurrentOrderFragment();
    fragment.setPreAuth(preauth);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    v = inflater.inflate(R.layout.fragment_current_order, container, false);
    updateListView();
    updateTotals();
    Button newOrderButton = ((Button) v.findViewById(R.id.NewOrderButton));
    newOrderButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (store.getCurrentOrder().getItems().size() > 0) {
          onNewOrderClicked();
        }
        else{
          ((NativePOSActivity)getActivity()).showPopupMessage(null, new String[]{"You cannot save an order with no items"}, false);
        }
      }
    });

    Button payButton = ((Button) v.findViewById(R.id.PayButton));
    payButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (store.getCurrentOrder().getItems().size() > 0) {
          if(preAuth){
            showTransactionSettings(new Runnable() {
              @Override
              public void run() {
                showEnterTipDialog();
              }
            }, TransactionSettingsFragment.transactionTypes.PREAUTH );
          }
          else if(vaulted){
            showTransactionSettings(new Runnable() {
              @Override
              public void run() {
                makeVaultedSale();
              }
            }, TransactionSettingsFragment.transactionTypes.SALE);
          }
          else {
            showChooseSaleType();
          }
        }
        else{
          ((NativePOSActivity)getActivity()).showPopupMessage(null, new String[]{"You cannot make a sale with no items"}, false);
        }
      }
    });

    final ListView currentOrderItemsListView = (ListView) v.findViewById(R.id.CurrentOrderItems);

    currentOrderItemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
      @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // prompt for delete...
        final POSLineItem lineItem = (POSLineItem)currentOrderItemsListView.getItemAtPosition(position);
        String thisTheseLabel = lineItem.getQuantity() == 1 ? "this" : "these";

        new AlertDialog.Builder(getActivity())
            .setTitle("Delete?")
            .setMessage(String.format("Do you want to remove %s items from the order?", thisTheseLabel))
            .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
              @Override public void onClick(DialogInterface dialog, int which) {
                order.remoteAllItems(lineItem);
              }
            })
            .setNegativeButton("No", null)
            .show();
        return true; // consume the event
      }
    });

    if(vaulted && vaultedCard != null){
        LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        vaulted.setVisibility(View.VISIBLE);
        TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
        TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
        vaultedName.setText(vaultedCard.getVaultedName());
        vaultedCardNum.setText(getString(R.string.vault_card_num, vaultedCard.getFirst6(), vaultedCard.getLast4()));
    }

    return v;
  }

  private void makeVaultedSale(){
    VaultedCard vaulted = new VaultedCard();
    vaulted.setCardholderName(vaultedCard.getName());
    vaulted.setFirst6(vaultedCard.getFirst6());
    vaulted.setLast4(vaultedCard.getLast4());
    vaulted.setExpirationDate(vaultedCard.getMonth() + vaultedCard.getYear());
    vaulted.setToken(vaultedCard.getToken());

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
    TipMode tipMode = store.getTipMode() != null ? TipMode.valueOf(store.getTipMode().toString()) : null;
    request.setTipMode(tipMode != null ? tipMode : null);
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    request.setTipAmount(store.getTipAmount());
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    request.setVaultedCard(vaulted);
    cloverConnectorWeakReference.get().sale(request);
  }

  private void showEnterTipDialog(){
    FragmentManager fm = getFragmentManager();
    EnterTipFragment enterTipFragment = EnterTipFragment.newInstance();
    enterTipFragment.addListener(this);
    enterTipFragment.show(fm, "fragment_enter_tip");
  }

  private void showTransactionSettings(Runnable runnable, TransactionSettingsFragment.transactionTypes type) {
    FragmentManager fm = getFragmentManager();
    TransactionSettingsFragment editNameDialog = TransactionSettingsFragment.newInstance(store, type);
    editNameDialog.continueAction = runnable;
    editNameDialog.setWeakCloverConnector(cloverConnectorWeakReference);
    editNameDialog.show(fm, "fragment_transaction_settings");
  }

  private void showChooseSaleType() {
    FragmentManager fm = getFragmentManager();
    ChooseSaleTypeFragment chooseSaleTypeFragment = ChooseSaleTypeFragment.newInstance();
    chooseSaleTypeFragment.addListener(this);
    chooseSaleTypeFragment.show(fm, "fragment_choose_sale_type");
  }

  private void payWithPreAuth(long amount){
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.payWithPreAuth(amount);
    }
  }

  private void onNewOrderClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onNewOrderClicked();
    }
  }

  private void onSaleClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onSaleClicked();
    }
  }

  private void onAuthClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onAuthClicked();
    }
  }

  private void updateListView() {

    if (v != null) {
      v.post(new Runnable(){
        @Override public void run() {
          ListView listView = (ListView) v.findViewById(R.id.CurrentOrderItems);
          POSLineItem[] itemArray = new POSLineItem[order.getItems().size()];
          AvailableItemListViewAdapter items = new AvailableItemListViewAdapter(listView.getContext(), R.layout.listitem_order_item, order.getItems().toArray(itemArray));
          listView.setAdapter(items);
        }
      });
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
      updateCurrentOrder();
      updateTotals();
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void updateCurrentOrder() {
    updateListView();
    updateTotals();
  }

  public void addListener(CurrentOrderFragmentListener listener) {
    listeners.add(listener);
  }

  private void updateTotals() {
    if (v != null) {
      v.post(new Runnable(){
        @Override public void run() {
          String subtotalFormatted = CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault());
          ((TextView) v.findViewById(R.id.SubtotalLabel)).setText(subtotalFormatted);
          String taxFormatted = CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault());
          ((TextView) v.findViewById(R.id.TaxLabel)).setText(taxFormatted);
          String totalFormatted = CurrencyUtils.format(order.getTotal(), Locale.getDefault());
          ((TextView) v.findViewById(R.id.TotalLabel)).setText(totalFormatted);
        }
      });
    }
  }

  @Override
  public void onSaleTypeChoice(final String choice) {
    showTransactionSettings(new Runnable() {
      @Override
      public void run() {
        if(choice == "Sale") {
          onSaleClicked();
        }
        else if (choice == "Auth"){
          onAuthClicked();
        }
      }
    }, choice == "Sale" ? TransactionSettingsFragment.transactionTypes.SALE : TransactionSettingsFragment.transactionTypes.AUTH);
  }

  @Override
  public void onContinue(long amount) {
    payWithPreAuth(amount);
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public void setOrder(POSOrder order) {
    if (order != null) {
      this.order.removeObserver(this);
      this.order = order;
      this.order.addOrderObserver(this);
      updateCurrentOrder();
      updateTotals();
    }
  }

  public void setVaultedCard(POSCard vaultedCard) {
    this.vaultedCard = vaultedCard;
    if (v != null){
      if(vaultedCard != null) {
        LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        vaulted.setVisibility(View.VISIBLE);
        TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
        TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
        vaultedName.setText(vaultedCard.getVaultedName());
        vaultedCardNum.setText(getString(R.string.vault_card_num, vaultedCard.getFirst6(), vaultedCard.getLast4()));
      } else {
        LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        vaulted.setVisibility(View.GONE);
        TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
        TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
        vaultedName.setText("");
        vaultedCardNum.setText("");
      }
    }
  }

  public void setVaulted(boolean vaulted) {
    this.vaulted = vaulted;
    if(v != null) {
      if(!vaulted){
        LinearLayout current = (LinearLayout) v.findViewById(R.id.CurrentOrder);
        current.setVisibility(View.VISIBLE);
        LinearLayout preauthInfo = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        preauthInfo.setVisibility(View.GONE);
      }
    }
  }

  public void setPreAuth(boolean value){
    this.preAuth = value;
    if(v != null) {
      if (!preAuth) {
        LinearLayout current = (LinearLayout) v.findViewById(R.id.CurrentOrder);
        current.setVisibility(View.VISIBLE);
        LinearLayout preauthInfo = (LinearLayout) v.findViewById(R.id.PreAuthInfo);
        preauthInfo.setVisibility(View.GONE);
      }
    }
  }

  public void setStore(POSStore store) {
    this.store = store;
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }

  @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();
  }

  @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {
    updateCurrentOrder();

  }

  @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {
    updateCurrentOrder();

  }

  @Override public void paymentChanged(POSOrder posOrder, POSTransaction pay) {
    updateCurrentOrder();

  }

  @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {
    updateCurrentOrder();

  }

  @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {
    updateCurrentOrder();

  }

  @Override
  public void paymentVoided(POSOrder posOrder, POSPayment payment) {

  }

}
