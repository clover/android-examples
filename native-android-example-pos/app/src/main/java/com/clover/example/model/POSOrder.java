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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class POSOrder implements Serializable{

  public enum OrderStatus implements Serializable{
    OPEN, CLOSED, LOCKED, PAID, INITIAL, PARTIALLY_PAID {
      @Override public String toString() {
        return "PARTIALLY PAID";
      }
    };
  }

  private List<POSLineItem> items;
  private List<POSTransaction> payments;
  private POSPayment preAuth;
  private POSDiscount discount;
  public String id;
  public Date date;

  private transient List<OrderObserver> observers = new ArrayList<OrderObserver>();
  private final String TAG = POSOrder.class.getSimpleName();

  public POSOrder() {
    items = new ObservableList<POSLineItem>();
    payments = new ObservableList<POSTransaction>();
    discount = new POSDiscount("None", 0);
    date = new Date();
  }

  public void addOrderObserver(OrderObserver observer) {
    this.observers.add(observer);
  }

  public void removeObserver(OrderObserver observer) {
    this.observers.remove(observer);
  }

  public long getPreDiscountSubTotal() {
    long sub = 0;
    for (POSLineItem li : items) {
      sub += li.getPrice() * li.getQuantity();
    }
    return sub;
  }

  public long getPreTaxSubTotal() {
    long sub = 0;
    for (POSLineItem li : items) {
      sub += li.getPrice() * li.getQuantity();
    }
    if (discount != null) {
      sub = discount.appliedTo(sub);
    }
    return sub;
  }

  public long getTippableAmount() {
    long tippableAmount = 0;
    for (POSLineItem li : items) {
      if (li.getItem().isTippable()) {
        tippableAmount += li.getPrice() * li.getQuantity();
      }
    }
    if (discount != null) {
      tippableAmount = discount.appliedTo(tippableAmount);
    }
    return tippableAmount + getTaxAmount(); // shuold match Total if there aren't any "non-tippable" items

  }

  public long getTaxableSubtotal() {
    long sub = 0;
    for (POSLineItem li : items) {
      if (li.getItem().isTaxable()) {
        sub += li.getPrice() * li.getQuantity();
      }
    }
    if (discount != null) {
      sub = discount.appliedTo(sub);
    }
    return sub;
  }

  public long getTaxAmount() {
    return (int) (getTaxableSubtotal() * 0.07);
  }

  public long getTotal() {
    return getPreTaxSubTotal() + getTaxAmount();
  }

  public long getTips() {
    long tips = 0;
    for (POSTransaction posPayment : payments) {
      if (posPayment instanceof POSPayment) {
        tips += ((POSPayment) posPayment).getTipAmount();
      }
    }
    return tips;
  }


  /// <summary>
  /// manages adding a POSItem to an order. If the POSItem already exists, the quantity is just incremented
  /// </summary>
  /// <param name="i"></param>
  /// <param name="quantity"></param>
  /// <returns>The POSLineItem for the POSItem. Will either return a new one, or an exising with its quantity incremented</returns>
  public POSLineItem addItem(POSItem i, int quantity) {
    boolean exists = false;
    POSLineItem targetItem = null;
    for (POSLineItem lineI : items) {
      if (lineI.getItem().getId().equals(i.getId())) {
        exists = true;
        lineI.incrementQuantity(quantity);
        targetItem = lineI;
        //notifyObserverItemChanged(targetItem);
        break;
      }
    }
    if (!exists) {
      POSLineItem li = new POSLineItem(this, i, quantity);
      targetItem = li;
      items.add(targetItem);
      notifyObserverItemAdded(targetItem);
    }
    return targetItem;
  }

  public boolean removeItem(POSLineItem li, int quantity) {
    if(li.getQuantity() <= quantity) {
      return remoteAllItems(li);
    } else {
      li.setQuantity(li.getQuantity()-quantity);
      notifyObserverItemChanged(li);
    }
    return true;
  }

  public boolean remoteAllItems(POSLineItem li) {
    boolean removed = items.remove(li);
    if(removed) {
      notifyObserverItemRemoved(li);
    }
    return removed;
  }

  void addPayment(POSPayment payment) {
    payments.add(payment);
    payment.setOrder(this);
    notifyObserverPaymentAdded(payment);
  }


  void addRefund(POSRefund refund) {
    for (POSTransaction pay : payments) {
      if (pay instanceof POSPayment) {
        if (pay.getId().equals(refund.getId())) {
          ((POSPayment) pay).setPaymentStatus(POSPayment.Status.REFUNDED);
          notifyObserverPaymentChanged(pay);
        }

      }
    }
    payments.add(refund);
    notifyObserverRefundAdded(refund);
  }

  public POSOrder.OrderStatus getStatus() {
    if(items.size() == 0 && payments.size() == 0) {
      return OrderStatus.INITIAL;
    } else {
      long totalPaid = 0;
      for(POSTransaction payment : payments) {
        if(payment instanceof POSPayment) {
          if(((POSPayment) payment).getPaymentStatus() != POSPayment.Status.VOIDED) {
            totalPaid += payment.getAmount();
          }
        } else if(payment instanceof POSRefund) {
          totalPaid -= payment.getAmount();
        }
      }
      if(getTotal() > 0 && totalPaid >= getTotal()) {
        return OrderStatus.PAID;
      } else if (totalPaid > 0) {
        return OrderStatus.PARTIALLY_PAID;
      } else {
        return OrderStatus.OPEN;
      }
    }
  }

  public void voidPayment(POSPayment payment){
    payment.setPaymentStatus(POSPayment.Status.VOIDED);
    payment.setId("");
    notifyObserverPaymentVoided(payment);
  }


  protected void removeItem(POSLineItem selectedLineItem) {
    items.remove(selectedLineItem);
    notifyObserverItemRemoved(selectedLineItem);
  }


  public List<POSLineItem> getItems() {
    return items;
  }

  public List<POSTransaction> getPayments() {
    return Collections.unmodifiableList(payments);
  }

  public POSPayment getPreAuth() {
    return preAuth;
  }

  public void setPreAuth(POSPayment preAuth) {
    this.preAuth = preAuth;
  }

  public void setDiscount(POSDiscount discount) {
    this.discount = discount;
    notifyObserverDiscountChanged(discount);
  }


  public POSDiscount getDiscount() {
    return discount;
  }

  void notifyObserverItemAdded(POSLineItem targetItem) {
    for (OrderObserver observer : observers) {
      observer.lineItemAdded(this, targetItem);
    }
  }

  void notifyObserverItemChanged(POSLineItem targetItem) {
    for (OrderObserver observer : observers) {
      observer.lineItemChanged(this, targetItem);
    }
  }

  void notifyObserverPaymentAdded(POSPayment payment) {
    for (OrderObserver observer : observers) {
      observer.paymentAdded(this, payment);
    }
  }

  void notifyObserverRefundAdded(POSRefund refund) {
    for (OrderObserver observer : observers) {
      observer.refundAdded(this, refund);
    }
  }

  public void notifyObserverPaymentChanged(POSTransaction pay) {
    for (OrderObserver observer : observers) {
      observer.paymentChanged(this, pay);
    }
  }

  void notifyObserverItemRemoved(POSLineItem lineItem) {
    for (OrderObserver observer : observers) {
      observer.lineItemRemoved(this, lineItem);
    }
  }

  void notifyObserverDiscountChanged(POSDiscount discount) {
    for (OrderObserver observer : observers) {
      observer.discountChanged(this, discount);
    }
  }

  void notifyObserverPaymentVoided(POSPayment payment){
    for (OrderObserver observer : observers){
      observer.paymentVoided(this, payment);
    }
  }
}
