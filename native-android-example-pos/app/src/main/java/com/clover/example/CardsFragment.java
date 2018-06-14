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

import com.clover.example.adapter.CardsListViewAdapter;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSStore;
import com.clover.example.model.POSTransaction;
import com.clover.example.model.StoreObserver;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IPaymentConnector;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import java.lang.ref.WeakReference;
import java.util.List;

public class CardsFragment extends Fragment implements EnterCustomerNameFragment.EnterCustomerNameListener {
    private String customerName = "";
    private Button vaultNewCard;

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<IPaymentConnector> paymentConnectorWeakReference;
    private ListView cardsListView;

    public static CardsFragment newInstance(POSStore store, IPaymentConnector paymentConnector) {
        CardsFragment fragment = new CardsFragment();
        fragment.setStore(store);
        fragment.setPaymentConnector(paymentConnector);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public CardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_cards, container, false);

        vaultNewCard = (Button) view.findViewById(R.id.VaultNewCardButton);
        vaultNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnterCustomerName();
            }
        });


        store.addStoreObserver(new StoreObserver() {
            @Override
            public void onCurrentOrderChanged(POSOrder currentOrder) {

            }

            @Override public void newOrderCreated(POSOrder order, boolean userInitiated) {

            }

            @Override public void cardAdded(POSCard card) {
                POSCard lastCard = store.getLastVaultedCard();
                if(lastCard != null) {
                    lastCard.setVaultedName(customerName);
                    store.setLastVaultedCard(null);
                    customerName = "";
                }
                final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
                new AsyncTask(){
                    @Override protected Object doInBackground(Object[] params) {
                        return null;
                    }

                    @Override protected void onPostExecute(Object o) {
                        cardsListView.setAdapter(cardsListViewAdapter);
                    }
                }.execute();
            }

            @Override public void refundAdded(POSTransaction refund) {

            }

            @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

            }

            @Override
            public void transactionsChanged(List<POSTransaction> transactions) {

            }
        });

        cardsListView = (ListView)view.findViewById(R.id.CardsListView);
        final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
        cardsListView.setAdapter(cardsListViewAdapter);


        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final POSCard posCard = (POSCard) cardsListView.getItemAtPosition(position);
                ((NativePOSActivity)getActivity()).startVaulted(posCard);
            }
        });

        return view;
    }

    private void showEnterCustomerName () {
        FragmentManager fm = getFragmentManager();
        EnterCustomerNameFragment enterCustomerNameFragment = EnterCustomerNameFragment.newInstance();
        enterCustomerNameFragment.addListener(this);
        enterCustomerNameFragment.show(fm, "fragment_enter_customer_name_dialog");
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                         + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setStore(POSStore store) {
        this.store = store;
    }

    @Override
    public void onContinue(String name) {
        this.customerName = name;
        paymentConnectorWeakReference.get().vaultCard(store.getCardEntryMethods());
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void setPaymentConnector(IPaymentConnector paymentConnector) {
        paymentConnectorWeakReference = new WeakReference<IPaymentConnector>(paymentConnector);
    }

}
