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

public class POSRefund extends POSTransaction implements Serializable{
  public String paymentId;
  public String cloverOrderId;
  public POSRefund(String refundID, String paymentID, String orderID, String employeeID, long amount) {
    super(refundID, employeeID, amount);
    this.paymentId = paymentID;
    this.cloverOrderId = orderID;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public String getCloverOrderId() {
    return cloverOrderId;
  }

  public void setCloverOrderId(String cloverOrderId) {
    this.cloverOrderId = cloverOrderId;
  }
}