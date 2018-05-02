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

import com.clover.example.adapter.TransactionsListViewAdapter;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.POSTransaction;
import com.clover.example.model.StoreObserver;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IPaymentConnector;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.List;

public class TransactionsFragment extends Fragment {

  private com.clover.example.DeviceFragment.OnFragmentInteractionListener mListener;
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  private View view;
  private POSStore store;
  private List<POSTransaction> transactions;
  private ListView transactionsListView;

  public static TransactionsFragment newInstance(IPaymentConnector cloverConnector, POSStore store) {
    TransactionsFragment fragment = new TransactionsFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);
    return fragment;
  }

  public TransactionsFragment(){

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_transactions, container, false);
    updateUI();

    store.addStoreObserver(new StoreObserver() {
      @Override
      public void onCurrentOrderChanged(POSOrder currentOrder) {

      }

      @Override
      public void newOrderCreated(POSOrder order, boolean userInitiated) {

      }

      @Override
      public void cardAdded(POSCard card) {

      }

      @Override
      public void refundAdded(POSTransaction refund) {

      }

      @Override
      public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

      }

      @Override
      public void transactionsChanged(List<POSTransaction> transactions) {
        updateUI();
      }
    });
    return view;
  }

  public void updateUI(){
    transactions = store.getTransactions();

    if(transactions != null && transactions.size() > 0) {
      Log.d("TransactionsFragment", transactions.toString());
      transactionsListView = (ListView) view.findViewById(R.id.TransactionsListView);
      TransactionsListViewAdapter transactionsListViewAdapter = new TransactionsListViewAdapter(view.getContext(), R.id.TransactionsListView,transactions);
      transactionsListView.setAdapter(transactionsListViewAdapter);
      transactionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          POSTransaction transaction = (POSTransaction) transactionsListView.getItemAtPosition(position);
          if(transaction.getTransactionTitle() == "Refund"){
            transaction = store.getPaymentByCloverId(((POSRefund)transaction).getPaymentId());
          }
          ((NativePOSActivity)getActivity()).showPaymentDetails(transaction);
        }
      });
    }
    else{
      noTransactions();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    transactions = store.getTransactions();
    if(transactions.size()>0 && transactionsListView != null) {
      TransactionsListViewAdapter transactionsListViewAdapter = new TransactionsListViewAdapter(view.getContext(), R.id.TransactionsListView, transactions);
      transactionsListView.setAdapter(transactionsListViewAdapter);
    }
    else{
      noTransactions();
    }
  }

  public void noTransactions(){
    TextView noTransactions = (TextView) view.findViewById(R.id.NoTransactionsText);
    noTransactions.setVisibility(View.VISIBLE);
  }

  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }

  public void setStore(POSStore posStore){
    this.store = posStore;
  }
}
