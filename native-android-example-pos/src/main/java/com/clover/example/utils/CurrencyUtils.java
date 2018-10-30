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

package com.clover.example.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils {

  public static String format(long value, Locale locale) {
    double amount = value / 100f;
    if(locale == null) {
      locale = Locale.getDefault();
    }
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

    return currencyFormatter.format(amount);
  }

  public static Long convertToLong(String amount){
    amount = amount.replace(",", "");
    amount = amount.substring(1,amount.length());
    String [] pieces = amount.split("\\.");
    amount = pieces[0]+pieces[1];
    return Long.parseLong(amount);
  }

  public static String convertToString(Long amount){
    if(amount == 0L){
      return "$0.00";
    }
    else {
      String amountString = String.valueOf(amount);
      String first = "";
      if(amountString.length()>1) {
        first = amountString.substring(0, amountString.length() - 2);
      }
      else{
        return "$0.0"+amountString;
      }
      if(first.length() < 1){
        first = "0";
      }
      String last = amountString.substring(amountString.length()-2);
      return "$"+first+"."+last;

    }
  }
}
