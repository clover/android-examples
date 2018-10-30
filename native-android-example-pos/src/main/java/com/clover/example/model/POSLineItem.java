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
import java.util.UUID;

public class POSLineItem implements Serializable{
  private transient POSOrder order;
  private POSLineItemDiscount discount;
  public String id;
  private POSItem item;

  public POSLineItem(POSOrder order, POSItem item) {
    this.item = item;
    this.order = order;
    this.quantity = 1;
    id = UUID.randomUUID().toString();
  }

  int quantity = 1;


  public POSLineItem(POSOrder order, POSItem item, int quantity) {
    this.item = item;
    this.order = order;
    this.quantity = quantity;
    id = UUID.randomUUID().toString();
  }


  public long getPrice() {
    if (discount != null) {
      return item.getPrice() - discount.getValue(item);
    } else {
      return item.getPrice();
    }
  }

  public void setDiscount(POSLineItemDiscount discount) {
    this.discount = discount;
    order.notifyObserverItemChanged(this);
  }

  public POSDiscount getDiscount() {
    return discount;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int newQuantity) {
    quantity = newQuantity;
    order.notifyObserverItemChanged(this);
  }

  public void incrementQuantity(int quantity) {
    this.quantity += quantity;
    this.quantity = Math.max(0, this.quantity);
    order.notifyObserverItemChanged(this);
  }

  public String getId() {
    return id;
  }

  public POSItem getItem() {
    return item;
  }
}
