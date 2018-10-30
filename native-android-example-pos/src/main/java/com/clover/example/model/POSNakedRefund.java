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

import com.clover.sdk.v3.payments.CardType;

import java.io.Serializable;
import java.util.Date;

public class POSNakedRefund extends POSTransaction implements Serializable{
  public String EmployeeID;
  public Date date;
  public long Amount;
  public String cardDetails;
  public CardType cardType;
  public String entryMethod;
  public String id;
  public boolean refund = true;
  public String tender;
  public String transactionState;
  public String transactionTitle;
  public String transacationType;

  public POSNakedRefund(String employeeID, long amount) {
    EmployeeID = employeeID;
    Amount = amount;
    date = new Date();
  }

}