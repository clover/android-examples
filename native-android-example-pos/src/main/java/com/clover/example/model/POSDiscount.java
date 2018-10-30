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

public class POSDiscount implements Serializable{
  public String name;
  private long _amountOff = 0;
  private float _percentageOff = 0.0f;

  public POSDiscount() {
    name = "";
  }

  public POSDiscount(String name, float percentOff) {
    this.name = name;
    _percentageOff = percentOff;
  }

  public POSDiscount(String name, long amountOff) {
    this.name = name;
    _amountOff = amountOff;
  }

  public long getAmountOff() {
    return _amountOff;
  }

  public void setAmountOff(long value) {
    _percentageOff = 0.0f;
    _amountOff = value;
  }

  public float getPercentageOff() {
    return _percentageOff;
  }

  public void setPercentageOff(float value) {
    _amountOff = 0;
    _percentageOff = value;
  }

  public String getName() {
    return name;
  }

  protected long appliedTo(long sub) {
    if (getAmountOff() == 0) {
      sub = (long) Math.round(sub - (sub * getPercentageOff()));
    } else {
      sub -= getAmountOff();
    }
    return Math.max(sub, 0);
  }

  public long getValue(long sub) {
    long value = _amountOff;
    if (getAmountOff() == 0) {
      value = (long) Math.round(sub * getPercentageOff());
    }

    return value;
  }
}
