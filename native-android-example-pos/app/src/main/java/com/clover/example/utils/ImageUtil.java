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

import com.clover.example.R;
import com.clover.sdk.v3.payments.CardType;

public class ImageUtil {

  public static int getCardTypeImage(CardType cardType){
    int image = R.drawable.tender_default;
    if(cardType == CardType.VISA){
      image = R.drawable.tender_visa;
    }
    else if(cardType == CardType.AMEX){
      image = R.drawable.tender_amex;
    }
    else if(cardType == CardType.MC){
      image = R.drawable.tender_mc;
    }
    else if(cardType == CardType.DISCOVER){
      image = R.drawable.tender_disc;
    }
    else if(cardType == CardType.EBT) {
      image = R.drawable.tender_ebt;
    }
    return image;
  }
}
