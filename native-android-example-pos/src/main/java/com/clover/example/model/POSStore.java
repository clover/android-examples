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

package com.clover.example.model;

import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.DataEntryLocation;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class POSStore implements Serializable{
  public enum TipMode {TIP_PROVIDED, ON_SCREEN_BEFORE_PAYMENT, NO_TIP}
  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33026
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33282
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33796
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS; // 34824

  private static int orderNumber = 1000;

  private LinkedHashMap<String, POSItem> availableItems;
  private List<POSDiscount> availableDiscounts;
  private List<POSOrder> orders;
  private List<POSCard> cards;
  private POSCard lastVaultedCard;
  private List<POSTransaction> refunds;
  private List<POSTransaction> transactions;
  private List<POSPayment> preAuths;
  private POSOrder currentOrder;

  private transient Map<String, POSOrder> orderIdToOrder = new HashMap<String, POSOrder>();
  private transient Map<String, POSPayment> paymentIdToPOSPayment = new HashMap<String, POSPayment>();

  private transient List<OrderObserver> orderObservers = new ArrayList<OrderObserver>();
  private transient List<StoreObserver> storeObservers = new ArrayList<StoreObserver>();


  private int cardEntryMethods = CARD_ENTRY_METHOD_MAG_STRIPE | CARD_ENTRY_METHOD_NFC_CONTACTLESS | CARD_ENTRY_METHOD_ICC_CONTACT | CARD_ENTRY_METHOD_MANUAL;
  private Boolean approveOfflinePaymentWithoutPrompt;
  private Boolean allowOfflinePayment;
  private Boolean forceOfflinePayment;
  private Boolean disablePrinting;
  private Long tipAmount;
  private Long signatureThreshold;
  private DataEntryLocation signatureEntryLocation;
  private TipMode tipMode;
  private Boolean disableReceiptOptions;
  private Boolean disableDuplicateChecking;
  private Boolean automaticSignatureConfirmation;
  private Boolean automaticPaymentConfirmation;
  private List<PendingPaymentEntry> pendingPayments;


  public POSStore() {
    availableItems = new LinkedHashMap<String, POSItem>();
    availableDiscounts = new ArrayList<POSDiscount>();
    orders = new ArrayList<POSOrder>();
    cards = new ArrayList<POSCard>();
    refunds = new ArrayList<POSTransaction>();
    preAuths = new ArrayList<POSPayment>();
    transactions = new ArrayList<POSTransaction>();
  }

  public void createOrder(boolean userInitiated) {
    if (currentOrder != null) {
      for (OrderObserver oo : orderObservers) {
        currentOrder.removeObserver(oo);
      }
    }
    POSOrder order = new POSOrder();
    for (OrderObserver oo : orderObservers) {
      order.addOrderObserver(oo);
    }
    order.id = "" + (++orderNumber);
    currentOrder = order;
    orders.add(order);
    orderIdToOrder.put(order.id, order);

    notifyNewOrderCreated(currentOrder, userInitiated);
  }

  private void notifyNewOrderCreated(POSOrder currentOrder, boolean userInitiated) {
    for (StoreObserver so : storeObservers) {
      so.newOrderCreated(currentOrder, userInitiated);
    }
  }

  public void addPaymentToOrder(POSPayment payment, POSOrder order) {
    order.addPayment(payment);
    paymentIdToPOSPayment.put(payment.getId(), payment);
  }

  public void addRefundToOrder(POSRefund refund, POSOrder order) {
    order.addRefund(refund);
  }

  public void addCurrentOrderObserver(OrderObserver observer) {
    this.orderObservers.add(observer);
    if(currentOrder != null) {
      currentOrder.addOrderObserver(observer);
    }
  }

  public void addStoreObserver(StoreObserver storeObserver) {
    this.storeObservers.add(storeObserver);
  }

  public List<POSTransaction> getTransactions(){
    return this.transactions;
  }

  public void addTransaction(POSTransaction transaction){
    this.transactions.add(transaction);
    for(StoreObserver observer : storeObservers){
      observer.transactionsChanged(this.transactions);
    }
  }

  public POSItem addAvailableItem(POSItem item) {
    availableItems.put(item.getId(), item);
    return item;
  }

  public void updateTransactionToRefund(String transactionId){
    for(POSTransaction transaction : this.transactions){
      if(transaction.getId() == transactionId){
        transaction.setRefund(true);
      }
    }
    for(StoreObserver so : storeObservers){
      so.transactionsChanged(this.transactions);
    }
  }

  public void updateTransactionToVoided(String transactionId){
    for(POSTransaction transaction : this.transactions){
      if(transaction.getId() == transactionId){
        transaction.setTransactionType(CardTransactionType.VOID);
      }
    }
    POSPayment payment = (POSPayment) this.getPaymentByCloverId(transactionId);
    if(payment != null) {
      payment.setTransactionType(CardTransactionType.VOID);
      payment.setPaymentStatus(POSPayment.Status.VOIDED);
    }
    for(StoreObserver so : storeObservers){
      so.transactionsChanged(this.transactions);
    }
  }

  public POSTransaction getPaymentByCloverId(String paymentId){
    POSTransaction payment = null;
    for(POSOrder order : this.orders){
      for(POSTransaction orderPayment : order.getPayments()){
        if(orderPayment.getId().equals(paymentId)){
          payment = orderPayment;
        }
      }
    }
    return payment;
  }

  public POSOrder getOrderByCloverPaymentId(String paymentId){
    POSOrder selectedOrder = null;
    for(POSOrder order : this.orders){
      for(POSTransaction payment : order.getPayments()){
        if(payment.getId() == paymentId){
          selectedOrder = order;
        }
      }
    }
    return selectedOrder;
  }

  public void addAvailableDiscount(POSDiscount discount) {
    availableDiscounts.add(discount);
  }

  public POSOrder getCurrentOrder() {
    return currentOrder;
  }

  public void setCurrentOrder(POSOrder order){
    currentOrder = order;
    for(StoreObserver so : storeObservers) {
      so.onCurrentOrderChanged(currentOrder );
    }
  }

  public Collection<POSItem> getAvailableItems() {
    return Collections.unmodifiableCollection(availableItems.values());
  }

  public void addCard(POSCard card) {
    lastVaultedCard = card;
    cards.add(card);
    for(StoreObserver so : storeObservers) {
      so.cardAdded(card);
    }
  }

  public POSCard getLastVaultedCard() {
    return lastVaultedCard;
  }

  public void setLastVaultedCard(POSCard lastVaultedCard) {
    this.lastVaultedCard = lastVaultedCard;
  }

  public List<POSCard> getCards() {
    return Collections.unmodifiableList(cards);
  }

  public List<POSOrder> getOrders() {
    return orders;
  }

  public void addRefund(POSTransaction nakedRefund) {
    refunds.add(nakedRefund);
    for(StoreObserver so : storeObservers) {
      so.refundAdded(nakedRefund);
      so.transactionsChanged(getTransactions());
    }
  }
  public List<POSTransaction> getRefunds() {
    return refunds;
  }

  public List<POSPayment> getPreAuths() {
    return Collections.unmodifiableList(preAuths);
  }

  public void setAllowOfflinePayment(Boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }
  public Boolean getAllowOfflinePayment() {
    return this.allowOfflinePayment;
  }

  public void setForceOfflinePayment(Boolean forceOfflinePayment) {
    this.forceOfflinePayment = forceOfflinePayment;
  }
  public Boolean getForceOfflinePayment() {
    return this.forceOfflinePayment;
  }

  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }
  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return this.approveOfflinePaymentWithoutPrompt;
  }

  public void setCardEntryMethods(int cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  public int getCardEntryMethods() {
    return cardEntryMethods;
  }

  public Boolean getDisablePrinting() {
    return disablePrinting;
  }

  public void setDisablePrinting(Boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }

  public DataEntryLocation getSignatureEntryLocation() {return signatureEntryLocation;}

  public void setSignatureEntryLocation(DataEntryLocation signatureEntryLocation) {this.signatureEntryLocation = signatureEntryLocation;}

  public Long getSignatureThreshold() {return signatureThreshold;}

  public void setSignatureThreshold(Long signatureThreshold) {this.signatureThreshold = signatureThreshold;}

  public Boolean getDisableReceiptOptions() {return disableReceiptOptions;}

  public void setDisableReceiptOptions(Boolean disableReceiptOptions) {this.disableReceiptOptions = disableReceiptOptions;}

  public TipMode getTipMode() {return tipMode;}

  public void setTipMode(TipMode tipMode) {this.tipMode = tipMode;}

  public Long getTipAmount() {return tipAmount;}

  public void setTipAmount(Long tipAmount) {this.tipAmount = tipAmount;}

  public void setPendingPayments(List<PendingPaymentEntry> pendingPayments) {
    this.pendingPayments = pendingPayments;
    for(StoreObserver so : storeObservers) {
      so.pendingPaymentsRetrieved(pendingPayments);
    }
  }

  public Boolean getDisableDuplicateChecking() {
    return disableDuplicateChecking;
  }

  public void setDisableDuplicateChecking(Boolean disableDuplicateChecking) {
    this.disableDuplicateChecking = disableDuplicateChecking;
  }

  public Boolean getAutomaticSignatureConfirmation() {
    return automaticSignatureConfirmation;
  }

  public void setAutomaticSignatureConfirmation(Boolean automaticSignatureConfirmation) {
    this.automaticSignatureConfirmation = automaticSignatureConfirmation;
  }

  public Boolean getAutomaticPaymentConfirmation() {
    return automaticPaymentConfirmation;
  }

  public void setAutomaticPaymentConfirmation(Boolean automaticPaymentConfirmation) {
    this.automaticPaymentConfirmation = automaticPaymentConfirmation;
  }
}