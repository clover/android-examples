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

import java.io.Serializable;
import java.util.List;

public interface StoreObserver extends Serializable{
  public abstract void onCurrentOrderChanged(POSOrder currentOrder);
  public abstract void newOrderCreated(POSOrder order, boolean userInitiated);
  public abstract void cardAdded(POSCard card);
  public abstract void refundAdded(POSTransaction refund);
  public abstract void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments);
  public abstract void transactionsChanged(List<POSTransaction> transactions);
}

