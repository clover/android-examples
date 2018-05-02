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

public interface OrderObserver extends Serializable{
  public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem);

  public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem);

  public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem);

  public void paymentAdded(POSOrder posOrder, POSPayment payment);

  public void refundAdded(POSOrder posOrder, POSRefund refund);

  public void paymentChanged(POSOrder posOrder, POSTransaction pay);

  public void discountAdded(POSOrder posOrder, POSDiscount discount);

  public void discountChanged(POSOrder posOrder, POSDiscount discount);

  public void paymentVoided(POSOrder posOrder, POSPayment payment);
}
