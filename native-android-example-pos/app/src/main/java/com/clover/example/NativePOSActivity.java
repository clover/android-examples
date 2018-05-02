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

import com.clover.connector.sdk.v3.DisplayConnector;
import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSDiscount;
import com.clover.example.model.POSItem;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.POSTransaction;
import com.clover.example.utils.CurrencyUtils;
import com.clover.example.utils.IdUtils;
import com.clover.example.utils.ImageUtil;
import com.clover.sdk.v3.base.CardData;
import com.clover.sdk.v3.base.Challenge;
import com.clover.sdk.v3.connector.IDisplayConnector;
import com.clover.sdk.v3.connector.IDisplayConnectorListener;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Result;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
import com.clover.sdk.v3.remotepay.CloseoutResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PaymentResponse;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataRequest;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.ResponseCode;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.remotepay.TipAdded;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;

public class NativePOSActivity extends Activity implements CurrentOrderFragment.OnFragmentInteractionListener,
    AvailableItem.OnFragmentInteractionListener, OrdersFragment.OnFragmentInteractionListener,
    RegisterFragment.OnFragmentInteractionListener, SignatureFragment.OnFragmentInteractionListener,
    CardsFragment.OnFragmentInteractionListener, ManualRefundsFragment.OnFragmentInteractionListener,
    ProcessingFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
    PreAuthDialogFragment.PreAuthDialogFragmentListener, EnterPaymentIdFragment.EnterPaymentIdFragmentListener, TransactionsFragment.OnFragmentInteractionListener,
    AdjustTipFragment.AdjustTipFragmentListener, ChooseSaleTypeFragment.ChooseSaleTypeListener, EnterTipFragment.EnterTipDialogFragmentListener, EnterCustomerNameFragment.EnterCustomerNameListener, RefundPaymentFragment.PaymentRefundListener{

  private static final String TAG = "NativePOSActivity";
  private static final String DEFAULT_EID = "DFLTEMPLYEE";
  IPaymentConnector paymentConnector;
  IDisplayConnector displayConnector;
  Payment currentPayment = null;
  ArrayList<Challenge> currentChallenges = null;
  boolean initialPass = true;
  SharedPreferences sharedPreferences;

  final PaymentConfirmationListener paymentConfirmationListener = new PaymentConfirmationListener() {
    @Override
    public void onRejectClicked(Challenge challenge) { // Reject payment and send the challenge along for logging/reason
      paymentConnector.rejectPayment(currentPayment, challenge);
      currentChallenges = null;
      currentPayment = null;
    }

    @Override
    public void onAcceptClicked(final int challengeIndex) {
      if (challengeIndex == currentChallenges.size() - 1) { // no more challenges, so accept the payment
        paymentConnector.acceptPayment(currentPayment);
        currentChallenges = null;
        currentPayment = null;
      } else { // show the next challenge
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showPaymentConfirmation(paymentConfirmationListener, currentChallenges.get(challengeIndex + 1), challengeIndex + 1);
          }
        });
      }
    }
  };

  final IPaymentConnectorListener ccListener = new IPaymentConnectorListener() {

    public void onDeviceDisconnected() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(NativePOSActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
          Log.d(TAG, "disconnected");
          ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
        }
      });

    }

    public void onDeviceConnected() {

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showMessage("Ready!", Toast.LENGTH_SHORT);
          ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connected!");
        }
      });
    }

    @Override
    public void onAuthResponse(final AuthResponse response) {
      Log.d(TAG, response.toString());
      if (response.getSuccess()) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Payment _payment = response.getPayment();
            CardTransaction cardTransaction = _payment.getCardTransaction();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();//
            String cardDetails = cardTransaction.getCardType().toString() + " " + cardTransaction.getLast4();
            POSPayment payment = new POSPayment(_payment.getAmount(), cardDetails, cardTransaction.getCardType(), new Date(_payment.getCreatedTime()), _payment.getId(), _payment.getTender().getLabel(),
                "Auth", cardTransaction.getType(), false, cardTransaction.getEntryType(), cardTransaction.getState(), cashback, _payment.getOrder().getId(), _payment.getExternalPaymentId(), _payment.getTaxAmount(), tip);
            setPaymentStatus(payment, response);
            payment.setResult(_payment.getResult());
            store.addPaymentToOrder(payment, store.getCurrentOrder());
            store.addTransaction(payment);
            showMessage("Auth successfully processed.", Toast.LENGTH_SHORT);

            store.createOrder(false);
            CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
            currentOrderFragment.setOrder(store.getCurrentOrder());
            hidePreAuth();
            showRegister(null);
            displayConnector.showWelcomeScreen();
          }
        });
      } else {
        showMessage("Auth error:" + response.getResult(), Toast.LENGTH_LONG);
        displayConnector.showMessage("There was a problem processing the transaction");
      }
    }

    @Override
    public void onPreAuthResponse(final PreAuthResponse response) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (response.getSuccess()) {
            Log.d("NATIVEPOS", response.toString());
            Payment _payment = response.getPayment();
            CardTransaction cardTransaction = _payment.getCardTransaction();
            String cardDetails = cardTransaction.getCardType().toString() + " " + cardTransaction.getLast4();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();
            POSPayment payment = new POSPayment(_payment.getAmount(), cardDetails, cardTransaction.getCardType(), new Date(_payment.getCreatedTime()), _payment.getId(), _payment.getTender().getLabel(),
                "PreAuth", cardTransaction.getType(), false, cardTransaction.getEntryType(), cardTransaction.getState(), cashback, _payment.getOrder().getId(), _payment.getExternalPaymentId(), _payment.getTaxAmount(), tip);
            setPaymentStatus(payment, response);
            payment.setResult(_payment.getResult());
            store.getCurrentOrder().setPreAuth(payment);
            store.addTransaction(payment);
            showMessage("PreAuth successfully processed.", Toast.LENGTH_SHORT);
            preAuthSuccess(_payment.getCardTransaction());
          } else {
            showMessage("PreAuth: " + response.getResult(), Toast.LENGTH_LONG);
          }
        }
      });
      displayConnector.showWelcomeScreen();
    }

    @Override
    public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
      if (response.getSuccess()) {

        boolean updatedTip = false;
        for (POSOrder order : store.getOrders()) {
          for (POSTransaction exchange : order.getPayments()) {
            if (exchange instanceof POSPayment) {
              POSPayment posPayment = (POSPayment) exchange;
              if (exchange.getId().equals(response.getPaymentId())) {
                posPayment.setTipAmount(response.getTipAmount());
                updatePaymentDetailsTip(posPayment);
                // TODO: should the stats be updated?
                updatedTip = true;
                break;
              }
            }
          }
          if (updatedTip) {
            showMessage("Tip successfully adjusted", Toast.LENGTH_LONG);
            break;
          }
        }
      } else {
        showMessage("Tip adjust failed", Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onCapturePreAuthResponse(final CapturePreAuthResponse response) {
      Log.d(TAG, response.toString());
      if (response.getSuccess()) {
        for(final POSOrder order: store.getOrders()) {
          final POSPayment payment = order.getPreAuth();
          if (payment != null) {
            if (payment.getId().equals(response.getPaymentId())) {
              final long paymentAmount = response.getAmount();
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  order.setPreAuth(null);
                  store.addPaymentToOrder(payment, store.getCurrentOrder());
                  payment.setPaymentStatus(POSPayment.Status.AUTHORIZED);
                  payment.setAmount(paymentAmount);
                  payment.setTransactionTitle("Auth");
                  payment.setTipAmount(response.getTipAmount());
                  showMessage("Sale successfully processing using Pre Authorization", Toast.LENGTH_LONG);
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      store.createOrder(false);
                      CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                      if(currentOrderFragment != null) {
                        currentOrderFragment.setOrder(store.getCurrentOrder());
                      }
                      hidePreAuth();
                      showRegister(null);
                    }
                  });
                }
              });
              break;
            } else {
              showMessage("PreAuth Capture: Payment received does not match any of the stored PreAuth records", Toast.LENGTH_LONG);
            }
          }
        }
      } else {
        showMessage("PreAuth Capture Error: Payment failed with response code = " + response.getResult() + " and reason: " + response.getReason(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onVerifySignatureRequest(VerifySignatureRequest request) {

      FragmentManager fragmentManager = getFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

      hideFragments(fragmentManager, fragmentTransaction);

      Fragment fragment = fragmentManager.findFragmentByTag("SIGNATURE");
      if (fragment == null) {
        fragment = SignatureFragment.newInstance(request, paymentConnector);
        fragmentTransaction.add(R.id.contentContainer, fragment, "SIGNATURE");
      } else {
        ((SignatureFragment) fragment).setVerifySignatureRequest(request);
        fragmentTransaction.show(fragment);
      }

      fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
      if (request.getPayment() == null || request.getChallenges() == null) {
        showMessage("Error: The ConfirmPaymentRequest was missing the payment and/or challenges.", Toast.LENGTH_LONG);
      } else {
        currentPayment = request.getPayment();
        currentChallenges = new ArrayList<>(request.getChallenges());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showPaymentConfirmation(paymentConfirmationListener, currentChallenges.get(0), 0);
          }
        });
      }
    }

    @Override
    public void onSaleResponse(final SaleResponse response) {
      Log.d(TAG, response.toString());
      if (response != null) {
        if (response.getSuccess()) { // Handle cancel response
          if (response.getPayment() != null) {
            Payment _payment = response.getPayment();
            CardTransaction cardTransaction = _payment.getCardTransaction();
            String cardDetails = cardTransaction.getCardType().toString() + " " + cardTransaction.getLast4();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();
            POSPayment payment = new POSPayment(_payment.getAmount(), cardDetails, cardTransaction.getCardType(), new Date(_payment.getCreatedTime()), _payment.getId(), _payment.getTender().getLabel(),
                "Payment", cardTransaction.getType(), false, cardTransaction.getEntryType(), cardTransaction.getState(), cashback, _payment.getOrder().getId(), _payment.getExternalPaymentId(), _payment.getTaxAmount(), tip);
            setPaymentStatus(payment, response);
            payment.setResult(_payment.getResult());
            store.addPaymentToOrder(payment, store.getCurrentOrder());
            store.addTransaction(payment);
            showMessage("Sale successfully processed", Toast.LENGTH_SHORT);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                store.createOrder(false);
                CurrentOrderFragment currentOrderFragment;
                if(getApplicationContext().getResources().getBoolean(R.bool.isFlex)) {
                  currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentByTag("CURRENT_ORDER");
                }
                else{
                  currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                }
                currentOrderFragment.setOrder(store.getCurrentOrder());
                hidePreAuth();
                showRegister(null);
              }
            });
          } else { // Handle null payment
            showMessage("Error: Sale response was missing the payment", Toast.LENGTH_LONG);
          }
        } else {
          showMessage(response.getResult().toString() + ":" + response.getReason() + "  " + response.getMessage(), Toast.LENGTH_LONG);
        }
      } else { //Handle null payment response
        showMessage("Error: Null SaleResponse", Toast.LENGTH_LONG);
      }
      displayConnector.showWelcomeScreen();
    }

    @Override
    public void onManualRefundResponse(final ManualRefundResponse response) {
      if (response.getSuccess()) {
        Credit credit = response.getCredit();
        CardTransaction cardTransaction = credit.getCardTransaction();
        String cardDetails = cardTransaction.getCardType().toString()+ " "+ cardTransaction.getLast4();
        final POSTransaction nakedRefund = new POSTransaction(credit.getAmount(), cardDetails, cardTransaction.getCardType(), new Date(credit.getCreatedTime()), credit.getId(),
            credit.getTender().getLabel(), "Manual Refund", cardTransaction.getType(), true, cardTransaction.getEntryType(), cardTransaction.getState());
        nakedRefund.setEmployee(credit.getEmployee().getId());
        nakedRefund.setResult(Result.SUCCESS);
        store.addTransaction(nakedRefund);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            store.addRefund(nakedRefund);
            showMessage("Manual Refund successfully processed", Toast.LENGTH_SHORT);
          }
        });
      } else if (response.getResult() == ResponseCode.CANCEL) {
        showMessage("User canceled the Manual Refund", Toast.LENGTH_SHORT);
      } else {
        showMessage("Manual Refund Failed with code: " + response.getResult() + " - " + response.getMessage(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onRefundPaymentResponse(final RefundPaymentResponse response) {
      if (response.getSuccess()) {
        POSRefund refund = new POSRefund(response.getRefund().getId(), response.getRefund().getPayment().getId(), response.getOrderId(), "DEFAULT", response.getRefund().getAmount());
        refund.setDate(new Date(response.getRefund().getCreatedTime()));
        boolean done = false;

        for (POSOrder order : store.getOrders()) {
          for (POSTransaction payment : order.getPayments()) {
            if (payment instanceof POSPayment) {
              if (payment.getId().equals(response.getRefund().getPayment().getId())) {
                refund.setCardDetails(payment.getCardDetails());
                refund.setCardType(payment.getCardType());
                refund.setTender(payment.getTender());
                refund.setResult(response.getResult() == ResponseCode.SUCCESS ? Result.SUCCESS : Result.FAIL);
                refund.setTransactionTitle("Refund");
                store.addTransaction(refund);
                store.addRefundToOrder(refund, order);
                ((POSPayment) payment).setRefundId(refund.getId());
                updatePaymentDetailsRefund(payment, refund.getAmount());
                showMessage("Payment successfully refunded", Toast.LENGTH_SHORT);
                done = true;
                break;
              }
            }
          }
          if (done) {
            break;
          }
        }
      } else {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(NativePOSActivity.this);
            builder.setTitle("Refund Error").setMessage("There was an error refunding the payment");
            builder.create().show();
            Log.d(getClass().getName(), "Got refund response of " + response.getReason());
          }
        });
      }
    }

    /**
     * Called when a customer selects a tip amount on the Clover device screen
     *
     * @param tipAdded
     */
    @Override
    public void onTipAdded(TipAdded tipAdded) {

    }

    @Override
    public void onVoidPaymentResponse(VoidPaymentResponse response) {
      if (response.getSuccess()) {
        boolean done = false;
        for (POSOrder order : store.getOrders()) {
          for (POSTransaction payment : order.getPayments()) {
            if (payment instanceof POSPayment) {
              if (payment.getId().equals(response.getPaymentId())) {
                order.voidPayment(((POSPayment)payment));
                store.updateTransactionToVoided(payment.getId());
                showMessage("Payment was voided", Toast.LENGTH_SHORT);
                updatePaymentDetailsVoided(payment);
                done = true;
                break;
              }
            }
          }
          if (done) {
            break;
          }
        }
        for (POSTransaction transaction: store.getTransactions()){
          if(transaction.getId().equals(response.getPaymentId())){
            store.updateTransactionToVoided(transaction.getId());
            showMessage("Payment was voided", Toast.LENGTH_SHORT);
            updatePaymentDetailsVoided(transaction);
          }
        }
      } else {
        showMessage(getClass().getName() + ":Got VoidPaymentResponse of " + response.getResult(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onVaultCardResponse(final VaultCardResponse response) {
      if (response.getSuccess())  {
        POSCard card = new POSCard();
        card.setFirst6(response.getCard().getFirst6());
        card.setLast4(response.getCard().getLast4());
        card.setName(response.getCard().getCardholderName());
        card.setMonth(response.getCard().getExpirationDate().substring(0, 2));
        card.setYear(response.getCard().getExpirationDate().substring(2, 4));
        card.setToken(response.getCard().getToken());
        store.addCard(card);
        showMessage("Card successfully vaulted", Toast.LENGTH_SHORT);
      } else {
        if (response.getResult() == ResponseCode.CANCEL) {
          showMessage("User canceled the operation", Toast.LENGTH_SHORT);
          displayConnector.showWelcomeScreen();
        } else {
          showMessage("Error capturing card: " + response.getResult(), Toast.LENGTH_LONG);
          displayConnector.showMessage("Card was not saved");
          SystemClock.sleep(4000); //wait 4 seconds
          displayConnector.showWelcomeScreen();
        }
      }
    }

    @Override
    public void onReadCardDataResponse(final ReadCardDataResponse response) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AlertDialog.Builder builder = new AlertDialog.Builder(NativePOSActivity.this);
          builder.setTitle("Read Card Data Response");
          if (response.getSuccess()) {

            LayoutInflater inflater = NativePOSActivity.this.getLayoutInflater();

            View view = inflater.inflate(R.layout.card_data_table, null);
            ListView listView = (ListView) view.findViewById(R.id.cardDataListView);


            if (listView != null) {
              class RowData {
                RowData(String label, String value) {
                  this.text1 = label;
                  this.text2 = value;
                }

                String text1;
                String text2;
              }

              ArrayAdapter<RowData> data = new ArrayAdapter<RowData>(getBaseContext(), android.R.layout.simple_list_item_2) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                  View v = convertView;

                  if (v == null) {
                    LayoutInflater vi;
                    vi = LayoutInflater.from(getContext());
                    v = vi.inflate(android.R.layout.simple_list_item_2, null);
                  }

                  RowData rowData = getItem(position);

                  if (rowData != null) {
                    TextView primaryColumn = (TextView) v.findViewById(android.R.id.text1);
                    TextView secondaryColumn = (TextView) v.findViewById(android.R.id.text2);

                    primaryColumn.setText(rowData.text2);
                    secondaryColumn.setText(rowData.text1);
                  }

                  return v;
                }
              };
              listView.setAdapter(data);
              CardData cardData = response.getCardData();
              data.addAll(new RowData("Encrypted", cardData.getEncrypted() + ""));
              data.addAll(new RowData("Cardholder Name", cardData.getCardholderName()));
              data.addAll(new RowData("First Name", cardData.getFirstName()));
              data.addAll(new RowData("Last Name", cardData.getLastName()));
              data.addAll(new RowData("Expiration", cardData.getExp()));
              data.addAll(new RowData("First 6", cardData.getFirst6()));
              data.addAll(new RowData("Last 4", cardData.getLast4()));
              data.addAll(new RowData("Track 1", cardData.getTrack1()));
              data.addAll(new RowData("Track 2", cardData.getTrack2()));
              data.addAll(new RowData("Track 3", cardData.getTrack3()));
              data.addAll(new RowData("Masked Track 1", cardData.getMaskedTrack1()));
              data.addAll(new RowData("Masked Track 2", cardData.getMaskedTrack2()));
              data.addAll(new RowData("Masked Track 3", cardData.getMaskedTrack3()));
              data.addAll(new RowData("Pan", cardData.getPan()));

            }
            builder.setView(view);

          } else if (response.getResult() == ResponseCode.CANCEL) {
            builder.setMessage("Get card data canceled.");
          } else {
            builder.setMessage("Error getting card data. " + response.getReason() + ": " + response.getMessage());
          }

          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
          AlertDialog dialog = builder.create();
          dialog.show();

        }
      });
    }

    /**
     * Called in response to a closeout being processed
     *
     * @param response
     */
    @Override
    public void onCloseoutResponse(CloseoutResponse response) {
      if (response.getSuccess()) {
        showMessage("Closeout successfully scheduled", Toast.LENGTH_LONG);
      } else {
        showMessage("Closeout unsuccessful: " + response.getReason() + (response.hasBatch() ? " - Batch: " + response.getBatch() : ""), Toast.LENGTH_LONG);
      }
    }

    /**
     * Called in response to a doRetrievePayment(...) request
     *
     * @param response
     */
    @Override
    public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
      if (response.getSuccess()) {
        showPopupMessage(null, new String[]{"Retrieve Payment successful for Payment ID: " + response.getExternalPaymentId(),
            " QueryStatus: " + response.getQueryStatus(),
            " Payment: " + response.getPayment(),
            " reason: " + response.getReason()}, false);
      } else {
        showPopupMessage(null, new String[]{"Retrieve Payment error: " + response.getResult()}, false);
      }
    }

    @Override
    public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
      if (!response.getSuccess()) {
        store.setPendingPayments(null);
      } else {
        store.setPendingPayments(response.getPendingPaymentEntries());
      }
    }
  };


  /**
   * Just listens for connection events.
   */
  final IDisplayConnectorListener dcListener = new IDisplayConnectorListener() {
    @Override
    public void onDeviceDisconnected() {
      Log.d(TAG, "onDeviceDisconnected");
    }

    @Override
    public void onDeviceConnected() {
      Log.d(TAG, "onDeviceConnected");
    }
  };

  POSStore store = new POSStore();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().hide();
    setContentView(R.layout.activity_example_pos);

    if(getApplicationContext().getResources().getBoolean(R.bool.isFlex)){
      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    else{
      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentContainer);

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    HomeFragment home = HomeFragment.newInstance(paymentConnector);
    fragmentTransaction.add(R.id.contentContainer, home, "HOME");
    fragmentTransaction.commit();
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    initStore();
  }

  private void initDisplayConnector() {
    disposeDisplayConnector();
    // Retrieve the Clover account
    Account account = CloverAccount.getAccount(this);

    // If an account can't be acquired, exit the app
    if (account == null) {
      Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Log.d(TAG, String.format("Account is=%s", account));
    displayConnector = new DisplayConnector(this, account, dcListener);
  }

  /**
   * Destroy this classes DisplayConnector and dispose of it.
   */
  private void disposeDisplayConnector() {
    if (displayConnector != null) {
      displayConnector.dispose();
      displayConnector = null;
    }
  }

  private void initPaymentConnector() {
    if (paymentConnector == null) {
      paymentConnector = new PaymentConnector(getApplicationContext(), CloverAccount.getAccount(getApplicationContext()), ccListener);
      paymentConnector.initializeConnection();
    }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    //Call the super and let it do its thing
    super.onSaveInstanceState(savedInstanceState);
    saveState(savedInstanceState);
  }

  private void saveState(Bundle savedInstanceState){
    savedInstanceState.putSerializable("POSStore", store);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can restore the view hierarchy
    super.onRestoreInstanceState(savedInstanceState);


    // Restore state members from saved instance
    store = (POSStore)savedInstanceState.getSerializable("POSStore");
    initStore();
  }


  @Override
  protected void onPause() {
    // The DisplayConnector must be disposed and created new to allow the
    // payment activities/fragments to use it during the payment flows.
    displayConnector.showWelcomeScreen();
    displayConnector.dispose();

    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initStore();

    if (paymentConnector == null) {
      initPaymentConnector();
    }

    // The DisplayConnector must be reinitialized to allow your activity
    // to control it when outside the Clover system payment flow.
    initDisplayConnector();
    updateComponentsWithNewConnectors();
    if (initialPass) {
      showHome(null);
      initialPass = false;
    }
  }

  private void initStore() {
    if (store == null) {
      store = new POSStore();
    }
    if (store.getAvailableItems().size() == 0) {
      // initialize store...
      store.addAvailableItem(new POSItem("0", "Chicken Nuggets", 539, true, true));
      store.addAvailableItem(new POSItem("1", "Hamburger", 699, true, true));
      store.addAvailableItem(new POSItem("2", "Cheeseburger", 759, true, true));
      store.addAvailableItem(new POSItem("3", "Double Hamburger", 819, true, true));
      store.addAvailableItem(new POSItem("4", "Double Cheeseburger", 899, true, true));
      store.addAvailableItem(new POSItem("5", "Bacon Cheeseburger", 999, true, true));
      store.addAvailableItem(new POSItem("6", "Small French Fries", 239, true, true));
      store.addAvailableItem(new POSItem("7", "Medium French Fries", 259, true, true));
      store.addAvailableItem(new POSItem("8", "Large French Fries", 279, true, true));
      store.addAvailableItem(new POSItem("9", "Small Fountain Drink", 169, true, true));
      store.addAvailableItem(new POSItem("10", "Medium Fountain Drink", 189, true, true));
      store.addAvailableItem(new POSItem("11", "Large Fountain Drink", 229, true, true));
      store.addAvailableItem(new POSItem("12", "Chocolate Milkshake", 449, true, true));
      store.addAvailableItem(new POSItem("13", "Vanilla Milkshake", 419, true, true));
      store.addAvailableItem(new POSItem("14", "Strawberry Milkshake", 439, true, true));
      store.addAvailableItem(new POSItem("15", "Ice Cream Cone", 189, true, true));
      store.addAvailableItem(new POSItem("16", "$25 Gift Card", 2500, false, false));
      store.addAvailableItem(new POSItem("17", "$50 Gift Card", 5000, false, false));

      store.addAvailableDiscount(new POSDiscount("10% Off", 0.1f));
      store.addAvailableDiscount(new POSDiscount("$5 Off", 500));
      store.addAvailableDiscount(new POSDiscount("None", 0));
      store.createOrder(false);
    }

    // Transaction level settings defaults can be set if desired here
    /////////////////////////////////////////////////////////////////////
    //store.setTipMode(SaleRequest.TipMode.ON_SCREEN_BEFORE_PAYMENT);
    //store.setSignatureEntryLocation(DataEntryLocation.ON_PAPER);
    //store.setDisablePrinting(false);
    //store.setDisableReceiptOptions(false);
    //store.setDisableDuplicateChecking(false);
    //store.setAllowOfflinePayment(false);
    //store.setForceOfflinePayment(false);
    //store.setApproveOfflinePaymentWithoutPrompt(true);
    //store.setAutomaticSignatureConfirmation(true);
    //store.setAutomaticPaymentConfirmation(true);

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed(){
    FragmentManager fm = getFragmentManager();
    if (fm.getBackStackEntryCount() > 0) {
      Log.i("MainActivity", "popping backstack");
      fm.popBackStack();
    } else {
      Log.i("MainActivity", "nothing on backstack, calling super");
      super.onBackPressed();
    }
  }

  private void updatePaymentDetailsVoided (POSTransaction payment){
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    ((PaymentDetailsFragment)fragment).paymentVoided(payment);
  }

  private void updatePaymentDetailsTip(POSTransaction payment){
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    ((PaymentDetailsFragment)fragment).setTip(((POSPayment)payment).getTipAmount());
  }

  private void updatePaymentDetailsRefund(POSTransaction payment, long amount){
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    ((PaymentDetailsFragment)fragment).addRefund(payment, amount);
  }

  private void setPaymentStatus(POSPayment payment, PaymentResponse response) {
    Log.d("setPaymentStatus: ", response.toString());
    if (response.hasPayment()){
      if (response.getIsSale() != null && response.getIsSale()) {
        payment.setPaymentStatus(POSPayment.Status.PAID);
      } else {
        if (response.getIsAuth() != null && response.getIsAuth()) {
          payment.setPaymentStatus(POSPayment.Status.AUTHORIZED);
        } else {
          if (response.getIsPreAuth() != null && response.getIsPreAuth()) {
            payment.setPaymentStatus(POSPayment.Status.PREAUTHORIZED);
          }
        }
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (paymentConnector != null) {
      paymentConnector.dispose();
    }
    if (displayConnector != null) {
      displayConnector.dispose();
    }
  }

  private void showPaymentConfirmation(PaymentConfirmationListener listenerIn, Challenge challengeIn, int challengeIndexIn) {
    final int challengeIndex = challengeIndexIn;
    final Challenge challenge = challengeIn;
    final PaymentConfirmationListener listener = listenerIn;
    AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(this);
    confirmationDialog.setTitle("Payment Confirmation");
    confirmationDialog.setCancelable(false);
    confirmationDialog.setMessage(challenge.getMessage());
    confirmationDialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        listener.onRejectClicked(challenge);
        dialog.dismiss();
      }
    });
    confirmationDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        listener.onAcceptClicked(challengeIndex);
        dialog.dismiss();
      }
    });
    confirmationDialog.show();
  }


  private void showMessage(final String msg, final int duration) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(NativePOSActivity.this, msg, duration).show();
      }
    });
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  protected void showPopupMessage (final String title, final String[] content, final boolean monospace) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        FragmentManager fm = getFragmentManager();
        PopupMessageFragment popupMessageFragment = PopupMessageFragment.newInstance(title, content, monospace);
        popupMessageFragment.show(fm, "fragment_popup_message");
      }
    });
  }

  private void updateComponentsWithNewConnectors() {
    FragmentManager fragmentManager = getFragmentManager();

    RegisterFragment refFragment = (RegisterFragment) fragmentManager.findFragmentByTag("REGISTER");
    if (refFragment != null) {
      refFragment.setPaymentConnector(paymentConnector);
      refFragment.setDisplayConnector(displayConnector);
    }
    OrdersFragment ordersFragment = (OrdersFragment) fragmentManager.findFragmentByTag("ORDERS");
    if (ordersFragment != null) {
      ordersFragment.setPaymentConnector(paymentConnector);
    }
    ManualRefundsFragment manualRefundsFragment = (ManualRefundsFragment) fragmentManager.findFragmentByTag("REFUNDS");
    if (manualRefundsFragment != null) {
      manualRefundsFragment.setPaymentConnector(paymentConnector);
    }
    CardsFragment cardsFragment = (CardsFragment) fragmentManager.findFragmentByTag("CARDS");
    if (cardsFragment != null) {
      cardsFragment.setPaymentConnector(paymentConnector);
    }
    HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag("HOME");
    if (homeFragment != null) {
      homeFragment.setCloverConnector(paymentConnector);
    }
    CustomActivitiesFragment customFragment = (CustomActivitiesFragment) fragmentManager.findFragmentByTag("CUSTOM");
    if (customFragment != null) {
      customFragment.setCloverConnector(paymentConnector);
    }
    DeviceFragment deviceFragment = (DeviceFragment) fragmentManager.findFragmentByTag("DEVICE");
    if (deviceFragment != null) {
      deviceFragment.setCloverConnector(paymentConnector);
    }
    RecoveryOptionsFragment recoveryFragment = (RecoveryOptionsFragment) fragmentManager.findFragmentByTag("RECOVERY");
    if (recoveryFragment != null) {
      recoveryFragment.setCloverConnector(paymentConnector);
    }
    TransactionsFragment transactionsFragment = (TransactionsFragment) fragmentManager.findFragmentByTag("TRANSACTIONS");
    if (transactionsFragment != null) {
      transactionsFragment.setCloverConnector(paymentConnector);
    }
    PaymentDetailsFragment paymentFragment = (PaymentDetailsFragment) fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    if (paymentFragment != null) {
//      paymentFragment.setCloverConnector(cloverConnector);
    }
  }

  public void preAuthSuccess(CardTransaction cardTransaction){
    CardType cardType = cardTransaction.getCardType();
    LinearLayout preauthInfo = (LinearLayout) findViewById(R.id.PreAuthInfo);
    preauthInfo.setVisibility(View.VISIBLE);
    LinearLayout current = (LinearLayout) findViewById(R.id.CurrentOrder);
    current.setVisibility(View.GONE);
    ImageView preauthTender = (ImageView) findViewById(R.id.PreAuthTender);
    preauthTender.setImageResource(ImageUtil.getCardTypeImage(cardType));
    TextView preAuthCardType = (TextView) findViewById(R.id.PreAuthCardType);
    preAuthCardType.setText("Card: "+cardType.toString()+" "+cardTransaction.getLast4());

  }

  public void showHome(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("HOME");
    if (fragment == null) {
      fragment = HomeFragment.newInstance(paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "HOME");
    } else {
      ((HomeFragment) fragment).setCloverConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("HOME");
    fragmentTransaction.commit();
  }

  public void showOrders(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment == null) {
      fragment = OrdersFragment.newInstance(store, paymentConnector);
      ((OrdersFragment) fragment).setPaymentConnector(paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "ORDERS");
    } else {
      ((OrdersFragment) fragment).setPaymentConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("ORDERS");
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showRegister(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment == null) {
      fragment = RegisterFragment.newInstance(store, paymentConnector, displayConnector, false);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
    }
    else {
      ((RegisterFragment) fragment).setPaymentConnector(paymentConnector);
      ((RegisterFragment)fragment).setPreAuth(false);
      ((RegisterFragment) fragment).setVaulted(false);
      ((RegisterFragment) fragment).setVaultedCard(null);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("REGISTER");
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showRefunds(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if (fragment == null) {
      fragment = ManualRefundsFragment.newInstance(store, paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REFUNDS");
    } else {
      ((ManualRefundsFragment) fragment).setPaymentConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("REFUNDS");
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showCards(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("CARDS");
    if (fragment == null) {
      fragment = CardsFragment.newInstance(store, paymentConnector);
      ((CardsFragment) fragment).setPaymentConnector(paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "CARDS");
    } else {
      ((CardsFragment) fragment).setPaymentConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("CARDS");
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showCustomActivities (View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("CUSTOM");
    if (fragment == null) {
      fragment = CustomActivitiesFragment.newInstance(paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "CUSTOM");
    } else {
      ((CustomActivitiesFragment) fragment).setCloverConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("CUSTOM");
    fragmentTransaction.commit();
  }

  public void showDevice (View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("DEVICE");
    if (fragment == null) {
      fragment = DeviceFragment.newInstance(paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "DEVICE");
    } else {
      ((DeviceFragment) fragment).setCloverConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("DEVICE");
    fragmentTransaction.commit();
  }

  public void showRecoveryOptions (View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("RECOVERY");
    if (fragment == null) {
      fragment = RecoveryOptionsFragment.newInstance(store, paymentConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "RECOVERY");
    } else {
      ((RecoveryOptionsFragment) fragment).setCloverConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("RECOVERY");
    fragmentTransaction.commit();
  }

  public void showTransactions (View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("TRANSACTIONS");
    if (fragment == null) {
      fragment = TransactionsFragment.newInstance(paymentConnector, store);
      fragmentTransaction.add(R.id.contentContainer, fragment, "TRANSACTIONS");
    } else {
      ((TransactionsFragment) fragment).setCloverConnector(paymentConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("TRANSACTIONS");
    fragmentTransaction.commit();
  }

  public void showPaymentDetails (POSTransaction transaction){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    if (fragment == null) {
      fragment = PaymentDetailsFragment.newInstance(transaction, paymentConnector, store);
      fragmentTransaction.add(R.id.contentContainer, fragment, "PAYMENT_DETAILS");
    } else {
      ((PaymentDetailsFragment) fragment).setCloverConnector(paymentConnector);
      ((PaymentDetailsFragment)fragment).setTransaction(transaction);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("PAYMENT_DETAILS");
    fragmentTransaction.commit();
  }

  public void hidePreAuth(){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    ((RegisterFragment)fragment).clearPreAuth();
  }

  public void startPreAuth (View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment == null) {
      fragment = RegisterFragment.newInstance(store, paymentConnector, displayConnector, true);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
    } else {
      ((RegisterFragment)fragment).setPaymentConnector(paymentConnector);
      ((RegisterFragment)fragment).setPreAuth(true);

      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("REGISTER");
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void startVaulted (POSCard vaultedCard){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment == null) {
      fragment = RegisterFragment.newInstance(store, paymentConnector, displayConnector, true, vaultedCard);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
    } else {
      ((RegisterFragment)fragment).setPaymentConnector(paymentConnector);
      ((RegisterFragment)fragment).setVaulted(true);
      ((RegisterFragment)fragment).setVaultedCard(vaultedCard);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("REGISTER");
    fragmentTransaction.commitAllowingStateLoss();
  }

  protected void hideFragments(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("SIGNATURE");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("CARDS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("MISC");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PRE_AUTHS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PENDING");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("HOME");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("CUSTOM");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("DEVICE");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("RECOVERY");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("TRANSACTIONS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PAYMENT_DETAILS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("CURRENT_ORDER");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
  }

  public void onClickCloseout(View view) {
    CloseoutRequest request = new CloseoutRequest();
    request.setAllowOpenTabs(false);
    request.setBatchId(null);
    paymentConnector.closeout(request);
  }

  public void onManualRefundClick(View view) {
    CharSequence val = ((TextView) findViewById(R.id.ManualRefundTextView)).getText();
    try {
      long refundAmount = CurrencyUtils.convertToLong(val.toString());
      ManualRefundRequest request = new ManualRefundRequest();
      request.setExternalId(IdUtils.getNextId());
      request.setAmount(refundAmount);
      request.setCardEntryMethods(store.getCardEntryMethods());
      request.setDisablePrinting(store.getDisablePrinting());
      request.setDisableReceiptSelection(store.getDisableReceiptOptions());
      paymentConnector.manualRefund(request);
    } catch (NumberFormatException nfe) {
      showMessage("Invalid value. Must be an integer.", Toast.LENGTH_LONG);
    }
  }

  public void preauthCardClick(View view) {
    PreAuthRequest request = new PreAuthRequest();
    request.setAmount(5000L);
    request.setExternalId(IdUtils.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    paymentConnector.preAuth(request);
  }


  public void onReadCardDataClick(View view) {
    ReadCardDataRequest readCardDataRequest = new ReadCardDataRequest();
    readCardDataRequest.setCardEntryMethods(store.getCardEntryMethods());
    paymentConnector.readCardData(readCardDataRequest);
  }

  public void refreshPendingPayments(View view) {
    paymentConnector.retrievePendingPayments();
  }

  @Override
  public void onContinue(String name, String amount) {

  }

  @Override
  public void onLookup(String paymentId) {

  }

  @Override
  public void onSave(long tipAmount) {

  }

  @Override
  public void onSaleTypeChoice(String choice) {

  }

  @Override
  public void onContinue(long amount) {

  }

  @Override
  public void onContinue(String name) {

  }

  @Override
  public void makePartialRefund(long amount) {

  }

  @Override
  public void makeFullRefund() {

  }
}
