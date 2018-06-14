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

import com.clover.sdk.v3.payments.CardEntryType;
import com.clover.sdk.v3.payments.CardTransactionState;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Result;

import java.io.Serializable;
import java.util.Date;

public class POSTransaction implements Serializable {

  private long amount = 0L;
  private String cardDetails, deviceId, id, transactionTitle, employee, tender;
  private CardType cardType;
  CardEntryType entryMethod;
  CardTransactionType transactionType;
  CardTransactionState transactionState;
  private Result result;
  private Date date = new Date();
  private boolean refund = false;

  public POSTransaction() {
  }

  public POSTransaction(String paymentID, String employeeID, long amount) {
    this.id = paymentID;
    this.employee = employeeID;
    this.amount = amount;
  }

  public POSTransaction(long amount, String cardDetails, CardType cardType, Date date, String id, String tender, String transactionTitle, CardTransactionType transactionType, boolean refund, CardEntryType entryMethod, CardTransactionState transactionState){
    this.amount = amount;
    this.cardDetails = cardDetails;
    this.cardType = cardType;
    this.date = date;
    this.id = id;
    this.tender = tender;
    this.transactionTitle = transactionTitle;
    this.transactionType = transactionType;
    this.refund = refund;
    this.entryMethod = entryMethod;
    this.transactionState = transactionState;
  }

  public long getAmount(){
    return this.amount;
  }

  public void setAmount(Long amount){
    this.amount = amount;
  }

  public String getCardDetails(){
    return this.cardDetails;
  }

  public void setCardDetails(String cardDetails){
    this.cardDetails = cardDetails;
  }

  public CardType getCardType(){
    return this.cardType;
  }

  public void setCardType(CardType cardType){
    this.cardType = cardType;
  }

  public Date getDate(){
    return this.date;
  }

  public void setDate(Date date){
    this.date = date;
  }

  public String getEmployee(){
    return this.employee;
  }

  public void setEmployee(String employee){
    this.employee = employee;
  }

  public String getId(){
    return this.id;
  }

  public void setId(String id){
    this.id = id;
  }

  public String getTender(){
    return this.tender;
  }

  public void setTender(String tender){
    this.tender = tender;
  }

  public String getTransactionTitle(){
    return this.transactionTitle;
  }

  public void setTransactionTitle(String transactionTitle){
    this.transactionTitle = transactionTitle;
  }

  public CardTransactionType getTransactionType(){
    return this.transactionType;
  }

  public void setTransactionType(CardTransactionType transactionType){
    this.transactionType = transactionType;
  }

  public boolean getRefund(){
    return this.refund;
  }

  public void setRefund(boolean refund){
    this.refund = refund;
  }

  public CardEntryType getEntryMethod(){
    return this.entryMethod;
  }

  public void setEntryMethod(CardEntryType entryMethod){
    this.entryMethod = entryMethod;
  }

  public CardTransactionState getTransactionState(){
    return this.transactionState;
  }

  public void setTransactionState(CardTransactionState state){
    this.transactionState = state;
  }

  public String getDeviceId(){
    return this.deviceId;
  }

  public void setDeviceId(String id){
    this.deviceId = id;
  }

  public boolean isRefund() {
    return refund;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }
}
